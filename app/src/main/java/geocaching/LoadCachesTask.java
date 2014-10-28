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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static geocaching.Utils.*;

public class LoadCachesTask extends AsyncTask<LatLngBounds, Void, Void> {
    List<GeoCache> caches = new ArrayList<GeoCache>();

    GoogleMap map;

    public LoadCachesTask(GoogleMap map) {
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
        for (GeoCache cach : caches) {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(cach.la, cach.ln))
                    .title(cach.name)
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