package com.hss01248.videocompress;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.hss01248.videocompress.listener.CompressLogListener;
import com.hss01248.videocompress.listener.ICompressListener;
import com.hss01248.videocompress.listener.PostProcessorListener;
import com.hss01248.videocompress.mediacodec.MediaCodecCompressImpl;

import java.io.File;




public class VideoCompressUtil {

   public static Context context;
   public static boolean showLog,showCompareAfterCompress,showGridInfo;

    public static void setiPreviewVideo(IPreviewVideo iPreviewVideo) {
        VideoCompressUtil.iPreviewVideo = iPreviewVideo;
    }

    public static IPreviewVideo iPreviewVideo;

    public static void init(Context context,boolean showLog,boolean showCompareAfterCompress){
        VideoCompressUtil.context = context;
        VideoCompressUtil.showLog = showLog;
        VideoCompressUtil.showCompareAfterCompress = showCompareAfterCompress;
    }
    public static void setCompressor(ICompressor compressor) {
        VideoCompressUtil.compressor = compressor;
    }

   static ICompressor compressor  = new MediaCodecCompressImpl();



    public static void doCompressAsync(String inputPath, @Nullable String outDir, @CompressType.Type String compressType, ICompressListener listener){

        File input = new File(inputPath);
        File dir = input.getParentFile();

        if(!TextUtils.isEmpty(outDir)){
            dir = new File(outDir);
        }

        String fileName = input.getName();
        if(fileName.contains(".")){
           int idx =  fileName.lastIndexOf(".");
           fileName = fileName.substring(0,idx)+"-"+compressType+fileName.substring(idx);
        }
        File out = new File(dir,fileName);

        String outPath = out.getAbsolutePath();
        //装饰器模式:
        listener = new PostProcessorListener(listener);
        if(VideoCompressUtil.showLog){
            listener = new CompressLogListener(listener);
        }

        if(listener != null){
            listener.onStart(inputPath,outPath);
        }

        VideoInfo.RealCompressInfo info = CompressHepler.getRealTargetWHBitrate(inputPath,compressType);
        if(!info.needCompress){
            Log.w("compress","无需压缩: 实际比特率和分辨率小于期望比特率");
            //无需压缩
            listener.onFinish(inputPath);
            return;
        }
        compressor.compress(info,inputPath,outPath,compressType,listener);

    }
}
