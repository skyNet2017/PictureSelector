package com.hss01248.base.compressorimpl;

import android.media.MediaMetadataRetriever;

import com.hss01248.videocompress.CompressType;
import com.hss01248.videocompress.CompressorConfig;
import com.hss01248.videocompress.listener.ICompressListener;
import com.hss01248.videocompress.ICompressor;
import com.hss01248.videocompress.VideoInfo;

import io.microshow.rxffmpeg.RxFFmpegInvoke;

public class FFmpegCompressImpl implements ICompressor {
    @Override
    public void compress(String inputPath, String outPath, @CompressType.Type String type, ICompressListener listener) {
        CompressorConfig compressType = new Config720pUpload();
        if(CompressType.TYPE_LOCAL_STORE.equals(type)){
            compressType = new ConfigLocalStore();
        }

        RxFFmpegInvoke.getInstance().runCommandAsync(compressType.buildCompressParams(inputPath, outPath), new RxFFmpegInvoke.IFFmpegListener() {
            @Override
            public void onFinish() {
                if(listener != null){
                    listener.onFinish(outPath);
                }
            }

            @Override
            public void onProgress(int progress, long progressTime) {
                if(listener != null){
                    listener.onProgress(progress,progressTime);
                }
            }

            @Override
            public void onCancel() {
                if(listener != null){
                    listener.onCancel();
                }
            }

            @Override
            public void onError(String message) {
                if(listener != null){
                    listener.onError(message);
                }
            }
        });
    }


    public static String calScaleStr(int targetResolution, String absolutePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(absolutePath);
        int rotation = VideoInfo.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));//视频的方向角度
        int width = VideoInfo.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)); //宽
        int height = VideoInfo.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)); //高
        String str  = "";
        if(rotation == 90 || rotation == 270){
            int tmp = width;
            width = height;
            height = tmp;
        }
        if(width <= height){
            if(width > targetResolution){
                str = targetResolution + ":-2";
            }

        }else {
            if(height > targetResolution){
                str = "-2:"+targetResolution;
            }
        }
        return str;
    }
}
