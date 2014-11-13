package geocaching.ui;

import android.app.Activity;
import android.os.Bundle;
import geocaching.tasks.LoadInfoTask;
import map.test.myapplication3.app.R;

public class GeoChacheActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_cache);
        long geoCacheId = getIntent().getLongExtra("geoCacheId", 0);
        new LoadInfoTask(this).execute(geoCacheId);
    }

}
