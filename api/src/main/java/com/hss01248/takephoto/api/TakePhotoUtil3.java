package com.hss01248.takephoto.api;

import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.video.RxVideoCompressor;

import java.util.List;

public class TakePhotoUtil3 {

    public static void pickImage(FragmentActivity activity,TakePhotoListener listener){

    }

    public static void pickVideo(FragmentActivity activity,TakePhotoListener listener){
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofVideo())
                //.loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(1)
                .imageSpanCount(3)
                .videoMaxSecond(15)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        if(result != null){
                            LocalMedia media = result.get(0);
                            listener.onSuccess(media.getRealPath());
                            /*if(media.getRealPath().endsWith(".mp4") || media.getRealPath().endsWith(".MP4")){
                                RxVideoCompressor.compress(SimpleActivity.this,media.getRealPath(),forUpload);
                            }*/
                        }else {
                            //Toast.makeText(SimpleActivity.this,"result is 0",Toast.LENGTH_LONG).show();
                            listener.onFail("","result empty");
                        }

                    }

                    @Override
                    public void onCancel() {
                        // onCancel Callback
                        listener.onCancel();
                    }
                });
    }
}
