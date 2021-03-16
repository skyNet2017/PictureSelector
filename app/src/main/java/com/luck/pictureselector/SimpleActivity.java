package com.luck.pictureselector;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hss01248.base.compressorimpl.FFmpegCompressImpl;
import com.hss01248.media.localvideoplayer.VideoPlayUtil;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.media.metadata.MetaDataUtil;
import com.hss01248.takephoto.api.TakePhotoListener;
import com.hss01248.takephoto.api.TakePhotoUtil3;
import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.compare.DefaultDialogCompressListener2;
import com.hss01248.videocompress.listener.ICompressListener;
import com.hss01248.videocompress.mediacodec.MediaCodecCompressImpl;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class SimpleActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_activity, btn_fragment;

    private RadioGroup rgEngine,rgMode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        btn_activity = findViewById(R.id.btn_activity);
        btn_fragment = findViewById(R.id.btn_fragment);
        btn_activity.setOnClickListener(this);
        btn_fragment.setOnClickListener(this);
        rgEngine = findViewById(R.id.rg_compress_engine);
        rgMode = findViewById(R.id.rg_compress_mode);
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



    private void doSelectAndCompress(String mode) {
        //系统内核模式
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofVideo())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(1)
                .imageSpanCount(2)
                //.videoMaxSecond(forUpload? 15 : 600)
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
                                //RxVideoCompressor.compress(SimpleActivity.this,media.getRealPath(),forUpload);
                                doComressBg(media.getRealPath(),mode);
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

    private void doComressBg(String realPath,String mode) {
        Log.w("meta",MetaDataUtil.getAllInfo(realPath).toString());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            PersistableBundle  bundle = new PersistableBundle();
            bundle.putString("realPath",realPath);
            MyJobService.doInBg(getApplicationContext(), bundle, new MyJobService.IDoInBackground() {
                @Override
                public void run(PersistableBundle bundle) {
                    VideoCompressUtil.doCompressAsync(bundle.getString("realPath"), "", mode,
                            new DefaultDialogCompressListener2(SimpleActivity.this,
                                    new ICompressListener() {
                                        @Override
                                        public void onFinish(String outputFilePath) {

                                        }

                                        @Override
                                        public void onError(String message) {

                                        }
                                    }));
                }
            });
        }


    }


    public void doCompress(View view) {

      int id =   rgEngine.getCheckedRadioButtonId();
      if(id == R.id.ffmpeg){
          VideoCompressUtil.setCompressor(new FFmpegCompressImpl());
      }else {
          VideoCompressUtil.setCompressor(new MediaCodecCompressImpl());
      }

      int modeId = rgMode.getCheckedRadioButtonId();
      String mode = CompressType.TYPE_UPLOAD_720P;
      if(modeId == R.id.upload720p){
          mode = CompressType.TYPE_UPLOAD_720P;
      }else if(modeId == R.id.upload1080p){
          mode = CompressType.TYPE_UPLOAD_720P;
      }else if(modeId == R.id.localstore){
          mode = CompressType.TYPE_LOCAL_STORE;
      }else if(modeId == R.id.bilibili){
          mode = CompressType.TYPE_BILIBILI;
      }
      doSelectAndCompress(mode);

    }

    public void picImage(View view) {
        TakePhotoUtil3.openAlbum(this,5, new TakePhotoListener() {
            @Override
            public void onSuccess(List<String> path) {
                //后续自己上传前分别压缩
                postHandler(path.get(0));
            }
        });

    }

    public void picVideo(View view) {
        TakePhotoUtil3.openCamera(this, new TakePhotoListener() {
            @Override
            public void onSuccess(List<String> path) {
                //单个压缩
                postHandler(path.get(0));
            }
        });
    }

    private void postHandler(String path) {
        Toast.makeText(SimpleActivity.this,path,Toast.LENGTH_LONG).show();
        Log.i("path:","path:"+path);
        //示例:
        if(path.endsWith(".mp4")){
            compressVideo(path);
        }else {
            try {
                Log.i("exif",ExifUtil.readExif(new FileInputStream(new File(path))).toString().replace(",","\n"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void compressVideo(String path) {
        ICompressListener listener1 = new ICompressListener() {
            @Override
            public void onFinish(String outputFilePath) {

            }

            @Override
            public void onError(String message) {

            }
        };
        Log.w("meta",MetaDataUtil.getAllInfo(path).toString());
        doComressBg(path,CompressType.TYPE_UPLOAD_720P);
        //这个应该在上传时自己调用:
       /* VideoCompressUtil.doCompressAsync(path,null,
                CompressType.TYPE_UPLOAD_720P,
                VideoCompressUtil.showCompareAfterCompress ? new DefaultDialogCompressListener2(this,listener1) : listener1);*/
    }

    public void playVideo(View view) {
//        String url = "https://test-bimg.akulaku.net/biz/live-chat/user/e0ac2ff895d046c2a167f9f21589687d7606.mp4";
        String url = "https://test-bimg.akulaku.net/biz/live-chat/user/ea1bf820cab54fa586c82a98153be95c5379.mp4";

        VideoPlayUtil.startPreview(this, url, false, false);
    }
}
