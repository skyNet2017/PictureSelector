package com.hss01248.takephoto.api;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.listener.ICompressListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.util.ArrayList;
import java.util.List;

public class TakePhotoUtil3 {

    public static void init(Context context,boolean showLog,boolean showCompareAfterCompress){
        VideoCompressUtil.init(context,showLog,showCompareAfterCompress);
    }

    public static void openAlbum(FragmentActivity activity, TakePhotoListener listener){
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll())
                //.loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(9)
                .videoMaxSecond(15)
                .imageSpanCount(3)
                .isCamera(false)
                //.compressSavePath(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        if(result != null){
                            List<String> paths = new ArrayList<>();
                            for (LocalMedia localMedia : result) {
                                paths.add(localMedia.getRealPath());
                            }
                            listener.onSuccess(paths);
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

    public static void openCamera(FragmentActivity activity, TakePhotoListener listener){
        PictureSelector.create(activity)
                .openCamera(PictureMimeType.ofAll())
                //.openGallery(PictureMimeType.ofVideo())
                //.loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(1)
                .isUseCustomCamera(true)
                //.imageSpanCount(2)
                //.isCompress(true)
                //.compressQuality(80)
                //限制15s
                .videoMaxSecond(15)
                .recordVideoSecond(15)
                .recordVideoMinSecond(1)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        if(result != null){
                            LocalMedia media = result.get(0);
                            List<String> paths = new ArrayList<>();
                            paths.add(media.getRealPath());
                            listener.onSuccess(paths);

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
