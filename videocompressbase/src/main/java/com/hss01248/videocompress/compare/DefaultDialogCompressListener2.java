package com.hss01248.videocompress.compare;

import android.app.Activity;

import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.listener.DefaultDialogCompressListener;
import com.hss01248.videocompress.listener.ICompressListener;

public class DefaultDialogCompressListener2 extends DefaultDialogCompressListener {
    public DefaultDialogCompressListener2(Activity activity, ICompressListener listener) {
        super(activity, listener);
    }

    @Override
    public void onFinish(String outputFilePath) {
        super.onFinish(outputFilePath);
        if(VideoCompressUtil.showCompareAfterCompress){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    CompressCompareActivity.start(activity,inputPath,outputFilePath,start);
                }
            });
        }

    }
}
