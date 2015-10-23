package geocaching;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import java.util.List;

import geocaching.ui.ImagePagerActivity;
import geocaching.ui.MainActivity;
import geocaching.ui.compass.CompassActivity;

public class GoTo {
    public static void geoCacheActivity(Context ctx, long geoCacheId, CharSequence name) {
        Intent intent = new Intent(ctx, geocaching.ui.GeoCacheActivity.class);
        intent.putExtra("geoCacheId", geoCacheId);
        intent.putExtra("name", name);
        ctx.startActivity(intent);
    }

    public static void imagePagerActivity(Context ctx, long geoCacheId, String name, List<String> imageUrls) {
        Intent intent = new Intent(ctx, ImagePagerActivity.class);
        intent.putExtra("geoCacheId", geoCacheId);
        intent.putExtra("name", name);
        intent.putExtra("imageUrls", imageUrls.toArray(new String[imageUrls.size()]));
        ctx.startActivity(intent);
    }

    public static void compassActivity(Context ctx, Location src, Location dest) {
        Intent intent = new Intent(ctx, CompassActivity.class);
        intent.putExtra("userLocation", new double[]{src.getLongitude(), src.getLatitude()});
        intent.putExtra("objectLocation", new double[]{dest.getLongitude(), dest.getLatitude()});
        ctx.startActivity(intent);
    }

    public static void mainActivity(Context ctx) {
        Intent intent = new Intent(ctx, MainActivity.class);
        ctx.startActivity(intent);
    }
}
