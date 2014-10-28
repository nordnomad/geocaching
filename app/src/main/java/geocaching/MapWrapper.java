package geocaching;

import android.location.Location;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import map.test.myapplication3.app.R;

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
