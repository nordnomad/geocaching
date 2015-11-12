package geocaching.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapResponseListener implements Response.Listener<Bitmap> {
    private String imageUrl;
    private int geoCacheId;
    Context ctx;

    public BitmapResponseListener(Context ctx, String imageUrl, int geoCacheId) {
        this.imageUrl = imageUrl;
        this.geoCacheId = geoCacheId;
        this.ctx = ctx;
    }

    @Override
    public void onResponse(final Bitmap response) {
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/"));
                File file = SDCard.with(ctx).getPhotoFile(fileName, geoCacheId);
                FileOutputStream fOut = null;
                try {
                    fOut = new FileOutputStream(file);
                    response.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                } catch (FileNotFoundException e) {
                    Log.e(getClass().getName(), e.getMessage(), e);
                } finally {
                    if (fOut != null) {
                        try {
                            fOut.flush();
                            fOut.close();
                        } catch (IOException e) {
                            Log.e(getClass().getName(), e.getMessage(), e);
                        }
                    }
                }
                return null;
            }
        }.execute();
    }
}
