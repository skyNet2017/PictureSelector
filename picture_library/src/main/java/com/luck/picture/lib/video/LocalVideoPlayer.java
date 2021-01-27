package com.luck.picture.lib.video;

import android.content.Context;
import android.util.AttributeSet;

import com.luck.picture.lib.R;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

public class LocalVideoPlayer extends StandardGSYVideoPlayer {
    public LocalVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public LocalVideoPlayer(Context context) {
        super(context);
    }

    public LocalVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_local;
    }
}
