package com.hss01248.takephoto.demo;



import androidx.multidex.MultiDexApplication;

import com.blankj.utilcode.util.AppUtils;
import com.hss01248.takephoto.api.TakePhotoUtil3;
import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.bitrate.YoutubeBitrateConfig;

public class BaseApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        TakePhotoUtil3.init(this,true, AppUtils.isAppDebug());

        //VideoCompressUtil.setCompressor(new FFmpegCompressImpl());
        VideoCompressUtil.setGlobalBitRateConfig(new YoutubeBitrateConfig());
    }
}
