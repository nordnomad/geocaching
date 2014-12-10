package geocaching.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import geocaching.common.SlidingTabLayout;
import geocaching.tasks.LoadCommentsTask;
import geocaching.tasks.LoadInfoTask;
import map.test.myapplication3.app.R;

public class GeoCacheActivity extends Activity {

    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_cache);


        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new GeoCachePagerAdapter());

        // Give the SlidingTabLayout the ViewPager, this must be
        // done AFTER the ViewPager has had it's PagerAdapter set.
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);
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
                    container.addView(view);
                    new LoadCommentsTask(GeoCacheActivity.this).execute(geoCacheId);
                    return view;
                case 2:
                    view = GeoCacheActivity.this.getLayoutInflater().inflate(R.layout.activity_geo_cache_foto_tab, container, false);
                    container.addView(view);
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
