package com.yxhuang.androidnormal;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.yxhuang.androidnormal.R;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_";

    private ImageView mImageView;
    private Button mBtnRecycle;
    private Button mBtnLoad;

    private static final String URL = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1599135353752&di=ddea1113dc4f7442916dd4c7e85d806f&imgtype=0&src=http%3A%2F%2Ft8.baidu.com%2Fit%2Fu%3D3571592872%2C3353494284%26fm%3D79%26app%3D86%26f%3DJPEG%3Fw%3D1200%26h%3D1290";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.img);
        mBtnLoad = findViewById(R.id.btn_load);
        mBtnRecycle = findViewById(R.id.btn_recycle);


        mBtnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(MainActivity.this)
                        .load(URL)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>(){
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                Log.i(TAG, "onResourceReady " + resource.isRecycled());
                                if (resource.isRecycled()){


                                } else  {
                                    mImageView.setImageBitmap(resource);
                                }
                            }
                        });
            }
        });


        mBtnRecycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable =  mImageView.getDrawable();
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                bitmapDrawable.getBitmap().recycle();
            }
        });

    }


}