package geocaching;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class GeoCacheInfo {
    public JSONObject info;
    public JSONArray comments;
    public JSONArray webPhotoUrls;
    public List<Uri> filePhotoUrls;

    public GeoCacheInfo() {
    }

    public GeoCacheInfo(JSONObject json) throws JSONException {
        info = json.getJSONObject("info");
        webPhotoUrls = json.getJSONArray("images");
        comments = json.getJSONArray("comments");
    }

    public boolean isEmpty() {
        return info == null || comments == null || webPhotoUrls == null;
    }
}
