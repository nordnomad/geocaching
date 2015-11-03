package geocaching.ui;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import geocaching.GeoCache;
import geocaching.GeoCacheInfo;
import geocaching.GoTo;
import geocaching.Utils;
import geocaching.common.SlidingTabLayout;
import geocaching.managers.Network;
import geocaching.managers.Storage;
import geocaching.ui.adapters.GeoCacheActivityPagerAdapter;
import map.test.myapplication3.app.R;

import static geocaching.Utils.isBlank;

public class GeoCacheActivity extends AppCompatActivity implements Response.ErrorListener, Response.Listener<JSONObject> {

    GeoCache geoCache;
    GeoCacheInfo geoCacheInfo;
    ViewPager viewPager;
    MenuItem saveCacheItem;
    MenuItem removeCacheItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoCache = getIntent().getParcelableExtra("geoCache");
        setTitle(geoCache.name);
        geoCacheInfo = Storage.with(this).findGeoCache(geoCache.id);
        if (geoCacheInfo.isEmpty()) {
            Network.with(this).loadGeoCacheFullInfo(geoCache.id, this, this);
        }
        setContentView(R.layout.activity_geo_cache);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new GeoCacheActivityPagerAdapter(this, geoCacheInfo));

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);
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
        removeCacheItem = menu.findItem(R.id.remove_cache);
        saveCacheItem = menu.findItem(R.id.save_cache);

        saveCacheItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Storage.with(getApplication()).saveGeoCacheFullInfo(Utils.merge(geoCache, geoCacheInfo));
                removeCacheItem.setVisible(true);
                saveCacheItem.setVisible(false);
                return true;
            }
        });
        removeCacheItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Storage.with(getApplication()).deleteGeoCache(geoCache.id);
                removeCacheItem.setVisible(false);
                saveCacheItem.setVisible(true);
                return true;
            }
        });
        if (geoCacheInfo.isEmpty()) {
            removeCacheItem.setVisible(false);
            saveCacheItem.setVisible(false);
        } else {
            removeCacheItem.setVisible(true);
            saveCacheItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(getLocalClassName(), error.getMessage(), error);
        String message = error.getMessage();
        if (isBlank(message)) {
            message = error.getClass().getName();
        }
        Toast.makeText(GeoCacheActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            geoCacheInfo = new GeoCacheInfo(response);
            saveCacheItem.setVisible(true);
            removeCacheItem.setVisible(false);
            viewPager.setAdapter(new GeoCacheActivityPagerAdapter(this, geoCacheInfo));
        } catch (JSONException e) {
            Log.e(getLocalClassName(), e.getMessage(), e);
        }
    }
}