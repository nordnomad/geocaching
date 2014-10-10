package geocaching;

public class GeoPoint {

    public double latitude;

    public double longitude;

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format("%s, %s", latitude, longitude);
    }
}