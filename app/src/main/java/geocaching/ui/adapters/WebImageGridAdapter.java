package geocaching.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;

import geocaching.managers.ImageManager;
import map.test.myapplication3.app.R;

import static geocaching.Utils.urls;

public class WebImageGridAdapter extends BaseAdapter {
    LayoutInflater inflater;
    Context ctx;
    JSONArray imageUrls;

    public WebImageGridAdapter(Context ctx, JSONArray imageUrls) {
        this.ctx = ctx;
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
        ViewHolder holder;
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
        String imageUrl = "";
        try {
            imageUrl = imageUrls.getJSONObject(position).getString("thumbnails");
        } catch (JSONException e) {
            Log.e(WebImageGridAdapter.class.getName(), e.getMessage(), e);
        }
        ImageManager.with(ctx).displayImage(imageUrl, holder.imageView, holder.progressBar);
        return view;
    }

    static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }
}
