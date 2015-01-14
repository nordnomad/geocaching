package geocaching.tasks;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import geocaching.GeoCache;
import geocaching.MapWrapper;

import static geocaching.Utils.getInputSteamReader;
import static geocaching.Utils.markerFromCache;
import static geocaching.Utils.numberToStatus;
import static geocaching.Utils.numberToType;

public class LoadCachesTask extends AsyncTask<LatLngBounds, Void, Void> {
    List<GeoCache> caches = new ArrayList<GeoCache>();

    MapWrapper map;

    public LoadCachesTask(MapWrapper map) {
        this.map = map;
    }

    @Override
    protected Void doInBackground(LatLngBounds... bounds1) {
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

    @Override
    protected void onPostExecute(Void o) {
        for (GeoCache cache : caches) {
            if (!map.markerGeoCaches.containsKey(cache)) {
                Marker marker = map.map.addMarker(markerFromCache(cache));
                map.markerGeoCaches.put(cache, marker);
            }
        }
        Iterator<GeoCache> iterator = map.markerGeoCaches.keySet().iterator();
        while (iterator.hasNext()) {
            GeoCache cache = iterator.next();
            if (!caches.contains(cache)) {
                Marker marker = map.markerGeoCaches.get(cache);
                marker.remove();
                iterator.remove();
            }
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
            geoCache.name = text;
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
            geoCache.la = latitude;
            geoCache.ln = longitude;
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