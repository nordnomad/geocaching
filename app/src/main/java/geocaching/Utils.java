package geocaching;

import android.content.ContentValues;
import android.location.Location;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import geocaching.db.DB;
import map.test.myapplication3.app.R;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_ORANGE;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker;

public class Utils {

    // if distance(m)
    // greater than this
    // show (x/1000) km else x m
    private static final int BIG_DISTANCE_VALUE = 1000; // distance in meters which mean "big distance"

    private static final String SMALL_PRECISE_DISTANCE_NUMBER_FORMAT = "%.0f %s";
    private static final String SMALL_NOT_PRECISE_DISTANCE_NUMBER_FORMAT = "≈%.0f %s";
    private static final String BIG_PRECISE_DISTANCE_NUMBER_FORMAT = "%.1f %s";
    private static final String BIG_NOT_PRECISE_DISTANCE_NUMBER_FORMAT = "≈%.1f %s";

    private static final String BIG_DISTANCE_VALUE_NAME = "км";
    private static final String SMALL_DISTANCE_VALUE_NAME = "м";
    private static final float BIG_DISTANCE_COEFFICIENT = 0.001f; // how many small_distance_name units in big_distance_units
    private static final float SMALL_DISTANCE_COEFFICIENT = 1f;

    public static BitmapDescriptor getMarkerBitmapDescriptor(GeoCacheType type) {
        switch (type) {
            case TRADITIONAL:
                return defaultMarker(100f);
            case STEP_BY_STEP_TRADITIONAL:
                return defaultMarker(HUE_YELLOW);
            case VIRTUAL:
            case WEBCAM:
                return defaultMarker(200f);
            case STEP_BY_STEP_VIRTUAL:
                return defaultMarker(155.f);
            case EVENT:
                return defaultMarker(HUE_AZURE);
            case EXTREME:
                return defaultMarker(HUE_RED);
            case CONTEST:
                return defaultMarker(HUE_ORANGE);
            case GROUP:
                return defaultMarker(315f);

        }
        return defaultMarker(180f);
    }

