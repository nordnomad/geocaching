package geocaching.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.List;

import map.test.myapplication3.app.R;

public class FileImageGridAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<Uri> photos;

    public FileImageGridAdapter(Context ctx, List<Uri> photos) {
        inflater = LayoutInflater.from(ctx);
        this.photos = photos;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Uri getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            view = inflater.inflate(R.layout.item_grid_image, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.image);
            holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.imageView.setImageURI(photos.get(position));
        holder.progressBar.setVisibility(View.GONE);
        return view;
    }

    static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }
}
