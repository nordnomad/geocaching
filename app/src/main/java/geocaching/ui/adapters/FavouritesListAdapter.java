package geocaching.ui.adapters;

import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import geocaching.GeoCacheStatus;
import geocaching.GeoCacheType;
import geocaching.db.DB;
import map.test.myapplication3.app.R;

public class FavouritesListAdapter extends CursorAdapter implements LocationListener {

    LayoutInflater inflater;
    // TODO refactor to gapi
    LocationManager locationManager;

    public FavouritesListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        locationManager = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.favourites_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.favouritesGeoCacheText);
        nameView.setText(cursor.getString(cursor.getColumnIndex(DB.Column.NAME)));


        Location lnl = getLastLocation();
        Location loc = new Location("");
        loc.setLatitude(cursor.getDouble(cursor.getColumnIndex(DB.Column.LAT)));
        loc.setLongitude(cursor.getDouble(cursor.getColumnIndex(DB.Column.LON)));

        TextView distanceView = (TextView) view.findViewById(R.id.favouritesGeoCacheDist);
        double distance = lnl != null ? lnl.distanceTo(loc) / 1000.0 : 0;
        distanceView.setText(String.format("%.1f км", distance));
        view.setTag(loc);

        int typeIdx = cursor.getColumnIndex(DB.Column.TYPE);
        cursor.getInt(typeIdx);
        GeoCacheType type = GeoCacheType.values()[cursor.getInt(typeIdx)];
        int statusIdx = cursor.getColumnIndex(DB.Column.STATUS);
        GeoCacheStatus status = GeoCacheStatus.values()[cursor.getInt(statusIdx)];

        ImageView iconView = (ImageView) view.findViewById(R.id.favouritesGeoCacheIcon);
    }

    //TODO refactor
    private Location getLastLocation() {
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && !isNetworkEnabled) {
            return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } else {
            long MIN_TIME_BW_UPDATES = 10;
            float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5;
            if (isNetworkEnabled) {
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    Location result = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (result != null) {
                        return result;
                    }
                }
            }
            if (isGPSEnabled) {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        this.notifyDataSetChanged();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
