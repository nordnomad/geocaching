package geocaching.ui;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import geocaching.GoTo;
import geocaching.db.GeoCacheProvider;
import geocaching.managers.Storage;
import geocaching.ui.adapters.FavouritesListAdapter;
import geocaching.ui.compass.CompassSensorsFragment;
import map.test.myapplication3.app.R;

public class FavoritesScreen extends CompassSensorsFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView listView;
    LocationManager locationManager; // TODO replace with gApi
    MenuItem removeCacheItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        locationManager = (LocationManager) getActivity().getSystemService(Service.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorites_screen, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.favourites_list_action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
        removeCacheItem = menu.findItem(R.id.favourites_delete);
        removeCacheItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Удалить все")
                        .setMessage("Вы действительно хотите удалить все сохраненные тайники?")

                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Storage.with(getActivity()).deleteAllGeoCaches();
                                removeCacheItem.setVisible(!Storage.with(getActivity()).isFavouriteListEmpty());
                                dialog.dismiss();
                            }
                        }).setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
                return true;
            }
        });
        removeCacheItem.setVisible(!Storage.with(this).isFavouriteListEmpty());
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(new FavouritesListAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER));
        getLoaderManager().initLoader(0, null, this);

        listView = (ListView) getActivity().findViewById(android.R.id.list);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedCount = listView.getCheckedItemCount();
                mode.setTitle(checkedCount + " Selected");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_delete:
                        Storage.with(getActivity()).deleteGeoCaches(getListView().getCheckedItemIds());
                        removeCacheItem.setVisible(!Storage.with(getActivity()).isFavouriteListEmpty());
                        mode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView nameView = (TextView) view.findViewById(R.id.nameView);
                Location loc = (Location) view.getTag();
                GoTo.geoCacheActivity(getActivity(), id, nameView.getText(), loc.getLongitude(), loc.getLatitude());
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), GeoCacheProvider.GEO_CACHE_CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        ((FavouritesListAdapter) getListAdapter()).swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        ((FavouritesListAdapter) getListAdapter()).swapCursor(null);
    }
}
