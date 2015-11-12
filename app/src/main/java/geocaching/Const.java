package geocaching;

import java.util.List;

import static android.text.TextUtils.join;

public interface Const {
    String PDA_GEOCACHING = "http://pda.geocaching.su";
    String LOGIN_URL = PDA_GEOCACHING + "/login.php";
    String INFO_URL = PDA_GEOCACHING + "/cache.php?cid=%d&mode=0";
    String COMMENTS_URL = PDA_GEOCACHING + "/note.php?cid=%d&mode=0";
    String PHOTO_URL = PDA_GEOCACHING + "/pict.php?cid=%d&mode=0";

    String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.101 Safari/537.36";
    String EMAIL_PARAM = "email";
    String PASSWORD_PARAM = "passwd";
    String LONGTERM_PARAM = "longterm";
    String LONGTERM_PARAM_VAL = "1";
    String LOGIN_PARAM = "LogIn";
    String LOGIN_PARAM_VAL = "";
    String ACCOUNT_TYPE = "com.geocaching";

    String caching_auth_lt = "caching_auth_lt";
    String caching_auth_st = "caching_auth_st";
    String geocaching = "geocaching";

    class M {
        static String ROOT = "http://nordnomad.herokuapp.com/v1";
        static String info = ROOT + "/info/%d";
        static String comments = ROOT + "/comments/%d";
        static String images = ROOT + "/images/%d";
        static String fullGeoCache = ROOT + "/fullGeoCache/%d";
        static String fullInfo = ROOT + "/fullInfo/%f_%f_%f_%f/%s";

        public static String infoUrl(long id) {
            return String.format(info, id);
        }

        public static String commentsUrl(long id) {
            return String.format(comments, id);
        }

        public static String imagesUrl(long id) {
            return String.format(images, id);
        }

        public static String fullGeoCacheUrl(long id) {
            return String.format(fullGeoCache, id);
        }

        public static String fullInfoUrl(double nLon, double sLon, double nLat, double shLat, List<? extends Number> excludeIds) {
            if(excludeIds == null || excludeIds.isEmpty()) {
                return String.format(fullInfo, nLon, sLon, nLat, shLat, "0").replace(",", ".");
            } else {
                return String.format(fullInfo, nLon, sLon, nLat, shLat, join("_", excludeIds)).replace(",", ".");
            }
        }
    }

}
