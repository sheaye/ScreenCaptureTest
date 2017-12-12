package com.xinyan.test;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements ScreenCaptureFragment.OnCaptureFinishedListener {

    private ImageView mImageView;
    protected ScreenCaptureFragment mCaptureFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.image_view);
        mCaptureFragment = new ScreenCaptureFragment();
        getSupportFragmentManager().beginTransaction().add(mCaptureFragment,"capture").commit();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_capture:
                mCaptureFragment.startCapture(this);
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
