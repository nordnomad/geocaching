package map.test.myapplication3.app;

import android.os.Parcel;
import android.os.Parcelable;

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

    public int id; // Unique identifier of GeoCache(from geocaching.su)
    public GeoPoint geoPoint = new GeoPoint(0, 0);
    public String name;
    public GeoCacheType type = GeoCacheType.TRADITIONAL;
    public GeoCacheStatus status = GeoCacheStatus.VALID;
    /**
     * Standard implementation of static final class which need for creating GeoCache object from parcel
     */
    public static final Creator<GeoCache> CREATOR = new Creator<GeoCache>() {

        public GeoCache createFromParcel(Parcel in) {
            // ! Important reading order from parcel
            GeoCache res = new GeoCache();
            res.id = in.readInt();
            res.name = in.readString();
            res.geoPoint = new GeoPoint(in.readDouble(), in.readDouble());
            res.type = GeoCacheType.values()[in.readInt()];
            res.status = GeoCacheStatus.values()[in.readInt()];
            return res;
        }

        public GeoCache[] newArray(int size) {
            return new GeoCache[size];
        }
    };

    @Override
    public String toString() {
        return "GeoCache{" + name + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        // ! Important order of writing to parcel
        arg0.writeInt(id);
        arg0.writeString(name);
        arg0.writeDouble(geoPoint.latitude);
        arg0.writeDouble(geoPoint.longitude);
        arg0.writeInt(type.ordinal());
        arg0.writeInt(status.ordinal());
    }

    @Override
    public boolean equals(Object cache) {
        if (this == cache) {
            return true;
        }
        if (!(cache instanceof GeoCache)) {
            return false;
        }
        GeoCache gc = (GeoCache) cache;
        return id == gc.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}