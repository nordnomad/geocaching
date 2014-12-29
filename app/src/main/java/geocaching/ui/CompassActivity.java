package geocaching.ui;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import garin.artemiy.compassview.library.CompassSensorsActivity;
import garin.artemiy.compassview.library.CompassView;
import map.test.myapplication3.app.R;

public class CompassActivity extends CompassSensorsActivity/* implements SensorEventListener */ {

    CompassView compassView;
    //    ImageView compassView;
    float currentDegree = 0f;
    //    SensorManager sensorManager;
    TextView tvHeading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
//        compassView = (ImageView) findViewById(R.id.compassView);
        tvHeading = (TextView) findViewById(R.id.tvHeading);
//        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        double[] ul = getIntent().getDoubleArrayExtra("userLocation");
        Location userLoc = new Location("");
        userLoc.setLongitude(ul[0]);
        userLoc.setLatitude(ul[1]);

        double[] ol = getIntent().getDoubleArrayExtra("objectLocation");
        Location objLoc = new Location("");
        objLoc.setLongitude(ol[0]);
        objLoc.setLatitude(ol[1]);

        compassView = (CompassView) findViewById(R.id.compassView);
        compassView.initializeCompass(userLoc, objLoc, R.drawable.arrow);
    }

   /* @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }*/

   /* @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        // how long the animation will take place
        ra.setDuration(210);
        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        // Start the animation
        compassView.startAnimation(ra);
        currentDegree = -degree;
    }*/

    /*@Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }*/
}
