package geocaching.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.json.JSONArray;

import geocaching.common.SlidingTabLayout;
import geocaching.tasks.LoadCommentsTask;
import geocaching.tasks.LoadInfoTask;
import geocaching.tasks.LoadPhotoUrlsTask;
import geocaching.ui.adapters.CommentsTabAdapter;
import geocaching.ui.adapters.ImageGridAdapter;
import map.test.myapplication3.app.R;

public class GeoCacheActivity extends Activity {

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_cache);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new GeoCachePagerAdapter());

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        ImageLoader.getInstance().init(config);
    }

    class GeoCachePagerAdapter extends PagerAdapter {

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
                    return "Информация";
                case 1:
                    return "Коментарии";
                case 2:
                    return "Фотографии";
            }
            return "Ошибка";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view;
            long geoCacheId = getIntent().getLongExtra("geoCacheId", 0);
            switch (position) {
                case 0:
                    view = GeoCacheActivity.this.getLayoutInflater().inflate(R.layout.activity_geo_cache_info_tab, container, false);
                    container.addView(view);
                    new LoadInfoTask(GeoCacheActivity.this).execute(geoCacheId);
                    return view;
                case 1:
                    view = GeoCacheActivity.this.getLayoutInflater().inflate(R.layout.activity_geo_cache_comment_tab, container, false);
                    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.commentsList);
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(GeoCacheActivity.this));
                    recyclerView.setAdapter(new CommentsTabAdapter(new JSONArray()));
                    container.addView(view);
                    new LoadCommentsTask(GeoCacheActivity.this).execute(geoCacheId);
                    return view;
                case 2:
                    view = GeoCacheActivity.this.getLayoutInflater().inflate(R.layout.activity_geo_cache_foto_tab, container, false);
                    GridView gridView = (GridView) view.findViewById(R.id.gallery);
                    gridView.setAdapter(new ImageGridAdapter(GeoCacheActivity.this, new JSONArray()));

                    container.addView(view);
                    new LoadPhotoUrlsTask(GeoCacheActivity.this).execute(geoCacheId);
                    return view;
            }
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}
