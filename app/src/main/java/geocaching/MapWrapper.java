package geocaching;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

public class MapWrapper {
    public GoogleMap map;
    public Map<GeoCache, Marker> markerGeoCaches = new HashMap<GeoCache, Marker>();

    public MapWrapper(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setMyLocationEnabled(true);
    }
}
