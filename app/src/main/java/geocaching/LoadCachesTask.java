package geocaching;

import android.os.AsyncTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static geocaching.Utils.getMarkerResId;
import static geocaching.Utils.numberToStatus;
import static geocaching.Utils.numberToType;

public class LoadCachesTask extends AsyncTask<LatLngBounds, Void, Void> {
    List<GeoCache> caches = new ArrayList<GeoCache>();

    GoogleMap map;

    public LoadCachesTask(GoogleMap map) {
        this.map = map;
    }

    @Override
    protected Void doInBackground(LatLngBounds... bounds1) {
//        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        LatLngBounds bounds = bounds1[0];
        double northLat = bounds.northeast.latitude;
        double northLong = bounds.northeast.longitude;
        double southLat = bounds.southwest.latitude;
        double southLong = bounds.southwest.longitude;

        String url = "http://www.geocaching.su/pages/1031.ajax.php?lngmax=" + northLong + "&lngmin=" + southLong + "&latmax=" + northLat + "&latmin=" + southLat + "&id=12345678&geocaching=5767e405a17c4b0e1cbaecffdb93475d&exactly=1";
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            InputSource geoCacheXml = new InputSource(getInputSteamReader(new URL(url)));
            GeoCachesHandler cachesHandler = new GeoCachesHandler();
            parser.parse(geoCacheXml, cachesHandler);
            caches = cachesHandler.geoCaches;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStreamReader getInputSteamReader(String url) throws IOException {
        return getInputSteamReader(new URL(url));
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

    private static String getCharsetFromContentType(String contentType) {
        if (contentType != null) {
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.toLowerCase().startsWith("charset=")) {
                    return param.split("=", 2)[1];
                }
            }
        }
        return "windows-1251";
    }

    @Override
    protected void onPostExecute(Void o) {
        if(!caches.isEmpty())
        for (GeoCache cach : caches) {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(cach.getLa(), cach.getLn()))
                    .title(cach.getN())
                    .icon(BitmapDescriptorFactory.fromResource(
                            getMarkerResId(cach.type, cach.status))));
        }
    }
}

class GeoCachesHandler extends DefaultHandler {

    public List<GeoCache> geoCaches = new ArrayList<GeoCache>();
    String C = "c";
    String ID = "id";
    String CN = "cn";
    String AREA = "a";
    String NAME = "n";
    String LATITUDE = "la";
    String LONGITUDE = "ln";
    String CACHE_TYPE = "ct";
    String STATUS = "st";
    StringBuilder content = new StringBuilder();
    private GeoCache geoCache;
    private double latitude, longitude;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        content.append(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        content = new StringBuilder();
        if (localName.equalsIgnoreCase(C)) {
            geoCache = new GeoCache();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String text = content.toString();
        if (localName.equalsIgnoreCase(ID)) {
            geoCache.id = parseInt(text, 0);
        } else if (localName.equalsIgnoreCase(NAME)) {
            geoCache.setN(text);
        } else if (localName.equalsIgnoreCase(LATITUDE)) {
            latitude = parseCoordinate(text);
        } else if (localName.equalsIgnoreCase(LONGITUDE)) {
            longitude = parseCoordinate(text);
        } else if (localName.equalsIgnoreCase(CACHE_TYPE)) {
            geoCache.type = numberToType(parseCacheParameter(text));
        } else if (localName.equalsIgnoreCase(STATUS)) {
           geoCache.status = numberToStatus(parseCacheParameter(text));
        } else if (localName.equalsIgnoreCase(C)) {
//            geoCache.geoPoint = new GeoPoint(latitude, longitude);
            geoCache.setLa(latitude);
            geoCache.setLn(longitude);
            geoCaches.add(geoCache);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    double parseCoordinate(String coordinate) {
        double result = 0;
        try {
            result = Double.parseDouble(coordinate);
        } catch (NumberFormatException e) {
        }
        return result;
    }

    int parseCacheParameter(String parameter) {
        return parseInt(parameter, 1);
    }

    int parseInt(String number, int defaultValue) {
        try {
            defaultValue = Integer.parseInt(number);
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

}