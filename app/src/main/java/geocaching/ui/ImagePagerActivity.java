package geocaching.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import geocaching.ui.adapters.ImagePagerAdapter;
import map.test.myapplication3.app.R;

public class ImagePagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_pager_activity);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        String[] imageUrls = getIntent().getStringArrayExtra("imageUrls");
        pager.setAdapter(new ImagePagerAdapter(this, imageUrls));
        setTitle(getIntent().getStringExtra("name"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}
