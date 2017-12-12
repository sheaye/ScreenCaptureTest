package com.xinyan.test;

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
}
