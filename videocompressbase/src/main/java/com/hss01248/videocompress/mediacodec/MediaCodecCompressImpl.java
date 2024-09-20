package com.hss01248.videocompress.mediacodec;

import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.VideoCompressUtil;
import com.hss01248.videocompress.VideoInfo;
import com.hss01248.videocompress.listener.ICompressListener;
import com.hss01248.videocompress.ICompressor;
import com.hw.videoprocessor.VideoProcessor;
import com.hw.videoprocessor.util.VideoProgressListener;

public class MediaCodecCompressImpl implements ICompressor {
    /**
     * https://github.com/yellowcath/VideoProcessor
     * VideoProcessor使用Android原生的MediaCodec实现视频压缩、剪辑、混音、快慢放及倒流的功能（快慢放及倒流支持音频同步变化），在支持MediaCodec的手机上优于使用FFmpeg的方案
     *
     * 体积小 ：编译后的aar只有262K，ffmpeg一个so就7、8M，精简之后也差不多还有一半大小
     * 速度快 ：在huaweiP9上压缩(1080P 20s 20000k -> 720p 2000k)
     * @param inputPath
     * @param outPath
     * @param compressType
     * @param listener
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    @Override
    public void compress(boolean async,VideoInfo.RealCompressInfo info ,String inputPath, String outPath, @CompressType.Type String compressType,
                         ICompressListener listener) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    //VideoInfo info = VideoInfo.getInfo(inputPath);
                    long start = System.currentTimeMillis();

                    final boolean[] finished = {false};
                    final boolean[] posted = {false};
                    Handler handler = new Handler(Looper.getMainLooper());
                    Runnable finish = new Runnable() {
                        @Override
                        public void run() {
                            if(finished[0]){
                                return;
                            }
                            finished[0] = true;
                            listener.onFinish(outPath);

                        }
                    };
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                        //兼容性处理,api21以下,不压缩.
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFinish(inputPath);
                            }
                        });
                        return;
                    }

                    VideoProcessor.processor(VideoCompressUtil.context)
                            .input(inputPath)
                            .output(outPath)
                            .outWidth(info.outWidth)
                            .outHeight(info.outHeight)
                            .bitrate(info.outBitRate)
                            .frameRate(30)
                            .progressListener(new VideoProgressListener() {
                                @Override
                                public void onProgress(float progress) {
                                    //Log.d("progress","P:"+progress);
                                    listener.onProgress((int) (progress*100),System.currentTimeMillis() - start);
                                    int percent = (int) (progress *100);
                                    if(percent == 98){
                                        if(posted[0]){
                                            return;
                                        }
                                        handler.postDelayed(finish,8000);
                                        posted[0] = true;
                                    }else if(progress == 1.0f){
                                        finished[0] = true;
                                        handler.removeCallbacks(finish);
                                        listener.onFinish(outPath);
                                    }

                                }
                            })
                            .process();
                   /* VideoProcessor.Processor process =   VideoProcessor.processor(VideoCompressUtil.context)
                            .input(inputPath) // .input(inputVideoUri)
                            .output(outPath);
                            //.startTimeMs(startTimeMs)//用于剪辑视频
                            //.endTimeMs(endTimeMs)    //用于剪辑视频
                            // .speed(speed)            //改变视频速率，用于快慢放
                            // .changeAudioSpeed(changeAudioSpeed) //改变视频速率时，音频是否同步变化
                            //.iFrameInterval(iFrameInterval)  //关键帧距，为0时可输出全关键帧视频（部分机器上需为-1）
                    */

                } catch (Throwable e) {
                    e.printStackTrace();
                    listener.onError(e.getClass().getName()+": "+e.getMessage());
                }
            }
        };
        if(async){
            new Thread(runnable).start();
        }else {
            runnable.run();
        }
    }






}
