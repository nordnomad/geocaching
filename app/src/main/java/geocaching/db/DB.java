package geocaching.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static geocaching.db.DB.Column.COMMENTS;
import static geocaching.db.DB.Column.DESCR;
import static geocaching.db.DB.Column.ID;
import static geocaching.db.DB.Column.LAT;
import static geocaching.db.DB.Column.LON;
import static geocaching.db.DB.Column.NAME;
import static geocaching.db.DB.Column.PHOTOS;
import static geocaching.db.DB.Column.STATUS;
import static geocaching.db.DB.Column.TYPE;
import static geocaching.db.DB.Column.USER_NOTES;
import static geocaching.db.DB.Column._ID;

public class DB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME_BASE = "MyGeocaching.db";

    private static final int DATABASE_VERSION = 7;

    interface Table {
        String GEO_CACHE = "geo_cache";
    }

    public interface Column {
        String _ID = "_id";
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

    private static final String SQL_DROP_DATABASE_TABLE = String.format("DROP TABLE IF EXISTS %s", Table.GEO_CACHE);

    private static final String SQL_CREATE_DATABASE_TABLE = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY, %s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s STRING, %s STRING, %s STRING, %s STRING);",
            Table.GEO_CACHE, _ID, ID, NAME, TYPE, STATUS, LAT, LON, DESCR, COMMENTS, USER_NOTES, PHOTOS);

    public DB(Context context) {
        super(context, DATABASE_NAME_BASE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_CREATE_DATABASE_TABLE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(DB.class.getName(), e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            db.execSQL(SQL_DROP_DATABASE_TABLE);
            db.execSQL(SQL_CREATE_DATABASE_TABLE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(DB.class.getName(), e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
    }
}
