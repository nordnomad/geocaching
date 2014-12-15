package geocaching;

import android.content.Context;
import android.content.Intent;

import java.util.List;

import geocaching.ui.ImagePagerActivity;

public class GoTo {
    public static void geoCacheActivity(Context ctx, long geoCacheId) {
        Intent intent = new Intent(ctx, geocaching.ui.GeoCacheActivity.class);
        intent.putExtra("geoCacheId", geoCacheId);
        ctx.startActivity(intent);
    }

    public static void imagePagerActivity(Context ctx, List<String> imageUrls) {
        Intent intent = new Intent(ctx, ImagePagerActivity.class);
        intent.putExtra("imageUrls", imageUrls.toArray(new String[imageUrls.size()]));
        ctx.startActivity(intent);
    }
}
