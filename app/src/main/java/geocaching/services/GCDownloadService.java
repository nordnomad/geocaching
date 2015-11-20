package geocaching.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import geocaching.managers.Network;
import geocaching.managers.Storage;
import map.test.myapplication3.app.R;

import static geocaching.Const.PREFS_KEY_BULK_SAVE_PROGRESS;
import static geocaching.Const.PREFS_NAME;
import static geocaching.Utils.numberToStatus;
import static geocaching.Utils.numberToType;
import static geocaching.services.GCDownloadService.Constants.DOWNLOAD_ERROR;
import static geocaching.services.GCDownloadService.Constants.DOWNLOAD_FINISHED;
import static geocaching.services.GCDownloadService.Constants.DOWNLOAD_STARTED;
import static geocaching.services.GCDownloadService.Constants.KEY_STATUS;
import static geocaching.services.GCDownloadService.Constants.SAVE_ALL_NOTIFICATION_ID;

public class GCDownloadService extends IntentService {

    public GCDownloadService() {
        super(GCDownloadService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREFS_KEY_BULK_SAVE_PROGRESS, true).commit();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.content_save_all)
                        .setContentTitle(getString(R.string.notification_save_all_title))
                        .setContentText(getString(R.string.notification_save_all_content_text))
                        .setProgress(0, 0, true);
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(SAVE_ALL_NOTIFICATION_ID, mBuilder.build());

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(KEY_STATUS, DOWNLOAD_STARTED);
        LocalBroadcastManager.getInstance(GCDownloadService.this).sendBroadcast(localIntent);
        double nLon = intent.getDoubleExtra("nLon", 0);
        double sLon = intent.getDoubleExtra("sLon", 0);
        double nLat = intent.getDoubleExtra("nLat", 0);
        double sLat = intent.getDoubleExtra("sLat", 0);
        List<Integer> excludeIds = intent.getIntegerArrayListExtra("excludedIds");
        Network.with(this).loadGeoCachesOnMap(nLon, sLon, nLat, sLat, excludeIds, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(final JSONArray response) {
                        if (response.length() == 0) {
                            nm.cancel(SAVE_ALL_NOTIFICATION_ID);
                            editor.putBoolean(PREFS_KEY_BULK_SAVE_PROGRESS, false).commit();
                            Intent localIntent = new Intent(GCDownloadService.Constants.BROADCAST_ACTION).putExtra(KEY_STATUS, DOWNLOAD_FINISHED);
                            LocalBroadcastManager.getInstance(GCDownloadService.this).sendBroadcast(localIntent);
                            return;
                        }
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                Storage.with(GCDownloadService.this).bulkSaveGeoCacheFullInfo(fixGeoCacheTypeAndStatus(response));
                                return null;
                            }

                            private JSONArray fixGeoCacheTypeAndStatus(JSONArray geoCaches) {
                                try {
                                    for (int i = 0; i < geoCaches.length(); i++) {
                                        JSONObject geoCache = geoCaches.getJSONObject(i);
                                        int serverType = geoCache.getInt("ct");
                                        geoCache.put("ct", numberToType(serverType).ordinal());
                                        int serverStatus = geoCache.getInt("st");
                                        geoCache.put("st", numberToStatus(serverStatus).ordinal());
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return geoCaches;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                nm.cancel(SAVE_ALL_NOTIFICATION_ID);
                                editor.putBoolean(PREFS_KEY_BULK_SAVE_PROGRESS, false).commit();
                                Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(KEY_STATUS, DOWNLOAD_FINISHED);
                                LocalBroadcastManager.getInstance(GCDownloadService.this).sendBroadcast(localIntent);
                            }
                        }.execute();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        nm.cancel(SAVE_ALL_NOTIFICATION_ID);
                        editor.putBoolean(PREFS_KEY_BULK_SAVE_PROGRESS, false).commit();
                        Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(KEY_STATUS, DOWNLOAD_ERROR);
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
        String KEY_STATUS = "status";
        int DOWNLOAD_STARTED = 0;
        int DOWNLOAD_FINISHED = 1;
        int DOWNLOAD_ERROR = 2;
        int SAVE_ALL_NOTIFICATION_ID = 0;
    }
}
