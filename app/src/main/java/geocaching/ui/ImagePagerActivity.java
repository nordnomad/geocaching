package geocaching.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import geocaching.ui.adapters.ImagePagerAdapter;
import map.test.myapplication3.app.R;

public class ImagePagerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_pager_activity);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        String[] imageUrls = getIntent().getStringArrayExtra("imageUrls");
        pager.setAdapter(new ImagePagerAdapter(this, imageUrls));
    }
}
