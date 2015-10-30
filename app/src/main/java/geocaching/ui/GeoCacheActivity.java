package geocaching.ui;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
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
import geocaching.ui.adapters.GeoCacheActivityPagerAdapter;
import map.test.myapplication3.app.R;

import static geocaching.Utils.isBlank;
import static geocaching.Utils.jsonGeoCacheToContentValues;
import static geocaching.db.DBUtil.isGeoCacheInFavouriteList;

public class GeoCacheActivity extends AppCompatActivity implements Response.ErrorListener {

    public RequestQueue queue;
    public GeoCache geoCache;
    public JSONObject infoObject;
    public JSONArray commentsArray;
    public JSONArray photosArray;
    public DefaultRetryPolicy retryPolicy;
    public ExternalStorageManager esm;
    public List<Uri> photos;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoCache = getIntent().getParcelableExtra("geoCache");
        setTitle(geoCache.name);
        esm = new ExternalStorageManager(this);
        try (Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCache.id), null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    int infoIndex = cursor.getColumnIndex(DB.Column.DESCR);
                    infoObject = new JSONObject(cursor.getString(infoIndex));
                    int commentsIndex = cursor.getColumnIndex(DB.Column.COMMENTS);
                    commentsArray = new JSONArray(cursor.getString(commentsIndex));
                    int photosIndex = cursor.getColumnIndex(DB.Column.PHOTOS);
                    photosArray = new JSONArray(cursor.getString(photosIndex));
                    photos = esm.getPhotos(geoCache.id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        setContentView(R.layout.activity_geo_cache);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new GeoCacheActivityPagerAdapter(this));

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
        final List<String> urls = urls(photosArray);
        for (String url : urls) {
            queue.add(new ImageRequest(url, new BitmapResponseListener(url), 0, 0, null, null));
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

    public static List<String> urls(JSONArray jsonArray) {
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
