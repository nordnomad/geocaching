package geocaching.managers;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_INSIDE;
import static com.android.volley.Request.Method.GET;
import static geocaching.Const.M.fullGeoCacheUrl;
import static geocaching.Const.M.fullInfoUrl;
import static geocaching.Utils.urls;

public class Network {

    private static Network instance;

    RequestQueue queue;
    Context ctx;
    DefaultRetryPolicy policy;

    private Network(Context ctx) {
        this.ctx = ctx;
        queue = Volley.newRequestQueue(ctx);
        policy = new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public static synchronized Network with(Context ctx) {
        if (instance == null) {
            instance = new Network(ctx);
        }
        return instance;
    }

    public void loadGeoCachesOnMap(double nLon, double sLon, double nLat, double sLat, List<Long> excludeIds, Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        queue.add(new JsonArrayRequest(fullInfoUrl(nLon, sLon, nLat, sLat, excludeIds), responseListener, errorListener).setRetryPolicy(policy));
    }

    public void loadGeoCacheFullInfo(int geoCacheId, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        queue.add(new JsonObjectRequest(GET, fullGeoCacheUrl(geoCacheId), responseListener, errorListener).setRetryPolicy(policy));
    }

    public void savePhotos(JSONArray photosArray, int geoCacheId) {
        final List<String> urls = urls(photosArray);
        for (String url : urls) {
            queue.add(new ImageRequest(url, new BitmapResponseListener(ctx, url, geoCacheId), 0, 0, CENTER_INSIDE, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    //TODO implement error handling
                    Log.e(getClass().getName(), e.getMessage(), e);
                }
            }));
        }
    }
}
