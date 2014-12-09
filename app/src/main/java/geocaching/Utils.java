package geocaching;

import android.content.ContentValues;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
    public static BitmapDescriptor getMarkerBitmapDescriptor(GeoCacheType type, GeoCacheStatus status) {
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
                .icon(getMarkerBitmapDescriptor(geoCache.type, geoCache.status));
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
}
