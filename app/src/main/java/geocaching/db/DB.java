package geocaching.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import geocaching.GeoCache;

import java.net.URL;
import java.util.Collection;

import static geocaching.db.DB.Column.*;

public class DB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME_BASE = "MyGeocaching.db";

    private static final int DATABASE_VERSION = 5;
    private static final String PHOTO_URL_DEVIDER = "; ";

    interface Table {
        String GEO_CACHE = "geo_cache";
    }

    interface Column {
        String ID = "cid";
        String NAME = "name";
        String TYPE = "type";
        String STATUS = "status";
        String LON = "longtitude";
        String LAT = "lantitude";
        String DESCR = "text";
        String COMMENTS = "notetext";
        String USER_NOTES = "user_notes";
        String PHOTOS = "photos";
    }

    private SQLiteDatabase db;

    private static final String SQL_CREATE_DATABASE_TABLE = String.format(
            "CREATE TABLE %s (%s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s STRING, %s STRING, %s STRING, %s STRING);",
            Table.GEO_CACHE, ID, NAME, TYPE, STATUS, LAT, LON, DESCR, COMMENTS, USER_NOTES, PHOTOS);

    public DB(Context context) {
        super(context, DATABASE_NAME_BASE, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_CREATE_DATABASE_TABLE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addGeoCache(GeoCache geoCache, String cacheDescription, String cacheComments, Collection<URL> photos) {
        ContentValues values = new ContentValues();
        values.put(ID, geoCache.id);
        values.put(NAME, geoCache.name);
        values.put(STATUS, geoCache.status.ordinal());
        values.put(TYPE, geoCache.type.ordinal());
        values.put(LAT, geoCache.la);
        values.put(LON, geoCache.ln);
        values.put(DESCR, cacheDescription);
        if (photos != null) {
            values.put(PHOTOS, TextUtils.join(PHOTO_URL_DEVIDER, photos));
        }
        if (cacheComments != null) {
            values.put(COMMENTS, cacheComments);
        }
        db.insert(Table.GEO_CACHE, null, values);
    }
}
