package geocaching.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;

import java.util.List;

import geocaching.managers.Network;
import geocaching.managers.Storage;

public class GCDownloadService extends IntentService {

    public GCDownloadService() {
        super(GCDownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra("status", 0);
        LocalBroadcastManager.getInstance(GCDownloadService.this).sendBroadcast(localIntent);
        double nLon = intent.getDoubleExtra("nLon", 0);
        double sLon = intent.getDoubleExtra("sLon", 0);
        double nLat = intent.getDoubleExtra("nLat", 0);
        double sLat = intent.getDoubleExtra("sLat", 0);
        List<Integer> excludeIds = intent.getIntegerArrayListExtra("excludeIds");
        Network.with(this).loadGeoCachesOnMap(nLon, sLon, nLat, sLat, excludeIds, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(final JSONArray response) {

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                Storage.with(GCDownloadService.this).bulkSaveGeoCacheFullInfo(response);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                Intent localIntent = new Intent(GCDownloadService.Constants.BROADCAST_ACTION).putExtra("status", 1);
                                LocalBroadcastManager.getInstance(GCDownloadService.this).sendBroadcast(localIntent);
                            }
                        }.execute();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra("status", 2);
                        LocalBroadcastManager.getInstance(GCDownloadService.this).sendBroadcast(localIntent);
                        GCDownloadService.this.stopSelf();
                        String message = TextUtils.isEmpty(error.getMessage()) ? "0 caches was saved" : error.getMessage();
                        Toast.makeText(GCDownloadService.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    public interface Constants {
        String BROADCAST_ACTION = "geocaching.bulkdownload.BROADCAST";
        int DOWNLOAD_STARTED = 0;
        int DOWNLOAD_FINISHED = 1;
        int DOWNLOAD_ERROR = 2;
    }
}
