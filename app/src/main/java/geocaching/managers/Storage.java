package geocaching.managers;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import geocaching.GeoCacheInfo;
import geocaching.db.DB;
import geocaching.db.GeoCacheProvider;

import static geocaching.Utils.jsonGeoCacheToContentValues;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class Storage {

    private static Storage storage;

    Context ctx;

    private Storage(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    public static synchronized Storage with(Context ctx) {
        if (storage == null) {
            storage = new Storage(ctx);
        }
        return storage;
    }

    public static synchronized Storage with(Fragment ctx) {
        if (storage == null) {
            storage = new Storage(ctx.getActivity());
        }
        return storage;
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

    public List<Integer> idsOfStoredGeoCachesInt() {
        List<Long> longs = idsOfStoredGeoCaches();
        List<Integer> result = new ArrayList<>();
        for (Long id: longs) {
            result.add(id.intValue());
        }
        return result;
    }

    public void saveGeoCacheFullInfo(JSONObject geoCache) {
        try {
            ctx.getContentResolver().insert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, jsonGeoCacheToContentValues(geoCache));
            Network.with(ctx).savePhotos(geoCache.getJSONArray("images"), geoCache.getInt("id"));
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
    }

    public void bulkSaveGeoCacheFullInfo(JSONArray geoCaches) {
        List<ContentValues> contentValues = new ArrayList<>();
        try {
            for (int i = 0; i < geoCaches.length(); i++) {
                JSONObject geoCache = geoCaches.getJSONObject(i);
                contentValues.add(jsonGeoCacheToContentValues(geoCache));
                Network.with(ctx).savePhotos(geoCache.getJSONArray("images"), geoCache.getInt("id"));
            }
        } catch (JSONException e) {
            Log.e("TAG", e.getMessage(), e);
        }
        ctx.getContentResolver().bulkInsert(GeoCacheProvider.GEO_CACHE_CONTENT_URI, contentValues.toArray(new ContentValues[geoCaches.length()]));
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
        SDCard.with(ctx).deletePhotos(geoCacheId);
    }

    public void deleteGeoCaches(long[] ids) {
        String where = "_id IN " + Arrays.toString(ids).replace("[", "(").replace("]", ")");
        ctx.getContentResolver().delete(GeoCacheProvider.GEO_CACHE_CONTENT_URI, where, null);
        for (long id : ids) {
            SDCard.with(ctx).deletePhotos((int) id);
        }
    }

    public void deleteAllGeoCaches() {
        ctx.getContentResolver().delete(GeoCacheProvider.GEO_CACHE_CONTENT_URI, null, null);
        SDCard.with(ctx).deleteAllPhotos();
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
                    cacheInfo.filePhotoUrls = SDCard.with(ctx).getPhotos(geoCacheId);
                } catch (JSONException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                }
            }
        }
        return cacheInfo;
    }
}
