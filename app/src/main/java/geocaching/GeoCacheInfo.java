package geocaching;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class GeoCacheInfo {
    public JSONObject info;
    public JSONArray comments;
    public JSONArray webPhotoUrls;
    public List<Uri> filePhotoUrls;
}
