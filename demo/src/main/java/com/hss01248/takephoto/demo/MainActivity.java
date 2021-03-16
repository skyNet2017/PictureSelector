package com.hss01248.takephoto.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hss01248.takephoto.api.TakePhotoListener;
import com.hss01248.takephoto.api.TakePhotoUtil3;
import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.listener.DefaultDialogCompressListener;
import com.hss01248.videocompress.listener.ICompressListener;
import com.luck.picture.lib.language.LanguageConfig;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.iv);
    }

    public void album(View view) {
        TakePhotoUtil3.openAlbum(this,5, LanguageConfig.INDONESIA,new TakePhotoListener() {
            @Override
            public void onSuccess(List<String> paths) {
                String list = Arrays.toString(paths.toArray()).replace(",","\n");
                Log.w("image","paths:"+list);
                /*Glide.with(MainActivity.this)
                        .load(paths.get(0))
                        .into(imageView);*/
                boolean hasCompress = false;
                for (String path : paths) {
                    if(!hasCompress){
                        if(path.endsWith(".mp4")|| path.endsWith(".MP4")){
                            hasCompress = true;
                            VideoCompressUtil.doCompressAsync(path, "", CompressType.TYPE_UPLOAD_720P, new DefaultDialogCompressListener(MainActivity.this,
                                    new ICompressListener() {
                                        @Override
                                        public void onFinish(String outputFilePath) {
                                            Log.w("image","doCompressAsync outputFilePath:"+outputFilePath);

                                        }

                                        @Override
                                        public void onError(String message) {
                                            Log.e("error","msg:"+message);

                                        }
                                    }));
                        }
                    }
                    if(path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")){
                        Glide.with(MainActivity.this)
                                .load(paths.get(0))
                                .into(imageView);
                    }

                }

            }
        });

    }

    public void camera(View view) {
        TakePhotoUtil3.openCamera(this, LanguageConfig.INDONESIA, new TakePhotoListener() {
            @Override
            public void onSuccess(List<String> paths) {
                String path = paths.get(0);
                Log.w("image","path:"+path);
                if(path.endsWith(".mp4")|| path.endsWith(".MP4")){
                    VideoCompressUtil.doCompressAsync(path, "", CompressType.TYPE_UPLOAD_720P, new DefaultDialogCompressListener(MainActivity.this,
                            new ICompressListener() {
                                @Override
                                public void onFinish(String outputFilePath) {
                                    Log.w("image","doCompressAsync outputFilePath:"+outputFilePath);

                                }

                                @Override
                                public void onError(String message) {
                                    Log.e("error","msg:"+message);

                                }
                            }));
                }else {
                    Glide.with(MainActivity.this)
                            .load(paths.get(0))
                            .into(imageView);
                }


            }

            @Override
            public void onFail(String path, String msg) {
                Log.e("error","msg:"+msg);
            }
        });
    }
}