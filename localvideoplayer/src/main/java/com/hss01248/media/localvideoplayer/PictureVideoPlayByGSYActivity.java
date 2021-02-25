package com.hss01248.media.localvideoplayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;


import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYStateUiListener;

import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;


import java.util.ArrayList;
import java.util.List;

import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_AUTO_COMPLETE;

public class PictureVideoPlayByGSYActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer> {
    LocalVideoPlayer detailPlayer;
    String videoPath;
    int position;

    public static final String PATH = "path";
    public static final String IS_VIEW_LIST = "isViewList";
    public static final String POSITION = "position";
    public static final String TAG_DISMISSPAGEWHENFINISHPLAY = "dismissPageWhenFinishPlay";

    boolean dismissPageWhenFinishPlay;
    boolean isViewList;

    public static List<String> getVideos() {
        return videos;
    }

    public static void setVideos(List<String> videos) {
        PictureVideoPlayByGSYActivity.videos.clear();
        PictureVideoPlayByGSYActivity.videos.addAll(videos);
    }

    static List<String> videos = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerFactory.setPlayManager(SystemPlayerManager.class);

        videoPath = getIntent().getStringExtra(PATH);
        dismissPageWhenFinishPlay = getIntent().getBooleanExtra(TAG_DISMISSPAGEWHENFINISHPLAY,false);
        position = getIntent().getIntExtra(POSITION,0);
        isViewList = getIntent().getBooleanExtra(IS_VIEW_LIST,false);

        setContentView(R.layout.activity_detail_player);
        detailPlayer = (LocalVideoPlayer) findViewById(R.id.detail_player);
        detailPlayer.setActivity(this);
        detailPlayer.setVideoList(isViewList);
        //增加title
        //detailPlayer.getTitleTextView().setVisibility(View.GONE);
        //detailPlayer.getBackButton().setVisibility(View.GONE);

        initVideoBuilderMode();


        try {
            //detailPlayer.getGSYVideoManager().start();
            //detailPlayer.getStartButton().setVisibility(View.GONE);
            detailPlayer.setDismissControlTime(2500);
            detailPlayer.startPlayLogic();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //hideSystemUI();
            //detailPlayer.startWindowFullscreen(this,false,false);
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    void onPlayNext(){

        if(!isViewList){
            return;
        }
        if(position >= videos.size()-1){
            return;
        }
        position++;
        videoPath = videos.get(position);

        getGSYVideoOptionBuilder().
                setVideoAllCallBack(this)
                .build(getGSYVideoPlayer());
        detailPlayer.startPlayLogic();
    }

    void onPlayPre(){

        if(!isViewList){
            return;
        }
        if(position <= 0){
            return;
        }
        position--;
        videoPath = videos.get(position);

        getGSYVideoOptionBuilder().
                setVideoAllCallBack(this)
                .build(getGSYVideoPlayer());
        detailPlayer.startPlayLogic();
    }


    @Override
    public StandardGSYVideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //内置封面可参考SampleCoverVideo
       // ImageView imageView = new ImageView(this);
        //loadCover(imageView, url);
        String uri = videoPath;
        if(videoPath.startsWith("/storage/")){
            uri = "file:// "+uri;
        }
        return new GSYVideoOptionBuilder()
                //.setThumbImageView(imageView)
                //.setUrl(url)
                .setUrl(uri)
                .setCacheWithPlay(false)
                .setShowFullAnimation(false)
                .setVideoTitle(getNameFromPath(videoPath))
        //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
                .setAutoFullWithSize(false)
                .setIsTouchWiget(true)
               // .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowPauseCover(false)
                .setStartAfterPrepared(true)
                .setShowFullAnimation(false)
                .setNeedLockFull(false)
                .setGSYStateUiListener(new GSYStateUiListener() {
                    @Override
                    public void onStateChanged(int state) {
                        if(state == CURRENT_STATE_AUTO_COMPLETE){
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(!isViewList){
                                        finish();
                                    }else {
                                        onPlayNext();
                                    }


                                }
                            },300);
                        }
                    }
                })
                /*.setGSYVideoProgressListener(new GSYVideoProgressListener() {
                    @Override
                    public void onProgress(int progress, int secProgress, int currentPosition, int duration) {
                        if(progress == 100 || progress == 99){
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            },500);

                        }
                    }
                })*/
                //.setThumbPlay(true)
                .setSeekRatio(1);
    }

    private String getNameFromPath(String videoPath) {
        if(videoPath.contains("/")){
            return videoPath.substring(videoPath.lastIndexOf("/")+1);
        }
        return videoPath;
    }

    @Override
    public void clickForFullScreen() {

    }

    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }
}
