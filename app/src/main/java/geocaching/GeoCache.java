package geocaching;

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

public class GeoCache {
    public int id;
    public int cn;
    public String name;
    public double la;
    public double ln;
    public GeoCacheType type;
    public GeoCacheStatus status;

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
}
