package com.hss01248.videocompress;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.activityresult.ActivityResultListener;
import com.hss01248.activityresult.StartActivityUtil;
import com.hss01248.openuri2.OpenUri2;
import com.hss01248.permission.MyPermissions;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @Despciption todo
 * @Author hss
 * @Date 9/27/24 2:22 PM
 * @Version 1.0
 */
public class VideoCaptureBySysUtil {





    public static void startVideoCapture(boolean useFrontCamera,int maxDurationInSecond, int maxFileSize, MyCommonCallback5<String> callback){
        File externalFilesDir = Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if(externalFilesDir == null){
            //Android4.4以下:
            externalFilesDir = new File(Utils.getApp().getFilesDir(),Environment.DIRECTORY_MOVIES);
        }
        externalFilesDir.mkdirs();
        File file = new File(externalFilesDir,System.currentTimeMillis()+".mp4");
        startVideoCapture(useFrontCamera,file.getAbsolutePath(),maxDurationInSecond,maxFileSize,callback);
    }


    public static void startVideoCapture(boolean useFrontCamera,String path, int maxDurationInSecond,  int maxFileSize, MyCommonCallback5<String> callback){

        MyPermissions.request(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(@NonNull List<String> granted) {
                videoCaptureIntent(useFrontCamera,path, maxDurationInSecond, maxFileSize,callback);
            }

            @Override
            public void onDenied(@NonNull List<String> deniedForever, @NonNull List<String> denied) {
                callback.onError("no permission","need permissions:"
                        + Arrays.toString(denied.toArray()).replace("[","")
                        .replace("]","")
                        .replaceAll("android\\.permission\\.","")
                        .replace(",","")
                        .toLowerCase() ,null);
            }
        }, Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO);

    }

    private static void videoCaptureIntent(boolean useFrontCamera,String path,int maxDurationInSecond, int maxFileSize,MyCommonCallback5<String> callback) {
        Intent intent=new Intent();
        // 指定开启系统相机的Action
        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
       // 0：表示低质量，适合用于MMS消息。
       // 1：表示高质量，通常用于保存到设备上// 设置视频质量

        if(useFrontCamera){
            intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        }

        intent.addCategory(Intent.CATEGORY_DEFAULT);
        // 根据文件地址创建文件
        File file=new File(path);
        // 把文件地址转换成Uri格式
        Uri uri= OpenUri2.fromFile(Utils.getApp(),file);
        OpenUri2.addPermissionRW(intent);
        // 设置系统相机拍摄照片完成后图片文件的存放地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if(maxDurationInSecond> 0){
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, maxDurationInSecond);
        }

        if(maxFileSize> 0){
            intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxFileSize);
        }
        //MediaStore.EXTRA_OUTPUT：设置媒体文件的保存路径。
        //MediaStore.EXTRA_VIDEO_QUALITY：设置视频录制的质量，0为低质量，1为高质量。
        //MediaStore.EXTRA_DURATION_LIMIT：设置视频最大允许录制的时长，单位为毫秒。
        //MediaStore.EXTRA_SIZE_LIMIT：指定视频最大允许的尺寸，单位为byte。

        StartActivityUtil.goOutAppForResult(ActivityUtils.getTopActivity(), intent, new ActivityResultListener() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                LogUtils.d(resultCode,data);
                if(resultCode == Activity.RESULT_CANCELED){
                    callback.onError("cancel","you have canceled the recoding",null);
                    return;
                }
                if(resultCode != Activity.RESULT_OK){
                    LogUtils.w("result code is not RESULT_OK:"+resultCode);
                }
                if(file.exists() && file.length()> 0){
                   // MediaStoreRefresher.refreshMediaCenter(Utils.getApp(),path);
                    //私有目录,其实刷也没有用
                    callback.onSuccess(path);
                }else {
                    callback.onError("file error","file saved error",null);
                }

            }

            @Override
            public void onActivityNotFound(Throwable e) {
                callback.onError("onActivityNotFound","no application to record video",null);
            }
        });
    }

}