    public static int getMarkerResId(GeoCacheType type, GeoCacheStatus status) {
        switch (type) {
            case TRADITIONAL:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_traditional_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_traditional_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_traditional_not_confirmed;
                }
                break;

            case STEP_BY_STEP_TRADITIONAL:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_stepbystep_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_stepbystep_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_stepbystep_not_confirmed;
                }
                break;
            case VIRTUAL:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_virtual_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_virtual_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_virtual_not_confirmed;
                }
                break;
            case EVENT:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_event_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_event_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_event_not_confirmed;
                }
                break;
            case WEBCAM:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_virtual_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_virtual_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_virtual_not_confirmed;
                }
                break;
            case EXTREME:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_extreme_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_extreme_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_extreme_not_confirmed;
                }
                break;
            case STEP_BY_STEP_VIRTUAL:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_virtual_stepbystep_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_virtual_stepbystep_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_virtual_stepbystep_not_confirmed;
                }
                break;
            case CONTEST:
                switch (status) {
                    case VALID:
                        return R.drawable.ic_cache_default_competition_valid;
                    case NOT_VALID:
                        return R.drawable.ic_cache_default_competition_not_valid;
                    case NOT_CONFIRMED:
                        return R.drawable.ic_cache_default_competition_not_confirmed;
                }
                break;
            case GROUP:
                return R.drawable.ic_cache_default_group;
        }
        return -1;
    }

    public static GeoCacheType numberToType(int type) {
        switch (type) {
            case 1:
                return GeoCacheType.TRADITIONAL;
            case 2:
                return GeoCacheType.STEP_BY_STEP_TRADITIONAL;
            case 3:
                return GeoCacheType.VIRTUAL;
            case 4:
                return GeoCacheType.EVENT;
            case 5:
                return GeoCacheType.WEBCAM;
            case 6:
                return GeoCacheType.EXTREME;
            case 7:
                return GeoCacheType.STEP_BY_STEP_VIRTUAL;
            case 8:
                return GeoCacheType.CONTEST;
            default:
                return GeoCacheType.TRADITIONAL;
        }
    }

    public static GeoCacheStatus numberToStatus(int number) {
        switch (number) {
            case 1:
                return GeoCacheStatus.VALID;
            case 2:
                return GeoCacheStatus.NOT_VALID;
            case 3:
                return GeoCacheStatus.NOT_CONFIRMED;
            default:
                return GeoCacheStatus.VALID;
        }
    }

    public static String getCharsetFromContentType(String contentType) {
        if (contentType != null) {
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.toLowerCase().startsWith("charset=")) {
                    return param.split("=", 2)[1];
                }
            }
        }
        return "windows-1251";
    }

    public static InputStreamReader getInputSteamReader(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept-Encoding", "gzip;q=1.0, identity;q=0.5, *;q=0");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Can't connect to geocaching.su. Response: " + connection.getResponseCode());
        }
        InputStream inputStream = connection.getInputStream();
        if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
            inputStream = new GZIPInputStream(inputStream);
        }
        String charset = getCharsetFromContentType(connection.getContentType());

        return new InputStreamReader(inputStream, charset);
    }

    public static MarkerOptions markerFromCache(GeoCache geoCache) {
        return new MarkerOptions()
                .position(new LatLng(geoCache.la, geoCache.ln))
                .title(geoCache.name)
                .icon(getMarkerBitmapDescriptor(geoCache.type));
//                .icon(BitmapDescriptorFactory.fromResource(
//                        getMarkerResId(geoCache.type, geoCache.status)));
    }

    public static boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }

    public static ContentValues geoCacheToContentValues(GeoCache geoCache) {
        ContentValues cv = new ContentValues();
        cv.put(DB.Column._ID, geoCache.id);
        cv.put(DB.Column.NAME, geoCache.name);
        cv.put(DB.Column.LAT, geoCache.la);
        cv.put(DB.Column.LON, geoCache.ln);
        cv.put(DB.Column.STATUS, geoCache.status.ordinal());
        cv.put(DB.Column.TYPE, geoCache.type.ordinal());
        return cv;
    }

    public static ContentValues jsonGeoCacheToContentValues(JSONObject geoCache) throws JSONException {
        ContentValues cv = new ContentValues();
        cv.put(DB.Column._ID, geoCache.getString("id"));
        cv.put(DB.Column.NAME, geoCache.getString("n"));
        cv.put(DB.Column.LAT, geoCache.getDouble("la"));
        cv.put(DB.Column.LON, geoCache.getDouble("ln"));
        cv.put(DB.Column.STATUS, geoCache.getInt("st"));
        cv.put(DB.Column.TYPE, geoCache.getInt("ct"));

        cv.put(DB.Column.DESCR, geoCache.getString("info"));
        cv.put(DB.Column.PHOTOS, geoCache.getString("images"));
        cv.put(DB.Column.COMMENTS, geoCache.getString("comments"));
        return cv;
    }

    /**
     * Formatting coordinate in accordance with standard
     *
     * @param location - coordinates
     * @return formating string (for example: "60° 12,123' с.ш. | 30° 32,321'" в.д.)
     */
    public static String coordinateToString(Location location) {
        Sexagesimal latitude = new Sexagesimal(location.getLatitude()).roundTo(3);
        Sexagesimal longitude = new Sexagesimal(location.getLongitude()).roundTo(3);

        String format;
        if (latitude.degrees > 0) {
            if (longitude.degrees > 0) {
                format = "%d° %.3f\\' с.ш. \n%d° %.3f\\' в.д.";
            } else {
                format = "%d° %.3f\\' с.ш. \n%d° %.3f\\' з.д.";
            }
        } else {
            if (longitude.degrees > 0) {
                format = "%d° %.3f\\' ю.ш. \n%d° %.3f\\' в.д.";
            } else {
                format = "%d° %.3f\\' ю.ш. \n%d° %.3f\\' з.д.";
            }
        }
        return String.format(format, latitude.degrees, latitude.minutes, longitude.degrees, longitude.minutes);
    }

    /**
     * @param dist      distance (suggested to geocache in meters)
     * @param isPrecise true, if distance value precise
     * @return String of distance formatted value and measure
     */
    public static String distanceToString(float dist, boolean isPrecise) {
        String textDistance;
        if (isPrecise) {
            if (dist >= BIG_DISTANCE_VALUE) {
                textDistance = String.format(BIG_PRECISE_DISTANCE_NUMBER_FORMAT, dist * BIG_DISTANCE_COEFFICIENT, BIG_DISTANCE_VALUE_NAME);
            } else {
                textDistance = String.format(SMALL_PRECISE_DISTANCE_NUMBER_FORMAT, dist * SMALL_DISTANCE_COEFFICIENT, SMALL_DISTANCE_VALUE_NAME);
            }
        } else {
            if (dist >= BIG_DISTANCE_VALUE) {
                textDistance = String.format(BIG_NOT_PRECISE_DISTANCE_NUMBER_FORMAT, dist * BIG_DISTANCE_COEFFICIENT, BIG_DISTANCE_VALUE_NAME);
            } else {
                textDistance = String.format(SMALL_NOT_PRECISE_DISTANCE_NUMBER_FORMAT, dist * SMALL_DISTANCE_COEFFICIENT, SMALL_DISTANCE_VALUE_NAME);
            }
        }
        return textDistance;
    }
}
