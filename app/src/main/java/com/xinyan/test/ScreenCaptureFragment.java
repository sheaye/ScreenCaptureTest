package com.xinyan.test;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import static android.app.Activity.RESULT_OK;

public class ScreenCaptureFragment extends Fragment {

    private static final String TAG = "ScreenCaptureFragment";
    private static final int REQUEST_CAPTURE = 2;

    public interface OnCaptureFinishedListener {
        void onCaptureSuccess(Bitmap bitmap);

        void onCaptureFailure();
    }

    private OnCaptureFinishedListener mOnCaptureFinishedListener;
    private MediaProjectionManager mMediaProjectionManager;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private Intent mResultData;
    private int mResultCode;
    private VirtualDisplay mVirtualDisplay;
    private ImageToBitmapAsyncTask mImageToBitmapTask;

    public ScreenCaptureFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMediaProjectionManager = ((MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE));
        setupImageReader();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setupImageReader() {
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        mScreenDensity = displayMetrics.densityDpi;
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
//      图片格式PixelFormat.RGBA_8888不是乱写的，与Bitmap.Config的图片格式对应
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
    }

    /**
     * 启动截屏
     *
     * @param onCaptureFinishedListener 截屏完成的监听器
     */
    public void startCapture(OnCaptureFinishedListener onCaptureFinishedListener) {
        mOnCaptureFinishedListener = onCaptureFinishedListener;
        if (mMediaProjection != null) {// 如果已经有令牌，直接拓印屏幕内容
            setupVirtualDisplay();
        } else if (mResultData != null && mResultCode != 0) {// 如果已经获得录屏权限，构建MediaProjection，然后拓印屏幕内容
            setupMediaProjection();
            setupVirtualDisplay();
        } else {// 如果没有录屏权限：请求录屏权限
            requestCapturePermission(REQUEST_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "截屏权限被禁止！");
                return;
            }
            mResultCode = resultCode;
            mResultData = data;
            setupMediaProjection();
            setupVirtualDisplay();
        }
    }

    /*获得MediaProjection:一个允许程序截取屏幕内容的令牌*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestCapturePermission(int requestCode) {
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), requestCode);
    }

    /*获得虚拟显示，可理解为拓印屏幕内容*/
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupVirtualDisplay() {
        if (mImageToBitmapTask != null && mImageToBitmapTask.isRunning()) {
            Log.e(TAG,"There is already a task running");
            return;
        }
        Image image;
        do {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCaptor",
                    mScreenWidth, mScreenHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.getSurface(), null, null);
            image = mImageReader.acquireNextImage();
        } while (image == null);
        mImageToBitmapTask = new ImageToBitmapAsyncTask(mOnCaptureFinishedListener);
        mImageToBitmapTask.execute(image);
    }

    private static class ImageToBitmapAsyncTask extends AsyncTask<Image, Void, Bitmap> {

        private boolean isRunning;
        private WeakReference<OnCaptureFinishedListener> mListenerWeakReference;

        public ImageToBitmapAsyncTask(OnCaptureFinishedListener listener) {
            mListenerWeakReference = new WeakReference<>(listener);
        }

        @Override
        protected void onPreExecute() {
            isRunning = true;
        }

        @Override
        protected Bitmap doInBackground(Image... images) {
            Image image = null;
            if (images != null && images.length > 0) {
                image = images[0];
            }
            if (image != null) {
                return imageToBitmap(images[0]);
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private Bitmap imageToBitmap(Image image) {
            if (image == null) {
                return null;
            }
            try {
                int width = image.getWidth();
                int height = image.getHeight();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
                //每个像素的间距
                int pixelStride = planes[0].getPixelStride();
                //总的间距
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                return Bitmap.createBitmap(bitmap, 0, 0, width, height);
            } catch (ArithmeticException e) {
                return null;
            } finally {
                image.close();
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            isRunning = false;
            OnCaptureFinishedListener listener = mListenerWeakReference.get();
            if (listener == null) {
                return;
            }
            if (bitmap != null) {
                listener.onCaptureSuccess(bitmap);
            } else {
                listener.onCaptureFailure();
            }
        }

        public boolean isRunning() {
            return isRunning;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopMediaProjection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageToBitmapTask != null && !mImageToBitmapTask.isCancelled()) {
            mImageToBitmapTask.cancel(true);
            mImageToBitmapTask = null;
        }
        releaseVirtualDisplay();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void releaseVirtualDisplay() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }
}
