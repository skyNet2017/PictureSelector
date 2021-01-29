package com.hss01248.takephoto.api;


import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.listener.ICompressListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;
import com.luck.picture.lib.video.DefaultDialogCompressListener2;

import java.util.List;

public class TakePhotoUtil3 {

    public static void init(Context context,boolean showLog,boolean showCompareAfterCompress){
        VideoCompressUtil.init(context,showLog,showCompareAfterCompress);
    }

    public static void pickImage(FragmentActivity activity,TakePhotoListener listener){
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofImage())
                //.loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(3)
                .imageSpanCount(3)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        if(result != null){
                            LocalMedia media = result.get(0);
                            listener.onSuccess(media.getRealPath());
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

    public static void pickVideo(FragmentActivity activity,TakePhotoListener listener){
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofVideo())
                //.loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(1)
                .imageSpanCount(2)
                //限制15s
                .videoMaxSecond(15)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        if(result != null){
                            LocalMedia media = result.get(0);
                            listener.onSuccess(media.getRealPath());

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
