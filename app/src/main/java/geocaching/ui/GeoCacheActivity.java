package geocaching.ui;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import geocaching.ExternalStorageManager;
import geocaching.GeoCache;
import geocaching.GoTo;
import geocaching.common.SlidingTabLayout;
import geocaching.db.DB;
import geocaching.db.GeoCacheProvider;
import geocaching.ui.adapters.CommentsTabAdapter;
import geocaching.ui.adapters.ImageGridAdapter;
import map.test.myapplication3.app.R;

import static com.android.volley.Request.Method.GET;
import static geocaching.Const.M.commentsUrl;
import static geocaching.Const.M.imagesUrl;
import static geocaching.Const.M.infoUrl;
import static geocaching.Utils.isBlank;
import static geocaching.Utils.jsonGeoCacheToContentValues;
import static geocaching.db.DBUtil.isGeoCacheInFavouriteList;

public class GeoCacheActivity extends AppCompatActivity implements Response.ErrorListener {

    RequestQueue queue;
    GeoCache geoCache;
    JSONObject infoObject;
    JSONArray commentsArray;
    JSONArray photosArray;
    DefaultRetryPolicy retryPolicy;
    ExternalStorageManager esm;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoCache = getIntent().getParcelableExtra("geoCache");
        setTitle(geoCache.name);

