package geocaching;

import android.content.Context;
import android.content.Intent;

public class GoTo {
    public static void geoCacheActivity(Context ctx, long geoCacheId) {
        Intent intent = new Intent(ctx, geocaching.ui.GeoCacheActivity.class);
        intent.putExtra("geoCacheId", geoCacheId);
        ctx.startActivity(intent);
    }
}
