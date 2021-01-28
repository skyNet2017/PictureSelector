package com.hss01248.videocompress.mediacodec;

import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.CompressorConfig;
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
    @Override
    public void compress(String inputPath, String outPath, @CompressType.Type String compressType,
                         ICompressListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    VideoInfo info = VideoInfo.getInfo(inputPath);
                    listener.onStart(inputPath, outPath);
                    long start = System.currentTimeMillis();
                    VideoProcessor.Processor process =   VideoProcessor.processor(VideoCompressUtil.context)
                            .input(inputPath) // .input(inputVideoUri)
                            .output(outPath);
                            //以下参数全部为可选
                            if(compressType.equals(CompressType.TYPE_UPLOAD_720P)){
                                 process.outWidth(720)
                                        .outHeight(1280)
                                        .bitrate(1000)       //输出视频比特率
                                        .frameRate(25);   //帧率
                            }else if(compressType.equals(CompressType.TYPE_LOCAL_STORE)){
                                process.outWidth(2160)
                                        .outHeight(3840)
                                        .bitrate(16*1024*1024*8)       //输出视频比特率
                                        .frameRate(30);   //帧率
                            }

                            //.startTimeMs(startTimeMs)//用于剪辑视频
                            //.endTimeMs(endTimeMs)    //用于剪辑视频
                            // .speed(speed)            //改变视频速率，用于快慢放
                            // .changeAudioSpeed(changeAudioSpeed) //改变视频速率时，音频是否同步变化

                            //.iFrameInterval(iFrameInterval)  //关键帧距，为0时可输出全关键帧视频（部分机器上需为-1）
                            process.progressListener(new VideoProgressListener() {
                                @Override
                                public void onProgress(float progress) {
                                    listener.onProgress((int) (progress*100),System.currentTimeMillis() - start);
                                }
                            })      //可输出视频处理进度
                            .process();
                    listener.onFinish(outPath);
                } catch (Throwable e) {
                    e.printStackTrace();
                    listener.onError(e.getClass().getName()+": "+e.getMessage());
                }
            }
        }).start();
    }
}
