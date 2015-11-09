package geocaching;

import android.location.Location;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Class for parsing data from geocaching.su and put it in the List of GeoCache. Parse XML file is as follows:
 * <p/>
 * <pre>
 *         {@code
 *         <c>
 *             <id>8901</id>
 *             <cn>10</cn>
 *             <a>47</a>
 *             <n>Geocache name</n>
 *             <la>59.6952333333</la>
 *             <ln>29.3968666667</ln>
 *             <ct>3</ct>
 *             <st>1</st>
 *         </c>
 *         }
 * </pre>
 */

public class GeoCache implements Parcelable {
    public int id;
    public int cn;
    public String name;
    public double la;
    public double ln;
    public GeoCacheType type;
    public GeoCacheStatus status;

    public LatLng latLng() {
        return new LatLng(la, ln);
    }

    public Location location() {
        Location location = new Location("");
        location.setLongitude(ln);
        location.setLatitude(la);
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoCache geoCache = (GeoCache) o;

        return id == geoCache.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle src = new Bundle();
        src.putInt("id", this.id);
        src.putString("name", this.name);
        src.putDouble("la", this.la);
        src.putDouble("ln", this.ln);
        src.putInt("status", this.status.ordinal());
        src.putInt("type", this.type.ordinal());
        dest.writeBundle(src);
    }

    public GeoCache() {
    }

    public GeoCache(Parcel in) {
        Bundle data = in.readBundle();
        this.id = data.getInt("id");
        this.name = data.getString("name");
        this.la = data.getDouble("la");
        this.ln = data.getDouble("ln");
        this.status = GeoCacheStatus.values()[data.getInt("status")];
        this.type = GeoCacheType.values()[data.getInt("type")];
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GeoCache createFromParcel(Parcel in) {
            return new GeoCache(in);
        }

        public GeoCache[] newArray(int size) {
            return new GeoCache[size];
        }
    };
}
