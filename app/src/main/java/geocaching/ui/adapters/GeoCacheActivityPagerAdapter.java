package geocaching.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import geocaching.ui.GeoCacheActivity;
import map.test.myapplication3.app.R;

import static com.android.volley.Request.Method.GET;
import static geocaching.Const.M.commentsUrl;
import static geocaching.Const.M.imagesUrl;
import static geocaching.Const.M.infoUrl;

public class GeoCacheActivityPagerAdapter extends PagerAdapter {

    private final GeoCacheActivity ctx;

    public GeoCacheActivityPagerAdapter(GeoCacheActivity ctx) {
        this.ctx = ctx;
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
                return "Information";
            case 1:
                return "Comments";
            case 2:
                return "Photos";
        }
        return "Error";
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final View view;
        switch (position) {
            case 0:
                view = ctx.getLayoutInflater().inflate(R.layout.activity_geo_cache_info_tab, container, false);
                final ProgressBar bar = (ProgressBar) view.findViewById(R.id.infoLoading);
                bar.setVisibility(View.VISIBLE);
                container.addView(view);
                if (ctx.infoObject == null) {
                    JsonObjectRequest request = new JsonObjectRequest(GET, infoUrl(ctx.geoCache.id), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            ctx.infoObject = response;
                            setDataToInfoTab(response, view, bar);
                        }
                    }, ctx);
                    request.setRetryPolicy(ctx.retryPolicy);
                    ctx.queue.add(request);
                } else {
                    setDataToInfoTab(ctx.infoObject, view, bar);
                }
                return view;
            case 1:
                view = ctx.getLayoutInflater().inflate(R.layout.activity_geo_cache_comment_tab, container, false);
                final ProgressBar commentsBar = (ProgressBar) view.findViewById(R.id.commentsLoading);
                commentsBar.setVisibility(View.VISIBLE);
                final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.commentsList);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
                recyclerView.setAdapter(new CommentsTabAdapter(new JSONArray()));
                container.addView(view);
                if (ctx.commentsArray == null) {
                    JsonArrayRequest request = new JsonArrayRequest(commentsUrl(ctx.geoCache.id), new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            ctx.commentsArray = response;
                            setDataToCommentsTab(response, recyclerView, commentsBar);
                        }
                    }, ctx);
                    request.setRetryPolicy(ctx.retryPolicy);
                    ctx.queue.add(request);
                } else {
                    setDataToCommentsTab(ctx.commentsArray, recyclerView, commentsBar);
                }
                return view;
            case 2:
                view = ctx.getLayoutInflater().inflate(R.layout.activity_geo_cache_foto_tab, container, false);
                final GridView gridView = (GridView) view.findViewById(R.id.gallery);
                if (ctx.photos != null && !ctx.photos.isEmpty()) {
                    gridView.setAdapter(new FileImageGridAdapter(ctx, ctx.photos));
                    container.addView(view);
                } else {
                    gridView.setAdapter(new WebImageGridAdapter(ctx, new JSONArray()));
                    container.addView(view);
                    if (ctx.photosArray == null) {
                        JsonArrayRequest request = new JsonArrayRequest(imagesUrl(ctx.geoCache.id), new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(final JSONArray response) {
                                ctx.photosArray = response;
                                setDataToPhotoTab(response, ctx, gridView);
                            }
                        }, ctx);
                        request.setRetryPolicy(ctx.retryPolicy);
                        ctx.queue.add(request);
                    } else {
                        setDataToPhotoTab(ctx.photosArray, ctx, gridView);
                    }
                }
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
        return null;
    }

    private void setDataToPhotoTab(final JSONArray response, final GeoCacheActivity ctx, GridView gridView) {
        WebImageGridAdapter gridAdapter = new WebImageGridAdapter(ctx, response);
        gridView.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();

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
            e.printStackTrace();
        }
        bar.setVisibility(View.GONE);
        view.findViewById(R.id.infoCard).setVisibility(View.VISIBLE);
        view.findViewById(R.id.description).setVisibility(View.VISIBLE);
        view.findViewById(R.id.area).setVisibility(View.VISIBLE);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}