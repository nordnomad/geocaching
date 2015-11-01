package geocaching.ui.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;

import map.test.myapplication3.app.R;

import static geocaching.Utils.urls;

public class WebImageGridAdapter extends BaseAdapter {
    LayoutInflater inflater;
    JSONArray imageUrls;

    public WebImageGridAdapter(Activity ctx, JSONArray imageUrls) {
        inflater = LayoutInflater.from(ctx);
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.length();
    }

    @Override
    public Uri getItem(int position) {
        return Uri.parse(urls(imageUrls).get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.item_grid_image, parent, false);
            holder = new ViewHolder();
            assert view != null;
            holder.imageView = (ImageView) view.findViewById(R.id.image);
            holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                //TODO implement images for such situations
//                .showImageOnLoading(R.drawable.ic_launcher)
//                .showImageForEmptyUri(R.drawable.ic_launcher)
//                .showImageOnFail(R.drawable.ic_launcher)
//                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        String imageUrl = "";
        try {
            imageUrl = imageUrls.getJSONObject(position).getString("thumbnails");
        } catch (JSONException e) {
            Log.e(WebImageGridAdapter.class.getName(), e.getMessage(), e);
        }

        ImageLoader.getInstance().displayImage(imageUrl, holder.imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                holder.progressBar.setProgress(0);
                holder.progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                holder.progressBar.setVisibility(View.GONE);
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                holder.progressBar.setProgress(Math.round(100.0f * current / total));
            }
        });

        return view;
    }

    static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }
}
