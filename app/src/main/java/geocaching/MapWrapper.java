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
    GoogleMap map;
    LocationClient locationClient;
    Map<Marker, GeoCache> markerMap = new HashMap<Marker, GeoCache>();
    Marker selectedMarker;

    public MapWrapper(GoogleMap googleMap) {
        this.map = googleMap;
        this.map.setMyLocationEnabled(true);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_48dp));
                Location location = new Location("Test");
                location.setLatitude(marker.getPosition().latitude);
                location.setLongitude(marker.getPosition().longitude);

                return true; // to prevent center marker on screen
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });
    }
}
