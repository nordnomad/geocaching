package geocaching;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

public class MapWrapper {
    public GoogleMap map;
    public Map<GeoCache, Marker> markerGeoCaches = new HashMap<>();

    public MapWrapper(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setMyLocationEnabled(true);
    }

    public Map<Marker, GeoCache> inverseMap() {
        Map<Marker, GeoCache> inversed = new HashMap<>();
        for (Map.Entry<GeoCache, Marker> entry : markerGeoCaches.entrySet()) {
            inversed.put(entry.getValue(), entry.getKey());
        }
        return inversed;
    }
}
