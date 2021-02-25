package com.hss01248.media.localvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.solver.GoalRow;

import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import moe.codeest.enviews.ENPlayView;

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

    SeekBar seekBar;
    TextView tvPre;
    TextView tvNext;
    LinearLayout llSingle;
    LinearLayout llList;
    LinearLayout bottomLl;
    ENPlayView playView;

    public void setActivity(PictureVideoPlayByGSYActivity activity) {
        this.activity = activity;
    }

    PictureVideoPlayByGSYActivity activity;

    @Override
    protected void init(Context context) {
        super.init(context);
        seekBar = findViewById(R.id.progress);
        tvPre = findViewById(R.id.tv_play_pre);
        tvNext = findViewById(R.id.tv_play_next);
        llSingle = findViewById(R.id.bottom_single);
        llList = findViewById(R.id.bottom_list);
        bottomLl = findViewById(R.id.layout_bottom);
        playView = findViewById(R.id.start_list);


    }

    public void setVideoList(boolean videoList) {
        isVideoList = videoList;
        if(isVideoList){
            llSingle.setVisibility(GONE);
            llList.setVisibility(VISIBLE);
            preOrNext();
        }else {
            llSingle.setVisibility(VISIBLE);
            llList.setVisibility(GONE);
        }
    }

    private void preOrNext() {
        tvPre.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

               activity.onPlayPre();
            }
        });

        tvNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onPlayNext();
            }
        });

        playView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isVideoList){
                    if(playView.getCurrentState() == ENPlayView.STATE_PAUSE){
                        playView.play();
                        onVideoResume();

                    }else {
                        playView.pause();
                        onVideoPause();
                    }
                    //startPlayLogic();
                }
            }
        });
    }

    boolean isVideoList;

    @Override
    protected void changeUiToPreparingShow() {
        //super.changeUiToPreparingShow();
    }

    @Override
    public void startPlayLogic() {
        //super.startPlayLogic();
        if (mVideoAllCallBack != null) {
            Debuger.printfLog("onClickStartThumb");
            mVideoAllCallBack.onClickStartThumb(mOriginUrl, mTitle, LocalVideoPlayer.this);
        }
        prepareVideo();
    }



    @Override
    public int getLayoutId() {
        return R.layout.video_layout_local;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
}
