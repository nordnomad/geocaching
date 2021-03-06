package geocaching.ui.compass;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import geocaching.ui.FavoritesScreen;
import geocaching.ui.MainActivity;

public class CompassView2 extends ImageView {

    private static final int FAST_ANIMATION_DURATION = 200;
    private static final int DEGREES_360 = 360;
    private static final float CENTER = 0.5f;

    private Context context;
    private Location userLocation;
    private Location objectLocation;
    private Bitmap directionBitmap;

    private int drawableResource;
    private float lastRotation;

    @SuppressWarnings("unused")
    public CompassView2(Context context) {
        super(context);
        init(context);
    }

    @SuppressWarnings("unused")
    public CompassView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public static boolean isDeviceCompatible(Context context) {
        return context.getPackageManager() != null &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER) &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);
    }

    private void init(Context context) {
        this.context = context;

//        if (isDeviceCompatible(context)) {
//            throw new RuntimeException("Your activity must extends from CompassSensorsActivity");
//        } else setVisibility(GONE);
    }

    public void initializeCompass(Location userLocation, Location objectLocation, int drawableResource) {
        if (isDeviceCompatible(context)) {
            this.userLocation = userLocation;
            this.objectLocation = objectLocation;
            this.drawableResource = drawableResource;
            startRotation();
        }
    }

    private void startRotation() {
        GeomagneticField geomagneticField = new GeomagneticField(
                (float) userLocation.getLatitude(), (float) userLocation.getLongitude(),
                (float) userLocation.getAltitude(), System.currentTimeMillis());

        float azimuth = ((FavoritesScreen) ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("FAVOURITE_SCREEN")).getAzimuth();
        azimuth -= geomagneticField.getDeclination();

        float bearTo = userLocation.bearingTo(objectLocation);
        if (bearTo < 0) bearTo = bearTo + DEGREES_360;

        float rotation = bearTo - azimuth;
        if (rotation < 0) rotation = rotation + DEGREES_360;
        setImageDrawable(ContextCompat.getDrawable(context, drawableResource));
        rotateImageView(this, drawableResource, rotation);

//        if (BuildConfig.DEBUG) Log.d(CompassConstants.LOG_TAG, String.valueOf(rotation));
    }

    @SuppressWarnings("ConstantConditions")
    private void rotateImageView(ImageView compassView, int drawable, float currentRotate) {
        if (directionBitmap == null) {

            directionBitmap = BitmapFactory.decodeResource(getResources(), drawable);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            fadeInAnimation.setAnimationListener(new CustomAnimationListener());
            compassView.startAnimation(fadeInAnimation);
            compassView.setImageDrawable(new BitmapDrawable(getResources(), directionBitmap));
            compassView.setScaleType(ScaleType.CENTER);

        } else {
            currentRotate = currentRotate % DEGREES_360;
            int animationDuration = FAST_ANIMATION_DURATION;

            RotateAnimation rotateAnimation = new RotateAnimation(lastRotation, currentRotate,
                    Animation.RELATIVE_TO_SELF, CENTER, Animation.RELATIVE_TO_SELF, CENTER);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            rotateAnimation.setDuration(animationDuration);
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setAnimationListener(new CustomAnimationListener());

            lastRotation = currentRotate;

            compassView.startAnimation(rotateAnimation);
        }
    }

    private class CustomAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            startRotation();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

}