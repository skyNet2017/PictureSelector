package com.luck.picture.lib.video;

import android.app.Activity;

import com.hss01248.videocompress.listener.DefaultDialogCompressListener;
import com.hss01248.videocompress.listener.ICompressListener;

public class DefaultDialogCompressListener2 extends DefaultDialogCompressListener {
    public DefaultDialogCompressListener2(Activity activity, ICompressListener listener) {
        super(activity, listener);
    }

    @Override
    public void onFinish(String outputFilePath) {
        super.onFinish(outputFilePath);
        handler.post(new Runnable() {
            @Override
            public void run() {
                CompressCompareActivity.start(activity,inputPath,outputFilePath,start);
            }
        });
    }
}
