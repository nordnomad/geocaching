package geocaching;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import geocaching.ui.ImagePagerActivity;
import geocaching.ui.MainActivity;
import geocaching.ui.MapCompassActivity;
import geocaching.ui.compass.CompassActivity;

public class GoTo {
    public static void geoCacheActivity(Context ctx, GeoCache geoCache) {
        Intent intent = new Intent(ctx, geocaching.ui.GeoCacheActivity.class);
        intent.putExtra("geoCache", geoCache);
        ctx.startActivity(intent);
    }

    public static void geoCacheActivity(Context ctx, long geoCacheId, CharSequence name, double ln, double la) {
        GeoCache geoCache = new GeoCache();
        geoCache.id = (int) geoCacheId;
        geoCache.name = String.valueOf(name);
        geoCache.ln = ln;
        geoCache.la = la;
        //TODO remove hardcode
        geoCache.status = GeoCacheStatus.ACTIVE_CHECKPOINT;
        geoCache.type = GeoCacheType.TRADITIONAL;
        geoCacheActivity(ctx, geoCache);
    }

    public static void imagePagerActivity(Context ctx, long geoCacheId, String name, List<String> imageUrls) {
        Intent intent = new Intent(ctx, ImagePagerActivity.class);
        intent.putExtra("geoCacheId", geoCacheId);
        intent.putExtra("name", name);
        intent.putExtra("imageUrls", imageUrls.toArray(new String[imageUrls.size()]));
        ctx.startActivity(intent);
    }

    public static void compassActivity(Context ctx, GeoCache geoCache) {
        Intent intent = new Intent(ctx, CompassActivity.class);
        intent.putExtra("geoCache", geoCache);
        ctx.startActivity(intent);
    }

    public static void mainActivity(Context ctx) {
        Intent intent = new Intent(ctx, MainActivity.class);
        ctx.startActivity(intent);
    }

    public static void compassMapActivity(Context ctx, GeoCache geoCache) {
        Intent intent = new Intent(ctx, MapCompassActivity.class);
        intent.putExtra("geoCache", geoCache);
        ctx.startActivity(intent);
    }
}
