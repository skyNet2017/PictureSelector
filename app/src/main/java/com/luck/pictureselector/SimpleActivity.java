package com.luck.pictureselector;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.video.RxVideoCompressor;


import java.util.List;

public class SimpleActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_activity, btn_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        btn_activity = findViewById(R.id.btn_activity);
        btn_fragment = findViewById(R.id.btn_fragment);
        btn_activity.setOnClickListener(this);
        btn_fragment.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_activity:
                startActivity(new Intent(SimpleActivity.this, MainActivity.class));
                break;
            case R.id.btn_fragment:
                startActivity(new Intent(SimpleActivity.this, PhotoFragmentActivity.class));
                break;
            default:
                break;
        }
    }

    public void compress(View view) {

        //系统内核模式
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofVideo())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(1)
                .imageSpanCount(2)
                //播放4k视频卡顿,黑屏:
                // 可以实现bindCustomPlayVideoCallback接口，自定义播放界面，PictureSelector自带的是系统的VideoView,兼容性不是特别好
               /* .bindCustomPlayVideoCallback(new OnVideoSelectedPlayCallback() {
                    @Override
                    public void startPlayVideo(Object data) {

                    }
                })*/
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        if(result != null){
                            LocalMedia media = result.get(0);
                            if(media.getRealPath().endsWith(".mp4") || media.getRealPath().endsWith(".MP4")){
                                RxVideoCompressor.compress(SimpleActivity.this,media.getRealPath());
                            }
                        }else {
                            Toast.makeText(SimpleActivity.this,"result is 0",Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onCancel() {
                        // onCancel Callback
                    }
                });
    }
}
