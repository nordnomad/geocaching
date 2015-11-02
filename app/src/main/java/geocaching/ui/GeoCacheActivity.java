package geocaching.ui;

import android.location.Location;
import android.net.Uri;
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
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import geocaching.ContentProviderManager;
import geocaching.ExternalStorageManager;
import geocaching.GeoCache;
import geocaching.GeoCacheInfo;
import geocaching.GoTo;
import geocaching.common.SlidingTabLayout;
import geocaching.ui.adapters.GeoCacheActivityPagerAdapter;
import map.test.myapplication3.app.R;

import static geocaching.Utils.isBlank;

public class GeoCacheActivity extends AppCompatActivity implements Response.ErrorListener {

    public RequestQueue queue;
    public ContentProviderManager cpm;
    public ExternalStorageManager esm;

    public GeoCache geoCache;
    public JSONObject infoObject;
    public JSONArray commentsArray;
    public JSONArray photosArray;
    public DefaultRetryPolicy retryPolicy;
    public List<Uri> photos;
    public boolean loaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoCache = getIntent().getParcelableExtra("geoCache");
        setTitle(geoCache.name);
        esm = new ExternalStorageManager(this);
        cpm = new ContentProviderManager(this);

        GeoCacheInfo geoCacheInfo = cpm.findGeoCache(geoCache.id);
        infoObject = geoCacheInfo.info;
        commentsArray = geoCacheInfo.comments;
        photosArray = geoCacheInfo.webPhotoUrls;
        photos = geoCacheInfo.filePhotoUrls;

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
                if (!loaded) return true;
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
                    cpm.saveGeoCacheFullInfo(jsonObject);
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
                cpm.deleteGeoCache(geoCache.id);
                removeCacheItem.setVisible(false);
                saveCacheItem.setVisible(true);
                return true;
            }
        });
        if (cpm.isGeoCacheInFavouriteList(geoCache.id)) {
            removeCacheItem.setVisible(true);
            saveCacheItem.setVisible(false);
        } else {
            saveCacheItem.setVisible(true);
            removeCacheItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        String message = error.getMessage();
        if (isBlank(message)) {
            message = error.getClass().getName();
        }
        Toast.makeText(GeoCacheActivity.this, message, Toast.LENGTH_LONG).show();
    }

}
