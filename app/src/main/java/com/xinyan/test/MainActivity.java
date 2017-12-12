package com.xinyan.test;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ScreenCaptureFragment.OnCaptureFinishedListener {

    private ImageView mImageView;
    protected ScreenCaptureFragment mCaptureFragment;
    protected ImageView mBeautyImage;
    protected View mDecorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBeautyImage = findViewById(R.id.beauty);
        mImageView = findViewById(R.id.image_view);
        mDecorView = getWindow().getDecorView();
        mCaptureFragment = new ScreenCaptureFragment();
        getSupportFragmentManager().beginTransaction().add(mCaptureFragment, "capture").commit();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.capture_with_api:
                mCaptureFragment.startCapture(this);
                break;
            case R.id.capture_with_cache:
                Bitmap bitmap = ViewCaptureUtil.capture(mDecorView);
                if (bitmap != null) {
                    mImageView.setImageBitmap(bitmap);
                }else {
                    Toast.makeText(this, "获取失败", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    public void onCaptureSuccess(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onCaptureFailure() {

    }

}
