package com.luck.picture.lib.video;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;


import com.yalantis.ucrop.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;

import java.util.HashMap;
import java.util.Map;

import io.microshow.rxffmpeg.RxFFmpegCommandList;
import io.microshow.rxffmpeg.RxFFmpegInvoke;

public class RxVideoCompressor {

    public static void compress(Activity activity,String path){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(path);
                if(!file.exists()){
                    Log.w("dd","file not exist:"+path);
                    return;
                }
                long start = System.currentTimeMillis();
                long original = file.length();


                //SiliCompressor 画质很差
                //File out = new File(file.getParent(),file.getName().replace(".mp4","-compressed-sili.mp4"));
                try {
                    //compressByVideoCompressor(file,start,original);

                    runRx(file,System.currentTimeMillis(),activity);
                    //compressBySili(file,start,original);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    /**
     * 参数参考handbrake
     * @param file
     * @param out
     * @return
     *
     * ffmpeg -y -i /storage/emulated/0/1/input.mp4 -b 2097k -r 30 -vcodec libx264 -preset superfast /storage/emulated/0/1/result.mp4
     *
     * 40s 4k视频 原大小: 137M->10.69M 耗时60s 清晰度还不错
     */
    public static String[] getBoxblurForUpload( File file,File out) {
        RxFFmpegCommandList cmdlist = new RxFFmpegCommandList();
        cmdlist.append("-y");
        cmdlist.append("-i");
        cmdlist.append(file.getAbsolutePath());
        //关键在于码率的设置: 4k视频,推荐5000k
        //1080p视频,推荐2084k
        //cmdlist.append("-b");
        //cmdlist.append("6000k");
        //量化比例的范围为0～51，其中0为无损模式，23为缺省值，51可能是最差的
        //若Crf值加6，输出码率大概减少一半；若Crf值减6，输出码率翻倍
        cmdlist.append("-crf");
        cmdlist.append("32");
        //cmdlist.append("1500k");
        cmdlist.append("-r");
        cmdlist.append("24");
        //cmdlist.append("-vf");
        //cmdlist.append("scale=720:1080");
        cmdlist.append("-vcodec");
        cmdlist.append("libx264");
        cmdlist.append("-preset");
        cmdlist.append("ultrafast");
        cmdlist.append(out.getAbsolutePath());
        return cmdlist.build();
    }

    /**
     * r20-b3000 137M->15.1M 50s  3210bps
     * r20  137M->112.7M 67s  23483 kbs
     *
     * r20-b4096 137M->20M 55s
     *
     * r23-b4096 38M->6 14s
     *
     * 码率推荐; https://blog.csdn.net/benkaoya/article/details/79558896
     *
     * https://blog.csdn.net/rootusers/article/details/41646557
     *
     * https://blog.csdn.net/DONGHONGBAI/article/details/84776431
     *
     * https://blog.csdn.net/Martin_chen2/article/details/105772872
     *
     * crf vs b
     * @param file
     * @param out
     * @return
     */
    public static String[] getBoxblurForLocal( File file,File out) {
        RxFFmpegCommandList cmdlist = new RxFFmpegCommandList();
        cmdlist.append("-y");
        cmdlist.append("-i");
        cmdlist.append(file.getAbsolutePath());
        //关键在于码率的设置: 4k视频,推荐5000k
        //1080p视频,推荐2084k
        //cmdlist.append("-b");
        //cmdlist.append("6000k");
        //量化比例的范围为0～51，其中0为无损模式，23为缺省值，51可能是最差的
        //若Crf值加6，输出码率大概减少一半；若Crf值减6，输出码率翻倍
        cmdlist.append("-crf");
        cmdlist.append("26");
        //cmdlist.append("1500k");
        cmdlist.append("-r");
        cmdlist.append("30");
        //cmdlist.append("-vf");
        //cmdlist.append("scale=720:1080");
        cmdlist.append("-vcodec");
        cmdlist.append("libx264");
        cmdlist.append("-preset");
        cmdlist.append("superfast");
        cmdlist.append(out.getAbsolutePath());
        return cmdlist.build();
    }

    /**
     * videostream_codecpar_width=544
     * videostream_codecpar_height=960
     * videostream_avcodocname=h264
     *
     * videostream_nb_frames=880
     * bit_rate=1126.506000 kbs
     * videostream_duration=29333.333333
     *
     * videostream_avg_frame_rate=30.000000 fps
     * videostream_r_frame_rate=30.000000 fps
     * @param file
     * @return
     */
   public static Map<String,String> getMediaInfo(File file){
        Log.d("info","path:"+file.getAbsolutePath());
        String str = RxFFmpegInvoke.getInstance().getMediaInfo(file.getAbsolutePath());
        String[] strings = str.split(";");
        Map<String,String> map =  new HashMap<>();
        for (String string : strings) {
            if(!TextUtils.isEmpty(string)){
                Log.d("info",string);
                if(string.contains("=")){
                    String[] kv = string.split("=");
                    if(kv.length> 1){
                        map.put(kv[0],kv[1]);
                    }
                }
            }
        }
        return map;
    }

    public static String getKeyMediaInfo(String path){
       File file = new File(path);
        Map<String,String> map = getMediaInfo(file);
        StringBuilder stringBuilder = new StringBuilder();
        String duration = map.get("videostream_duration");
        if(duration.contains(".")){
            duration = duration.substring(0,duration.indexOf("."));
        }
        int dura = 0;
        String bitRate = "0" ;
        try {
            dura =  Integer.parseInt(duration)/1000;
            bitRate = (int) (file.length()/dura)/1024 +"kbps";
        }catch (Throwable throwable){
            throwable.printStackTrace();
            bitRate = map.get("bit_rate");
        }

        stringBuilder.append(file.getName())
                .append("\n")
                .append(size(file))
                .append("\n")
                .append(map.get("videostream_codecpar_width"))
                .append("x")
                .append(map.get("videostream_codecpar_height"))
                .append("\n")
                .append("bit_rate:")
                .append(bitRate)
                .append("\n")
                .append("len:")
                .append(dura)  .append("s") ;
        return stringBuilder.toString();
    }


    public static String getKeyInfo2(String path){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); //宽
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); //高
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);//视频的方向角度
        long duration = Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) /1000;//视频的长度

        File file = new File(path);
        StringBuilder stringBuilder = new StringBuilder();
      String  bitRate = (int) (file.length()/duration)/1024 +"kbps";
        stringBuilder.append(file.getName())
                .append("\n")
                .append(size(file))
                .append("\n")
                .append(width)
                .append("x")
                .append(height)
                .append(", rotation:")
                .append(rotation)
                .append("\n")
                .append("bit_rate:")
                .append(bitRate)
                .append("\n")
                .append("len:")
                .append(duration)  .append("s") ;
        return stringBuilder.toString();
    }

    private static String size(File file) {
        long len = file.length();
        String size = "";
        if(len> 1024*1024){
            size = len/1024/1024+"MB";
        }else {
            size = len/1024+"kB";
        }
        return size;
    }


    private static void runRx(File file, long start,Activity activity) {
        //getMediaInfo(file);
        File out = new File(file.getParent(),file.getName().replace(".mp4","-compressed-rx-r30-crf26.mp4"));

        //注意还需要判断下视频的旋转角度，不然也会crash
       // -vf scale=-1:720
        //横屏使用：scale=720:-1
        //竖屏使用：scale=-1:720
        //注：这里压成720P的分辨率了，具体可以自己设置
        /*String text = "ffmpeg -y -i " + file.getAbsolutePath() + " -b 2000k -r 23 -vcodec libx264  -preset superfast " + out.getAbsolutePath();

        String[] commands = text.split(" ");*/

        Handler handler  = new Handler(Looper.getMainLooper());

        final ProgressDialog[] dialog = {null};
        handler.post(new Runnable() {
            @Override
            public void run() {
                dialog[0] = new ProgressDialog(activity);
                dialog[0].setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog[0].setTitle("压缩中: "+file.getName());
                dialog[0].setMax(100);
                dialog[0].setCancelable(false);
                dialog[0].setCanceledOnTouchOutside(false);
                dialog[0].show();
            }
        });

        RxFFmpegInvoke.getInstance().runCommand(getBoxblurForLocal(file, out), new RxFFmpegInvoke.IFFmpegListener() {
            @Override
            public void onFinish() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(dialog[0] != null){
                            dialog[0].dismiss();
                        }
                        //showInfo(file,out,start,activity,handler);
                        CompressCompareActivity.start(activity,file.getAbsolutePath(),out.getAbsolutePath(),start);
                    }
                });
            }

            @Override
            public void onProgress(int progress, long progressTime) {
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
            public void onCancel() {

            }

            @Override
            public void onError(String message) {
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
        });
        //log(file,out,start);

       // Log.i("dd","info:"+RxFFmpegInvoke.getInstance().getMediaInfo(out.getAbsolutePath()));

        //major_brand=isom;minor_version=512;compatible_brands=isomiso2avc1mp41;encoder=Lavf58.12.100;
        // location-eng=+23.1319+114.4192/;location=+23.1319+114.4192/;
        // url=/storage/emulated/0/DCIM/Camera/VID_20210109_105150-compressed-rx-r23.mp4;
        // iformat_name=mov,mp4,m4a,3gp,3g2,mj2;iformat_long_name=(null);bit_rate=4169.863000 kbs;
        // duration=13142.000000 ms;filesize=201.16 KB;protocol_whitelist=file,crypto;protocol_blacklist=(null);
        // max_ts_probe=50;max_interleave_delta=10000000;max_picture_buffer=3041280 Bytes;
        // videostream_codecpar_codec_type=video;videostream_avcodocname=h264;videostream_profilestring=(null);
        // videostream_codec_fourcc=avc1;pix_fmt_name=yuv420p;videostream_nb_frames=302;videostream_codecpar_width=3840;
        // videostream_codecpar_height=2160;videostream_sample_aspect_ratio_num=1;videostream_sample_aspect_ratio_den=1;
        // display_aspect_ratio_num=16;display_aspect_ratio_den=9;videostream_r_frame_rate=23.000000 fps
        // ;videostream_avg_frame_rate=23.000000 fps;videostream_codecpar_bits_per_raw_sample=8 bits;
        // videostream_codecpar_bits_per_coded_sample=24 bits;videostream_codecpar_bit_rate=4041.615000 kbps;
        // videostream_time_base_num=1;videostream_time_base_den=11776;videostream_time_base=11776.000000;
        // videostream_duration=13131.029212;videostream_codec_time_base=46.000000;videostream_size=201.16 KB;
        // audiostream_codecpar_codec_type=audio;audiostream_size=201.16 KB;audiostream_duration=13120.000000 ms;
        // audiostream_codecpar_bit_rate=125.601000 kbps;audiostream_codecpar_sample_rate=48000 Hz;
        // audiostream_codecpar_channels=2;audiostream_avcodocname=aac;audiostream_profilestring=(null);
        // audiostream_codec_fourcc=mp4a
    }



    /**
     * 效果一般般
     * @param file
     * @param start
     * @param original
     * @throws Exception
     */
    private static void compressByVideoCompressor(File file, long start, long original) throws Exception{
        File out = new File(file.getParent(),file.getName().replace(".mp4","-compressed-3.mp4"));
       /* VideoProcessor.processor(PictureAppMaster.getInstance().getAppContext())
                .input(file.getAbsolutePath()) // .input(inputVideoUri)
                .output(out.getAbsolutePath())
                //.outWidth(1080)
                //.outHeight(1920)
                //以下参数全部为可选
               *//* .outWidth(width)
                .outHeight(height)
                .startTimeMs(startTimeMs)//用于剪辑视频
                .endTimeMs(endTimeMs)    //用于剪辑视频
                .speed(speed)            //改变视频速率，用于快慢放
                .changeAudioSpeed(changeAudioSpeed) //改变视频速率时，音频是否同步变化*//*
                .bitrate(1000)       //输出视频比特率
               // .frameRate(24)   //帧率 30 24  60
               // .iFrameInterval(iFrameInterval)  //关键帧距，为0时可输出全关键帧视频（部分机器上需为-1）
                //.progressListener(listener)      //可输出视频处理进度
                .process();
                log(file,out,start);*/
    }

    private static void log(File file, File out, long start) {
        Log.i("dd","original:"+file.getAbsolutePath());
        Log.i("dd","compressed:"+out.getAbsolutePath());
        Log.i("dd","cost:"+(System.currentTimeMillis() - start)/1000+"s");
        Log.i("dd","compressed file size:"+file.length()/1024/1024+"M->"+out.length()/1024.0f/1024.0f+"M");
    }

    private static void showInfo(File file, File out, long start, Activity activity, Handler handler) {


        StringBuilder sb = new StringBuilder("compressed:\n")
                .append(out.getAbsolutePath())
                .append("\n")
                .append("cost:"+(System.currentTimeMillis() - start)/1000+"s\n")
                .append("compressed file size:"+file.length()/1024/1024+"M->"+out.length()/1024.0f/1024.0f+"M")
                .append("\n").append("\n")
                .append(getKeyInfo2(out.getAbsolutePath()));
        AlertDialog dialog =    new AlertDialog.Builder(activity)
                .setTitle("压缩完成")
                .setMessage(sb.toString())
                .setCancelable(false)
                .setPositiveButton("覆盖原文件", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        FileUtils.copyFile(new FileInputStream(out),file.getAbsolutePath());
                                        out.delete();
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(activity,"覆盖成功",Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(activity,"覆盖失败:"+e.getMessage(),Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }

                                }
                            }).start();



                    }
                }).setNeutralButton("两个文件都保留", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("删除压缩的文件", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                out.delete();
            }
        }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }


    //SiliCompressor 画质很差
    private static void compressBySili(File file, long start, long original) throws Exception{
       /* String filePath = SiliCompressor.with(PictureAppMaster.getInstance().getAppContext()).compressVideo(file.getAbsolutePath(), file.getParent());
       log(file,new File(filePath),start);*/

    }



    public static  void refreshMediaCenter(Context activity, String filePath){
       /* File file  = new File(filePath);
        try {
            MediaStore.Images.Media.insertImage(activity.getContentResolver(),file.getAbsolutePath(), file.getName(), null);
        } catch (FileNotFoundException e) {
            ExceptionReporterHelper.reportException(e);
        }*/


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
}
