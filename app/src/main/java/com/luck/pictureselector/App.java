package com.luck.pictureselector;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.CameraXConfig;
import androidx.multidex.MultiDexApplication;


import com.luck.picture.lib.app.IApp;
import com.luck.picture.lib.app.PictureAppMaster;
import com.luck.picture.lib.engine.PictureSelectorEngine;

import io.microshow.rxffmpeg.RxFFmpegInvoke;


/**
 * @author：luck
 * @date：2019-12-03 22:53
 * @describe：Application
 */

public class App extends MultiDexApplication implements IApp, CameraXConfig.Provider {
    private static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        PictureAppMaster.getInstance().setApp(this);
        RxFFmpegInvoke.getInstance().setDebug(false);
        //VideoUtil2.call();
    }

    @Override
    public Context getAppContext() {
        return this;
    }

    @Override
    public PictureSelectorEngine getPictureSelectorEngine() {
        return new PictureSelectorEngineImp();
    }

    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }
}
