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
    int id;
    int cn;
    String n;//name
    double la;
    double ln;
    int ct; // type
    int st; // status

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCn() {
        return cn;
    }

    public void setCn(int cn) {
        this.cn = cn;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public double getLa() {
        return la;
    }

    public void setLa(double la) {
        this.la = la;
    }

    public double getLn() {
        return ln;
    }

    public void setLn(double ln) {
        this.ln = ln;
    }

    public int getCt() {
        return ct;
    }

    public void setCt(int ct) {
        this.ct = ct;
    }

    public int getSt() {
        return st;
    }

    public void setSt(int st) {
        this.st = st;
    }

    @Override
    public String toString() {
        return "GeoCacheDto{" +
                "n='" + n + '\'' +
                '}';
    }
}
