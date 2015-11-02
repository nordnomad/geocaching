package geocaching.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import geocaching.GeoCacheInfo;
import map.test.myapplication3.app.R;

public class GeoCacheActivityPagerAdapter extends PagerAdapter {

    Context ctx;
    GeoCacheInfo geoCache;

    public GeoCacheActivityPagerAdapter(Context ctx, GeoCacheInfo geoCache) {
        this.ctx = ctx;
        this.geoCache = geoCache;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return ctx.getString(R.string.gc_activity_tab_information);
            case 1:
                return ctx.getString(R.string.gc_activity_tab_comments);
            case 2:
                return ctx.getString(R.string.gc_activity_tab_photos);
        }
        return ctx.getString(R.string.gc_activity_tab_error);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        switch (position) {
            case 0:
                return instantiateInfoTab(container);
            case 1:
                return instantiateCommentsTab(container);
            case 2:
                return instantiatePhotosTab(container);
        }
        return null;
    }

    private View instantiatePhotosTab(ViewGroup container) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.activity_geo_cache_foto_tab, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.gallery);
        if (geoCache.filePhotoUrls != null && !geoCache.filePhotoUrls.isEmpty()) {
            gridView.setAdapter(new FileImageGridAdapter(ctx, geoCache.filePhotoUrls));
        } else {
            gridView.setAdapter(new WebImageGridAdapter(ctx, new JSONArray()));
            if (geoCache.webPhotoUrls != null) {
                WebImageGridAdapter gridAdapter = new WebImageGridAdapter(ctx, geoCache.webPhotoUrls);
                gridView.setAdapter(gridAdapter);
                gridAdapter.notifyDataSetChanged();
            }
        }
        container.addView(view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO decide open images by own activity or standart one
                // GoTo.imagePagerActivity(ctx, geoCacheId, geoCacheName, urls(response));
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType((Uri) (parent.getAdapter().getItem(position)), "image/*");
                ctx.startActivity(intent);
            }
        });
        return view;
    }

    private View instantiateCommentsTab(ViewGroup container) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.activity_geo_cache_comment_tab, container, false);
        ProgressBar commentsBar = (ProgressBar) view.findViewById(R.id.commentsLoading);
        commentsBar.setVisibility(View.VISIBLE);
        container.addView(view);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.commentsList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        recyclerView.setAdapter(new CommentsTabAdapter(new JSONArray()));
        if (geoCache.comments != null) {
            setDataToCommentsTab(geoCache.comments, recyclerView, commentsBar);
        }
        return view;
    }

    private View instantiateInfoTab(ViewGroup container) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.activity_geo_cache_info_tab, container, false);
        ProgressBar bar = (ProgressBar) view.findViewById(R.id.infoLoading);
        bar.setVisibility(View.VISIBLE);
        container.addView(view);
        if (geoCache.info != null) {
            setDataToInfoTab(geoCache.info, view, bar);
        }
        return view;
    }

    private void setDataToCommentsTab(JSONArray response, RecyclerView recyclerView, ProgressBar commentsBar) {
        RecyclerView.Adapter mAdapter = new CommentsTabAdapter(response);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        commentsBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setDataToInfoTab(JSONObject response, View view, ProgressBar bar) {
        try {
            TextView createDate = (TextView) view.findViewById(R.id.createDate);
            createDate.setText(response.getString("created"));
            TextView updateDate = (TextView) view.findViewById(R.id.updateDate);
            updateDate.setText(response.getString("updated"));
            TextView country = (TextView) view.findViewById(R.id.country);
            country.setText(response.getString("country"));
            TextView city = (TextView) view.findViewById(R.id.city);
            city.setText(response.getString("city"));
            TextView location = (TextView) view.findViewById(R.id.location);
            location.setText(response.getString("coordinates"));
            TextView author = (TextView) view.findViewById(R.id.author);
            author.setText(response.getJSONObject("author").getString("name"));
            TextView descriptionText = (TextView) view.findViewById(R.id.descriptionText);
            descriptionText.setText(response.getString("description"));
            TextView surroundingArea = (TextView) view.findViewById(R.id.surroundingArea);
            surroundingArea.setText(response.getString("surroundingArea"));
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        bar.setVisibility(View.GONE);
        view.findViewById(R.id.infoCard).setVisibility(View.VISIBLE);
        view.findViewById(R.id.description).setVisibility(View.VISIBLE);
        view.findViewById(R.id.area).setVisibility(View.VISIBLE);
    }
}