package geocaching.db;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class DBUtil {
    public static boolean isFavouriteListEmpty(Context ctx) {
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

    public static boolean isGeoCacheInFavouriteList(Context ctx, int geoCacheId) {
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

}
