package com.hss01248.media.localvideoplayer;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

public class LocalVideoPlayer extends StandardGSYVideoPlayer {
    private boolean isSilent;
    private int mStreamVolume;
    private ImageView mIvVoice;

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
    protected void init(Context context) {
        super.init(context);

        mIvVoice = findViewById(R.id.iv_voice);
        if (mAudioManager != null) {
            mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        mIvVoice.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAudioManager == null) {
                    return;
                }
                if (!isSilent) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0, 0);
                    isSilent = true;
                    mIvVoice.setImageResource(R.drawable.icon_video_voice);
                }else {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mStreamVolume, 0);
                    isSilent = false;
                    mIvVoice.setImageResource(R.drawable.icon_video_silent);
                }
            }
        });

    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_local;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        //离开页面后 回复声音
        if (mAudioManager != null && mIvVoice != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,mStreamVolume, 0);
            isSilent = false;
            mIvVoice.setImageResource(R.drawable.icon_video_silent);
        }
    }

    @Override
    protected void updateStartImage() {
        if(mStartButton instanceof ImageView) {
            ImageView imageView = (ImageView) mStartButton;
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                imageView.setImageResource(R.drawable.icon_video_pause);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(R.drawable.icon_video_play);
            } else {
                imageView.setImageResource(R.drawable.icon_video_play);
            }
        }
    }

    @Override
    public int getEnlargeImageRes() {
        return R.drawable.icon_video_zoom_out;
    }

    @Override
    public int getShrinkImageRes() {
        return R.drawable.icon_video_zoom_in;
    }

}
