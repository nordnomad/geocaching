package geocaching.ui.compass;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import map.test.myapplication3.app.BuildConfig;

// TODO duplicate of geocaching.ui.compass.CompassView2
public class CompassView extends ImageView {

    private static final int FAST_ANIMATION_DURATION = 600;
    private static final int DEGREES_360 = 360;
    private static final float CENTER = 0.5f;

    private Context context;
    private Location userLocation;
    private Location objectLocation;
    private Bitmap directionBitmap;

    private int drawableResource;
    private float lastRotation;

    public CompassView(Context context) {
        super(context);
        init(context);
    }

    public CompassView(Context context, AttributeSet attrs) {
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

        if (isDeviceCompatible(context)) {
            if (!(context instanceof CompassSensorsActivity))
                throw new RuntimeException("Your activity must extends from CompassSensorsActivity");
        } else setVisibility(GONE);
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

        float azimuth = ((CompassSensorsActivity) context).getAzimuth();
        azimuth -= geomagneticField.getDeclination();

        float rotation = -azimuth;
        if (rotation < 0) rotation = rotation + DEGREES_360;

        rotateImageView(this, drawableResource, rotation);

        if (BuildConfig.DEBUG) Log.d("compass-logs", String.valueOf(rotation));
    }

    private void rotateImageView(ImageView compassView, int drawable, float currentRotate) {
        if (directionBitmap == null) {
            directionBitmap = BitmapFactory.decodeResource(getResources(), drawable);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            fadeInAnimation.setAnimationListener(new CustomAnimationListener());
            compassView.startAnimation(fadeInAnimation);
            compassView.setImageDrawable(new BitmapDrawable(getResources(), directionBitmap));
            compassView.setScaleType(ImageView.ScaleType.CENTER);
        } else {
            currentRotate = currentRotate % DEGREES_360;
            RotateAnimation rotateAnimation = new RotateAnimation(lastRotation, currentRotate,
                    Animation.RELATIVE_TO_SELF, CENTER, Animation.RELATIVE_TO_SELF, CENTER);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            rotateAnimation.setDuration(FAST_ANIMATION_DURATION);
            rotateAnimation.setFillAfter(true);
            rotateAnimation.setAnimationListener(new CustomAnimationListener());

            lastRotation = currentRotate;
            compassView.startAnimation(rotateAnimation);
        }
    }

    private void drawTriangle(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        int ch = canvas.getHeight();
        int cw = canvas.getWidth();
        if (userLocation != null) {
            canvas.rotate(userLocation.bearingTo(objectLocation), cw / 2, ch / 2);
        }
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(cw / 2, 0);
        path.lineTo(cw / 2 + 20, 40);
        path.lineTo(cw / 2 - 20, 40);
        path.lineTo(cw / 2, 0);
        path.close();
        canvas.drawPath(path, paint);
        canvas.save();
        canvas.restore();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = new Paint();
        paint.setStrokeWidth(10);
        paint.setColor(Color.BLUE);
        paint.setTextSize(80);
        int cW = canvas.getWidth();
        int cH = canvas.getHeight();
        canvas.drawText("N", cW / 2 - 28, 114, paint);
        canvas.rotate(90, cW / 2, cH / 2);
        canvas.drawText("E", cW / 2 - 25, 114, paint);
        canvas.rotate(90, cW / 2, cH / 2);
        canvas.drawText("S", cW / 2 - 27, 114, paint);
        canvas.rotate(90, cW / 2, cH / 2);
        canvas.drawText("W", cW / 2 - 33, 114, paint);
        canvas.save();
        canvas.restore();

        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(cW / 2, cH / 2, 200, paint);

        paint.setStrokeWidth(5);
        for (int i = 0; i < 36; i++) {
            canvas.rotate(10, cW / 2, cH / 2);
            canvas.drawLine(cW / 2, 20, cW / 2, 0, paint);
        }
        canvas.save();
        canvas.restore();

        paint.setStrokeWidth(30);
        for (int i = 0; i < 4; i++) {
            canvas.rotate(90, cW / 2, cH / 2);
            canvas.drawLine(cW / 2, 40, cW / 2, 0, paint);
        }
        canvas.save();
        canvas.restore();

        canvas.drawLine(cW / 2, 130, cW / 2, cH - 130, paint);
        canvas.rotate(90, cW / 2, cH / 2);
        canvas.drawLine(cW / 2, 130, cW / 2, cH - 130, paint);
        canvas.save();
        canvas.restore();
        drawTriangle(canvas);

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
