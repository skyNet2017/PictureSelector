package com.hss01248.videocompress.listener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.hss01248.videocompress.listener.ICompressListener;

import java.io.File;

public class DefaultDialogCompressListener implements ICompressListener {
   protected Handler handler  = new Handler(Looper.getMainLooper());
    protected final ProgressDialog[] dialog = {null};
    protected ICompressListener listener;

    public DefaultDialogCompressListener(Activity activity,ICompressListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

   protected Activity activity;
    protected String inputPath;
    protected long start;
    @Override
    public void onStart(String inputPath,String outPath) {
        listener.onStart(inputPath, outPath);
        this.inputPath = inputPath;
        start = System.currentTimeMillis();
        handler.post(new Runnable() {
            @Override
            public void run() {
                dialog[0] = new ProgressDialog(activity);
                dialog[0].setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog[0].setTitle("压缩中: "+new File(inputPath).getName());
                dialog[0].setMax(100);
                dialog[0].setCancelable(false);
                dialog[0].setCanceledOnTouchOutside(false);
                dialog[0].show();
            }
        });
    }

    @Override
    public void onProgress(int progress, long progressTime) {
        listener.onProgress(progress, progressTime);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(dialog[0] != null){
                    dialog[0].setProgress(progress);
                }
            }
        });
    }

    @Override
    public void onError(String message) {
        listener.onError(message);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(dialog[0] != null){
                    dialog[0].dismiss();
                }
                Toast.makeText(activity,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onFinish(String outputFilePath) {
        listener.onFinish(outputFilePath);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(dialog[0] != null){
                    dialog[0].dismiss();
                }
                //showInfo(file,out,start,activity,handler);
            }
        });
    }
}
