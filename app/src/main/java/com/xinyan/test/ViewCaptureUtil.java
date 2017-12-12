package com.xinyan.test;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Xinyan on 2017/12/12.
 */

public class ViewCaptureUtil {

    public static Bitmap capture(View view) {
        if (view == null) {
            return null;
        }
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache();
        bitmap = Bitmap.createBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public static Bitmap capture(Activity activity) {
        if (activity == null) {
            return null;
        }
        View decorView = activity.getWindow().getDecorView();
        return capture(decorView);
    }
}
