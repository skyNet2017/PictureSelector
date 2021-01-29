package com.hss01248.videocompress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;


import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;



public class CompressHepler {



    /**
     *
     * @param inputPath
     * @param targetResolution 目标最短边,比如720p,1080p ...
     * @return
     */
    public static VideoInfo.RealCompressInfo getRealTargetWHBitrate(String inputPath,  @CompressType.Type String compressType){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(inputPath);
        int originWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int originHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int bitrate = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));


        VideoInfo.RealCompressInfo info = new VideoInfo.RealCompressInfo();
        info.inputPath = inputPath;
        info.inputBitRate = bitrate;
        info.inputWidth = originWidth;
        info.inputHeight = originHeight;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            info.inputFrameCount = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
        }

        int targetResolution = 720;
        if(CompressType.TYPE_UPLOAD_720P.equals(compressType)){
            targetResolution = 720;
        }else if(CompressType.TYPE_UPLOAD_1080P.equals(compressType)){
            targetResolution = 1080;
        }else if(CompressType.TYPE_BILIBILI.equals(compressType)){
            targetResolution = 1080;
        }else if(CompressType.TYPE_LOCAL_STORE.equals(compressType)){
            targetResolution = Math.min(originWidth,originHeight);
        }


        calCompressConfig(info,targetResolution,originWidth,originHeight,bitrate,compressType);
        return info;
    }

    /**
     * 码率/分辨率 比例,线性拟合
     *  720p上传的 拟合数据源: 阿里云点播码率表  https://help.aliyun.com/document_detail/86068.html  y = 0.0018x - 545.63
     *
     *  本地收藏保存: y = 0.0018x + 1059.6
     * @param originWidth
     * @param originHeight
     * @return kbps
     */
    private static int getExpectedBitRate(int originWidth, int originHeight, @CompressType.Type String compressType) {
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

    /**
     *
     * @param targetResolution
     * @param inputWidth
     * @param inputHeight
     * @param originalBitrate
     * @param compressType
     * @return 返回是否需要压缩
     */
    private static boolean calCompressConfig(VideoInfo.RealCompressInfo info, int targetResolution,
                                      int inputWidth, int inputHeight, int originalBitrate, String compressType) {

        if(inputWidth < inputHeight){
            if(inputWidth >= targetResolution){
                float ratio = inputHeight*1.0f/inputWidth;
                int targetHeight = targetResolution*inputHeight/inputWidth;
                //todo
                int expetedRatesInkps = getExpectedBitRate(targetResolution,targetHeight,compressType)*1024;
                int bitRates = getBitRate(expetedRatesInkps,originalBitrate,ratio);
                Log.w("dd","bitrates cal to compress:"+bitRates/1024);
                info.outWidth = targetResolution;
                        info.outHeight = targetHeight;
                        info.outBitRate = bitRates;
            }else {
                int expetedRatesInkps = getExpectedBitRate(inputWidth,inputHeight,compressType)*1024;
                if(originalBitrate > expetedRatesInkps){
                    info.outWidth = inputWidth;
                    info.outHeight = inputHeight;
                    info.outBitRate = expetedRatesInkps;
                }else {
                    //不需要压缩
                    info.needCompress = false;
                    return false;
                }
            }
        }else {
            if(inputHeight >= targetResolution){
                float ratio = inputWidth*1.0f/inputHeight;
                int targetW = targetResolution*inputWidth/inputHeight;
                int expetedRatesInkps = getExpectedBitRate(targetResolution,targetW,compressType)*1024;
                int bitRates = getBitRate(expetedRatesInkps,originalBitrate,ratio);
                Log.w("dd","bitrates cal to compress:"+bitRates/1024);
                info.outWidth = targetW;
                info.outHeight = targetResolution;
                info.outBitRate = bitRates;
            }else {
                //不需要压缩分辨率,就看看要不要减少码率
                int expetedRatesInkps = getExpectedBitRate(inputWidth,inputHeight,compressType)*1024;
                if(originalBitrate > expetedRatesInkps){
                    info.outWidth = inputWidth;
                    info.outHeight = inputHeight;
                    info.outBitRate = originalBitrate;
                }else {
                    //不需要压缩
                    info.needCompress = false;
                    return false;
                }
            }
        }
        return true;
    }

    private static int getBitRate(int expetedRatesInkps, int originalBitrate, float ratio) {
        if(originalBitrate/ratio > expetedRatesInkps){
            return expetedRatesInkps;
        }else if(expetedRatesInkps > originalBitrate){
            return originalBitrate;
        }else {
            return (int) (originalBitrate/ratio);
        }
    }



    public static  void refreshMediaCenter(Context activity, String filePath){
        if (Build.VERSION.SDK_INT>19){
            String mineType =getMineType(filePath);

            saveImageSendScanner(activity,new MyMediaScannerConnectionClient(filePath,mineType));
        }else {

            saveImageSendBroadcast(activity,filePath);
        }
    }

    public static String getMineType(String filePath) {

        String type = "text/plain";
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;


       /* MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "text/plain";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (IllegalStateException e) {
                return mime;
            } catch (IllegalArgumentException e) {
                return mime;
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;*/
    }

    /**
     * 保存后用广播扫描，Android4.4以下使用这个方法
     * @author YOLANDA
     */
    private static void saveImageSendBroadcast(Context activity, String filePath){
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
    }

    /**
     * 保存后用MediaScanner扫描，通用的方法
     *
     */
    private static void saveImageSendScanner (Context context, MyMediaScannerConnectionClient scannerClient) {

        final MediaScannerConnection scanner = new MediaScannerConnection(context, scannerClient);
        scannerClient.setScanner(scanner);
        scanner.connect();
    }
    private   static class MyMediaScannerConnectionClient implements MediaScannerConnection.MediaScannerConnectionClient {

        private MediaScannerConnection mScanner;

        private String mScanPath;
        private String mimeType;

        public MyMediaScannerConnectionClient(String scanPath, String mimeType) {
            mScanPath = scanPath;
            this.mimeType = mimeType;
        }

        public void setScanner(MediaScannerConnection con) {
            mScanner = con;
        }

        @Override
        public void onMediaScannerConnected() {
            mScanner.scanFile(mScanPath, mimeType);
        }

        @Override
        public void onScanCompleted(String path, Uri uri) {
            mScanner.disconnect();
        }
    }

    public static void copyFile(@NonNull String pathFrom, @NonNull String pathTo) throws IOException {
        if (pathFrom.equalsIgnoreCase(pathTo)) {
            return;
        }

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            inputChannel = new FileInputStream(new File(pathFrom)).getChannel();
            outputChannel = new FileOutputStream(new File(pathTo)).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }
    }
}
