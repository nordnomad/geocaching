package geocaching.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import static geocaching.Utils.isBlank;

public class GeoCacheProvider extends ContentProvider {

    DB db;

    static final String AUTHORITY = "com.geocaching.myown";
    static final int URI_CACHES = 1;
    static final int URI_CACHES_ID = 2;

    public static final Uri GEO_CACHE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB.Table.GEO_CACHE);

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, DB.Table.GEO_CACHE, URI_CACHES);
        matcher.addURI(AUTHORITY, DB.Table.GEO_CACHE + "/#", URI_CACHES_ID);
        return matcher;
    }

    // Типы данных
    // набор строк
    static final String CONTACT_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DB.Table.GEO_CACHE;
    // одна строка
    static final String CONTACT_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DB.Table.GEO_CACHE;

    @Override
    public boolean onCreate() {
        db = new DB(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_CACHES:
                if (isBlank(sortOrder)) {
                    sortOrder = DB.Column.NAME + " ASC";
                }
                break;
            case URI_CACHES_ID:
                String id = uri.getLastPathSegment();
                if (isBlank(selection)) {
                    selection = DB.Column._ID + " = " + id;
                } else {
                    selection = selection + " AND " + DB.Column._ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        Cursor cursor = db.getReadableDatabase().query(DB.Table.GEO_CACHE, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), GEO_CACHE_CONTENT_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_CACHES:
                return CONTACT_CONTENT_TYPE;
            case URI_CACHES_ID:
                return CONTACT_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != URI_CACHES) throw new IllegalArgumentException("Wrong URI: " + uri);
        long rowID = db.getWritableDatabase().insert(DB.Table.GEO_CACHE, null, values);
        Uri resultUri = ContentUris.withAppendedId(GEO_CACHE_CONTENT_URI, rowID);
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_CACHES:
                break;
            case URI_CACHES_ID:
                String id = uri.getLastPathSegment();
                if (isBlank(selection)) {
                    selection = DB.Column._ID + " = " + id;
                } else {
                    selection = selection + " AND " + DB.Column._ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = db.getWritableDatabase().delete(DB.Table.GEO_CACHE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_CACHES:
                break;
            case URI_CACHES_ID:
                String id = uri.getLastPathSegment();
                if (isBlank(selection)) {
                    selection = DB.Column.ID + " = " + id;
                } else {
                    selection = selection + " AND " + DB.Column.ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        int cnt = db.getWritableDatabase().update(DB.Table.GEO_CACHE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }
}
