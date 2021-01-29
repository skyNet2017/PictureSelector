package com.hss01248.videocompress.mediacodec;

import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hss01248.videocompress.CompressHepler;
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
    public void compress(VideoInfo.RealCompressInfo info ,String inputPath, String outPath, @CompressType.Type String compressType,
                         ICompressListener listener) {
        new Thread(new Runnable() {
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


                    VideoProcessor.processor(VideoCompressUtil.context)
                            .input(inputPath)
                            .output(outPath)
                            .outWidth(info.outWidth)
                          .outHeight(info.outHeight)
                          .bitrate(info.outBitRate)
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
        }).start();
    }

    /**
     * 码率/分辨率 比例,线性拟合
     *  720p上传的 拟合数据源: 阿里云点播码率表  https://help.aliyun.com/document_detail/86068.html  y = 0.0018x - 545.63
     *
     *  本地收藏保存: y = 0.0018x + 1059.6
     * @param inputPath
     * @param originWidth
     * @param originHeight
     * @return kbps
     */
    private int getExpectedBitRate(String inputPath, int originWidth, int originHeight, @CompressType.Type String compressType) {
        int expect = 1500;
        //本地收藏保存: y = 0.0018x + 1059.6
        if(CompressType.TYPE_LOCAL_STORE.equals(compressType) || CompressType.TYPE_BILIBILI.equals(compressType)){
             expect = (int) (0.0018*originHeight*originWidth +1059.6);
             //y = 0.0018x - 545.63
        }else if( CompressType.TYPE_UPLOAD_720P.equals(compressType) || CompressType.TYPE_UPLOAD_1080P.equals(compressType)){
            expect = (int) ((int) (0.0018*originHeight*originWidth -545.63) *1.2);
        }
        return expect;
    }

    private boolean processByType(VideoProcessor.Processor process,int outWidth, int outHeight, int bitrate, String inputPath, String compressType) {
        if(CompressType.TYPE_UPLOAD_720P.equals(compressType)){
          return   compressToUpload(720,process,outWidth,outHeight,bitrate,inputPath,compressType);
        }else if(CompressType.TYPE_UPLOAD_1080P.equals(compressType)){
          return   compressToUpload(1080,process,outWidth,outHeight,bitrate,inputPath,compressType);
        }else if(CompressType.TYPE_BILIBILI.equals(compressType)){
          return   compressToUpload(1080,process,outWidth,outHeight,bitrate,inputPath,compressType);
        }else if(CompressType.TYPE_LOCAL_STORE.equals(compressType)){
           return compressToUpload(Math.min(outHeight,outWidth),process,outWidth,outHeight,bitrate,inputPath,compressType);
        }
        return   compressToUpload(720,process,outWidth,outHeight,bitrate,inputPath,compressType);
    }

    /**
     *
     * @param targetResolution
     * @param process
     * @param inputWidth
     * @param inputHeight
     * @param originalBitrate
     * @param inputPath
     * @param compressType
     * @return 返回是否需要压缩
     */
    private boolean compressToUpload(int targetResolution,  VideoProcessor.Processor process,
                                  int inputWidth, int inputHeight, int originalBitrate, String inputPath, String compressType) {

        if(inputWidth < inputHeight){
            if(inputWidth >= targetResolution){
                float ratio = inputHeight*1.0f/inputWidth;
                int targetHeight = targetResolution*inputHeight/inputWidth;
                //todo
                int expetedRatesInkps = getExpectedBitRate(inputPath,targetResolution,targetHeight,compressType)*1024;
                int bitRates = getBitRate(expetedRatesInkps,originalBitrate,ratio);
                Log.w("dd","bitrates cal to compress:"+bitRates/1024);
                process.outWidth(targetResolution)
                        .outHeight(targetHeight)
                .bitrate(bitRates);
            }else {
                int expetedRatesInkps = getExpectedBitRate(inputPath,inputWidth,inputHeight,compressType)*1024;
                if(originalBitrate > expetedRatesInkps){
                    process.outWidth(inputWidth)
                            .outHeight(inputHeight)
                            .bitrate(originalBitrate);
                }else {
                    //不需要压缩
                    return false;
                }
            }
        }else {
            if(inputHeight >= targetResolution){
                float ratio = inputWidth*1.0f/inputHeight;
                int targetW = targetResolution*inputWidth/inputHeight;
                int expetedRatesInkps = getExpectedBitRate(inputPath,targetResolution,targetW,compressType)*1024;
                int bitRates = getBitRate(expetedRatesInkps,originalBitrate,ratio);
                Log.w("dd","bitrates cal to compress:"+bitRates/1024);
                process.outHeight(targetResolution)
                        .outWidth(targetResolution*inputWidth/inputHeight)
                        .bitrate(bitRates);
            }else {
                //不需要压缩分辨率,就看看要不要减少码率
                int expetedRatesInkps = getExpectedBitRate(inputPath,inputWidth,inputHeight,compressType)*1024;
                if(originalBitrate > expetedRatesInkps){
                    process.outWidth(inputWidth)
                            .outHeight(inputHeight)
                            .bitrate(originalBitrate);
                }else {
                    //不需要压缩
                    return false;
                }
            }
        }
        return true;
    }

    private int getBitRate(int expetedRatesInkps, int originalBitrate, float ratio) {
        if(originalBitrate/ratio > expetedRatesInkps){
            return expetedRatesInkps;
        }else if(expetedRatesInkps > originalBitrate){
            return originalBitrate;
        }else {
            return (int) (originalBitrate/ratio);
        }
    }
}
