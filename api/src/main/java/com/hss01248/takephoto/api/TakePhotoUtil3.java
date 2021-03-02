package com.hss01248.takephoto.api;


import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.hss01248.media.localvideoplayer.VideoPlayUtil;
import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.IPreviewVideo;
import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.listener.ICompressListener;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.app.IApp;
import com.luck.picture.lib.app.PictureAppMaster;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.engine.PictureSelectorEngine;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * com.hw.videoprocessor   minSdk version to at least 21
 */
public class TakePhotoUtil3 {

    public static void init(Application context, boolean showLog, boolean showCompareAfterCompress){
        VideoCompressUtil.init(context,showLog,showCompareAfterCompress);
        PictureAppMaster.getInstance().setApp(new IApp() {
            @Override
            public Context getAppContext() {
                return context;
            }

            @Override
            public PictureSelectorEngine getPictureSelectorEngine() {
                return new PictureSelectorEngineImp();
            }
        });

        VideoCompressUtil.setiPreviewVideo(new IPreviewVideo() {
            @Override
            public void preview(Context context, String path) {
                VideoPlayUtil.startPreview(context,path,false,true);
            }
        });
    }

    public static void openAlbum(FragmentActivity activity,int maxSelectNum, TakePhotoListener listener){
        PictureSelector.create(activity)
                .openGallery(PictureMimeType.ofAll())
                //.loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(maxSelectNum)
                .videoMaxSecond(15)
                .imageSpanCount(3)
                .isCamera(false)
                //.compressSavePath(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // onResult Callback
                        if(result != null){
                            String list = Arrays.toString(result.toArray()).replace(",","\n");
                            Log.d("onResult","list:\n"+list);
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
                            String list = Arrays.toString(result.toArray()).replace(",","\n");
                            Log.d("onResult","list:\n"+list);
                            LocalMedia media = result.get(0);
                            List<String> paths = new ArrayList<>();
                            if(TextUtils.isEmpty(media.getRealPath())){
                                listener.onFail("","result empty");
                            }else {
                                paths.add(media.getRealPath());
                                listener.onSuccess(paths);
                            }
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
