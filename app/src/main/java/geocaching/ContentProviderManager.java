package geocaching;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import geocaching.db.DB;
import geocaching.db.GeoCacheProvider;

import static geocaching.Utils.jsonGeoCacheToContentValues;
import static geocaching.Utils.urls;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class ContentProviderManager {

    Context ctx;
    RequestQueue queue;
    ExternalStorageManager esm;

    public ContentProviderManager(Context ctx) {
        this.ctx = ctx;
        queue = Volley.newRequestQueue(ctx);
        esm = new ExternalStorageManager(ctx);
    }

    public List<Long> idsOfStoredGeoCaches() {
        List<Long> excludedIds = new ArrayList<>();
        try (Cursor cursor = ctx.getContentResolver().query(GeoCacheProvider.GEO_CACHE_CONTENT_URI, new String[]{DB.Column._ID}, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    excludedIds.add(cursor.getLong(0));
                }
            }
        }
        if (excludedIds.isEmpty()) excludedIds.add(0l);
        return excludedIds;
    }

    public void saveGeoCacheFullInfo(JSONObject geoCache) {
        try {
            ctx.getContentResolver().insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, jsonGeoCacheToContentValues(geoCache));
            savePhotos(geoCache.getJSONArray("images"), geoCache.getInt("id"));
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
    }

    private void savePhotos(JSONArray photosArray, int geoCacheId) {
        final List<String> urls = urls(photosArray);
        for (String url : urls) {
            queue.add(new ImageRequest(url, new BitmapResponseListener(ctx, url, geoCacheId), 0, 0, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                }
            }));
        }
    }

    public boolean isGeoCacheInFavouriteList(int geoCacheId) {
        int count = 0;
        try (Cursor countCursor = ctx.getContentResolver().query(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCacheId),
                new String[]{"count(*) AS count"}, null, null, null)) {
            if (countCursor != null) {
                countCursor.moveToFirst();
                count = countCursor.getInt(0);
            }
        }
        return count > 0;
    }

    public boolean isFavouriteListEmpty() {
        int count = 0;
        try (Cursor countCursor = ctx.getContentResolver().query(GeoCacheProvider.GEO_CACHE_CONTENT_URI,
                new String[]{"count(*) AS count"}, null, null, null)) {
            if (countCursor != null) {
                countCursor.moveToFirst();
                count = countCursor.getInt(0);
            }
        }
        return count == 0;
    }

    public void deleteGeoCache(int geoCacheId) {
        ctx.getContentResolver().delete(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCacheId), null, null);
        esm.deletePhotos(geoCacheId);
    }

    public void deleteGeoCaches(long[] ids) {
        String where = "_id IN " + Arrays.toString(ids).replace("[", "(").replace("]", ")");
        ctx.getContentResolver().delete(GeoCacheProvider.GEO_CACHE_CONTENT_URI, where, null);
        for (long id : ids) {
            esm.deletePhotos((int) id);
        }
    }

    public void deleteAllGeoCaches() {
        ctx.getContentResolver().delete(GeoCacheProvider.GEO_CACHE_CONTENT_URI, null, null);
        esm.deleteAllPhotos();
    }

    public GeoCacheInfo findGeoCache(int geoCacheId) {
        GeoCacheInfo cacheInfo = new GeoCacheInfo();
        try (Cursor cursor = ctx.getContentResolver().query(ContentUris.withAppendedId(GeoCacheProvider.GEO_CACHE_CONTENT_URI, geoCacheId), null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    int infoIndex = cursor.getColumnIndex(DB.Column.DESCR);
                    cacheInfo.info = new JSONObject(cursor.getString(infoIndex));
                    int commentsIndex = cursor.getColumnIndex(DB.Column.COMMENTS);
                    cacheInfo.comments = new JSONArray(cursor.getString(commentsIndex));
                    int photosIndex = cursor.getColumnIndex(DB.Column.PHOTOS);
                    cacheInfo.webPhotoUrls = new JSONArray(cursor.getString(photosIndex));
                    cacheInfo.filePhotoUrls = esm.getPhotos(geoCacheId);
                } catch (JSONException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                }
            }
        }
        return cacheInfo;
    }
}
