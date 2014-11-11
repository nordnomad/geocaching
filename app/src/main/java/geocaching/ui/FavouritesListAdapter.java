package geocaching.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import geocaching.GeoCacheStatus;
import geocaching.GeoCacheType;
import geocaching.Utils;
import geocaching.db.DB;
import map.test.myapplication3.app.R;

public class FavouritesListAdapter extends CursorAdapter {

    LayoutInflater inflater;

    public FavouritesListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.favourites_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.favouritesGeoCacheText);
        nameView.setText(cursor.getString(cursor.getColumnIndex(DB.Column.NAME)));

        int typeIdx = cursor.getColumnIndex(DB.Column.TYPE);
        cursor.getInt(typeIdx);
        GeoCacheType type = GeoCacheType.values()[cursor.getInt(typeIdx)];
        int statusIdx = cursor.getColumnIndex(DB.Column.STATUS);
        GeoCacheStatus status = GeoCacheStatus.values()[cursor.getInt(statusIdx)];

        ImageView iconView = (ImageView) view.findViewById(R.id.favouritesGeoCacheIcon);
        iconView.setImageResource(Utils.getMarkerResId(type, status));
    }
}
