package com.luck.picture.lib.video;

import android.media.MediaMetadataRetriever;

import androidx.annotation.Keep;

import com.luck.picture.lib.tools.ValueOf;

import java.io.File;


@Keep
public class VideoInfo {
    public String name;
    public String path;
    public int height;
    public int width;
    public int rotation;
    public long fileLength;
    public int duration;
    public int bitRates;
    public int quality;


    public static VideoInfo getInfo(String path) {
        VideoInfo info = new VideoInfo();
        info.path = path;
        File file = new File(path);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        info.width = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)); //宽
        info.height = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)); //高
        info.rotation = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));//视频的方向角度
        info.duration = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;//视频的长度
        info.bitRates = ValueOf.toInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)) / 1024;
        if (info.bitRates <= 0) {
            info.bitRates = (int) (file.length() / info.duration) / 1024;
        }
        info.name = file.getName();
        info.fileLength = file.length();

        return info;
    }

    @Override
    public String toString() {
        return
                "wh=" + width +
                        "x" + height +
                        "\nrotation=" + rotation +
                        "\nfileLength=" + size(fileLength) +
                        "\nduration=" + duration +
                        "s\nbitRates=" + bitRates +
                        "kbps\nfile=" +name +
                        "\npath=" + path;
    }

    private static String size(long len) {
        String size = "";
        if (len > 1024 * 1024) {
            size = len / 1024 / 1024 + "MB";
        } else {
            size = len / 1024 + "kB";
        }
        return size;
    }

}
