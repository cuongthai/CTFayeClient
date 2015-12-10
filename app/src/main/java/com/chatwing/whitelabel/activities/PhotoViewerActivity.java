package com.chatwing.whitelabel.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.modules.PhotoViewerModule;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cuongthai on 11/16/15.
 */
public class PhotoViewerActivity extends BaseABFragmentActivity {
    public static final String PHOTO_URL = "PHOTO_URL";
    private ImageLoader mImageLoader;
    private DisplayImageOptions displayImageOptions;
    private String mUrl;
    private SubsamplingScaleImageView mImageView;

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new PhotoViewerModule(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoviewer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mUrl = getIntent().getStringExtra(PHOTO_URL);

        mImageLoader = ImageLoader.getInstance();
        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_film)
                .showImageForEmptyUri(R.drawable.ic_film)
                .showImageOnFail(R.drawable.ic_film)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        mImageView = (SubsamplingScaleImageView) findViewById(R.id.imageView);
        mImageLoader.loadImage(mUrl, displayImageOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if (bitmap != null) {
                    mImageView.setImage(ImageSource.cachedBitmap(bitmap));
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