        try (Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCache.id), null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    int infoIndex = cursor.getColumnIndex(DB.Column.DESCR);
                    infoObject = new JSONObject(cursor.getString(infoIndex));
                    int commentsIndex = cursor.getColumnIndex(DB.Column.COMMENTS);
                    commentsArray = new JSONArray(cursor.getString(commentsIndex));
                    int photosIndex = cursor.getColumnIndex(DB.Column.PHOTOS);
                    photosArray = new JSONArray(cursor.getString(photosIndex));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        setContentView(R.layout.activity_geo_cache);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new GeoCachePagerAdapter());

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);
        queue = Volley.newRequestQueue(this);
        retryPolicy = new DefaultRetryPolicy(2000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        esm = new ExternalStorageManager(this);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.geo_cache_activity_action_bar, menu);
        MenuItem findCacheItem = menu.findItem(R.id.find_cache);
        findCacheItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Location cacheLocation = new Location("");
                cacheLocation.setLatitude(geoCache.la);
                cacheLocation.setLongitude(geoCache.ln);
                GoTo.compassActivity(GeoCacheActivity.this, geoCache.id, geoCache.name, cacheLocation);
                return true;
            }
        });
        final MenuItem saveCacheItem = menu.findItem(R.id.save_cache);
        final MenuItem removeCacheItem = menu.findItem(R.id.remove_cache);
        saveCacheItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ContentResolver resolver = GeoCacheActivity.this.getContentResolver();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id", geoCache.id);
                    jsonObject.put("n", geoCache.name);
                    jsonObject.put("ln", geoCache.ln);
                    jsonObject.put("la", geoCache.la);
                    jsonObject.put("st", geoCache.status.ordinal());
                    jsonObject.put("ct", geoCache.type.ordinal());
                    jsonObject.put("info", infoObject);
                    jsonObject.put("images", photosArray);
                    jsonObject.put("comments", commentsArray);
                    resolver.insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, jsonGeoCacheToContentValues(jsonObject));
                    savePhotos(photosArray);
                } catch (JSONException e) {
                    Log.e(GeoCacheActivity.class.getName(), e.getMessage(), e);
                }
                removeCacheItem.setVisible(true);
                saveCacheItem.setVisible(false);
                return true;
            }
        });
        removeCacheItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ContentResolver resolver = GeoCacheActivity.this.getContentResolver();
                resolver.delete(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCache.id), null, null);
                removeCacheItem.setVisible(false);
                saveCacheItem.setVisible(true);
                return true;
            }
        });
        if (isGeoCacheInFavouriteList(this, geoCache.id)) {
            removeCacheItem.setVisible(true);
            saveCacheItem.setVisible(false);
        } else {
            saveCacheItem.setVisible(true);
            removeCacheItem.setVisible(false);
        }
        return true;
    }

    private void savePhotos(JSONArray photosArray) throws JSONException {
        for (int i = 0; i < photosArray.length(); i++) {
            JSONObject photoObject = photosArray.getJSONObject(i);
            String cachesUrl = photoObject.has("caches") ? photoObject.getString("caches") : "";
            if (!isBlank(cachesUrl))
                queue.add(new ImageRequest(cachesUrl, new BitmapResponseListener(cachesUrl), 0, 0, null, null));
            String areasUrl = photoObject.has("areas") ? photoObject.getString("areas") : "";
            if (!isBlank(areasUrl))
                queue.add(new ImageRequest(areasUrl, new BitmapResponseListener(areasUrl), 0, 0, null, null));
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        String message = error.getMessage();
        if (isBlank(message)) {
            message = error.getClass().getName();
        }
        Toast.makeText(GeoCacheActivity.this, message, Toast.LENGTH_LONG).show();
    }

    class GeoCachePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Информация";
                case 1:
                    return "Коментарии";
                case 2:
                    return "Фотографии";
            }
            return "Ошибка";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final GeoCacheActivity ctx = GeoCacheActivity.this;
            final View view;
            switch (position) {
                case 0:
                    view = ctx.getLayoutInflater().inflate(R.layout.activity_geo_cache_info_tab, container, false);
                    final ProgressBar bar = (ProgressBar) view.findViewById(R.id.infoLoading);
                    bar.setVisibility(View.VISIBLE);
                    container.addView(view);
                    if (infoObject == null) {
                        JsonObjectRequest request = new JsonObjectRequest(GET, infoUrl(geoCache.id), null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                infoObject = response;
                                setDataToInfoTab(response, view, bar);
                            }
                        }, ctx);
                        request.setRetryPolicy(retryPolicy);
                        queue.add(request);
                    } else {
                        setDataToInfoTab(infoObject, view, bar);
                    }
                    return view;
                case 1:
                    view = ctx.getLayoutInflater().inflate(R.layout.activity_geo_cache_comment_tab, container, false);
                    final ProgressBar commentsBar = (ProgressBar) view.findViewById(R.id.commentsLoading);
                    commentsBar.setVisibility(View.VISIBLE);
                    final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.commentsList);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
                    recyclerView.setAdapter(new CommentsTabAdapter(new JSONArray()));
                    container.addView(view);
                    if (commentsArray == null) {
                        JsonArrayRequest request = new JsonArrayRequest(commentsUrl(geoCache.id), new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                commentsArray = response;
                                setDataToCommentsTab(response, recyclerView, commentsBar);
                            }
                        }, ctx);
                        request.setRetryPolicy(retryPolicy);
                        queue.add(request);
                    } else {
                        setDataToCommentsTab(commentsArray, recyclerView, commentsBar);
                    }
                    return view;
                case 2:
                    view = ctx.getLayoutInflater().inflate(R.layout.activity_geo_cache_foto_tab, container, false);
                    final GridView gridView = (GridView) view.findViewById(R.id.gallery);
                    gridView.setAdapter(new ImageGridAdapter(ctx, new JSONArray()));
                    container.addView(view);
                    if (photosArray == null) {
                        JsonArrayRequest request = new JsonArrayRequest(imagesUrl(geoCache.id), new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(final JSONArray response) {
                                photosArray = response;
                                setDataToPhotoTab(response, ctx, gridView);
                            }
                        }, ctx);
                        request.setRetryPolicy(retryPolicy);
                        queue.add(request);
                    } else {
                        setDataToPhotoTab(photosArray, ctx, gridView);
                    }
                    return view;
            }
            return null;
        }

        private void setDataToPhotoTab(final JSONArray response, final GeoCacheActivity ctx, GridView gridView) {
            ImageGridAdapter gridAdapter = new ImageGridAdapter(ctx, response);
            gridView.setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO decide open images by own activity or standart one
//                    GoTo.imagePagerActivity(ctx, geoCacheId, geoCacheName, urls(response));
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(urls(response).get(position)), "image/*");
                    startActivity(intent);
                }
            });
        }

        private void setDataToCommentsTab(JSONArray response, RecyclerView recyclerView, ProgressBar commentsBar) {
            RecyclerView.Adapter mAdapter = new CommentsTabAdapter(response);
            recyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            commentsBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        private void setDataToInfoTab(JSONObject response, View view, ProgressBar bar) {
            try {
                TextView createDate = (TextView) view.findViewById(R.id.createDate);
                createDate.setText(response.getString("created"));
                TextView updateDate = (TextView) view.findViewById(R.id.updateDate);
                updateDate.setText(response.getString("updated"));
                TextView country = (TextView) view.findViewById(R.id.country);
                country.setText(response.getString("country"));
                TextView city = (TextView) view.findViewById(R.id.city);
                city.setText(response.getString("city"));
                TextView location = (TextView) view.findViewById(R.id.location);
                location.setText(response.getString("coordinates"));
                TextView author = (TextView) view.findViewById(R.id.author);
                author.setText(response.getJSONObject("author").getString("name"));
                TextView descriptionText = (TextView) view.findViewById(R.id.descriptionText);
                descriptionText.setText(response.getString("description"));
                TextView surroundingArea = (TextView) view.findViewById(R.id.surroundingArea);
                surroundingArea.setText(response.getString("surroundingArea"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            bar.setVisibility(View.GONE);
            view.findViewById(R.id.infoCard).setVisibility(View.VISIBLE);
            view.findViewById(R.id.description).setVisibility(View.VISIBLE);
            view.findViewById(R.id.area).setVisibility(View.VISIBLE);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private static List<String> urls(JSONArray jsonArray) {
        List<String> result = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.has("areas")) {
                    result.add(jsonObject.getString("areas"));
                } else if (jsonObject.has("caches")) {
                    result.add(jsonObject.getString("caches"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class BitmapResponseListener implements Response.Listener<Bitmap> {
        private final String imageUrl;

        public BitmapResponseListener(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        public void onResponse(Bitmap response) {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/"));
            File file = esm.getPhotoFile(fileName, geoCache.id);
            FileOutputStream fOut = null;
            try {
                fOut = new FileOutputStream(file);
                response.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fOut != null) {
                    try {
                        fOut.flush();
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
