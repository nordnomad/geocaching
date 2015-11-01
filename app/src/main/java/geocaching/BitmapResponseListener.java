package geocaching;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.android.volley.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapResponseListener implements Response.Listener<Bitmap> {
    private String imageUrl;
    private ExternalStorageManager esm;
    private int geoCacheId;

    public BitmapResponseListener(Context ctx, String imageUrl, int geoCacheId) {
        esm = new ExternalStorageManager(ctx);
        this.imageUrl = imageUrl;
        this.geoCacheId = geoCacheId;
    }

    @Override
    public void onResponse(Bitmap response) {
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/"));
        File file = esm.getPhotoFile(fileName, geoCacheId);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            response.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        } catch (FileNotFoundException e) {
            Log.e(BitmapResponseListener.class.getName(), e.getMessage(), e);
        } finally {
            if (fOut != null) {
                try {
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    Log.e(BitmapResponseListener.class.getName(), e.getMessage(), e);
                }
            }
        }
    }
}
