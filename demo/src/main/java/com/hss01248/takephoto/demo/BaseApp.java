package com.hss01248.takephoto.demo;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

import com.hss01248.takephoto.api.TakePhotoUtil3;

public class BaseApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        TakePhotoUtil3.init(this,true,true);
    }
}
