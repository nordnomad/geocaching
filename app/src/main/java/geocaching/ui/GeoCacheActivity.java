package geocaching.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import java.util.ArrayList;
import java.util.List;

import geocaching.GoTo;
import geocaching.common.SlidingTabLayout;
import geocaching.ui.adapters.CommentsTabAdapter;
import geocaching.ui.adapters.ImageGridAdapter;
import map.test.myapplication3.app.R;

import static com.android.volley.Request.Method.GET;
import static geocaching.Const.M.commentsUrl;
import static geocaching.Const.M.imagesUrl;
import static geocaching.Const.M.infoUrl;

public class GeoCacheActivity extends AppCompatActivity implements Response.ErrorListener {

    RequestQueue queue;
    long geoCacheId;

    JSONObject infoObject;
    JSONArray commentsArray;
    JSONArray photosArray;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoCacheId = getIntent().getLongExtra("geoCacheId", 0);
        setTitle(getIntent().getStringExtra("name"));
//        try (Cursor cursor = getContentResolver().query(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCacheId), null, null, null, null)) {
//            try {
//                cursor.moveToFirst();
//                int infoIndex = cursor.getColumnIndex(DB.Column.DESCR);
//                infoObject = new JSONObject(cursor.getString(infoIndex));
//                int commentsIndex = cursor.getColumnIndex(DB.Column.COMMENTS);
//                commentsArray = new JSONArray(cursor.getString(commentsIndex));
//                int photosIndex = cursor.getColumnIndex(DB.Column.PHOTOS);
//                photosArray = new JSONArray(cursor.getString(photosIndex));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
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
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(GeoCacheActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
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

        @TargetApi(Build.VERSION_CODES.KITKAT)
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
                        queue.add(new JsonObjectRequest(GET, infoUrl(geoCacheId), null, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                setDataToInfoTab(response, view, bar);
                            }
                        }, ctx));
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
                        queue.add(new JsonArrayRequest(commentsUrl(geoCacheId), new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                setDataToCommentsTab(response, recyclerView, commentsBar);
                            }
                        }, ctx));
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
                        queue.add(new JsonArrayRequest(imagesUrl(geoCacheId), new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(final JSONArray response) {
                                setDataToPhotoTab(response, ctx, gridView);
                            }
                        }, ctx));
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
                    GoTo.imagePagerActivity(ctx, urls(response));
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

}
