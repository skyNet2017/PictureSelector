package com.hss01248.media.localvideoplayer;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYStateUiListener;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_AUTO_COMPLETE;

public class PictureVideoPlayByGSYActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer> {
    StandardGSYVideoPlayer detailPlayer;
    String videoPath;
    int sortType;

    public static final String PATH = "path";
    public static final String SORT_TYPE = "sortType";
    public static final String TAG_DISMISSPAGEWHENFINISHPLAY = "dismissPageWhenFinishPlay";

    boolean dismissPageWhenFinishPlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlayerFactory.setPlayManager(SystemPlayerManager.class);

        setImmersiveStatusBar(false, getResources().getColor(R.color.black));


        videoPath = getIntent().getStringExtra(PATH);
        dismissPageWhenFinishPlay = getIntent().getBooleanExtra(TAG_DISMISSPAGEWHENFINISHPLAY,false);
        sortType = getIntent().getIntExtra(SORT_TYPE,0);

        setContentView(R.layout.activity_detail_player);
        detailPlayer = (StandardGSYVideoPlayer) findViewById(R.id.detail_player);
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
//        detailPlayer.getBackButton().setVisibility(View.GONE);
        initVideoBuilderMode();

        TextView tvCurrent = detailPlayer.findViewById(R.id.current);
        TextView tvDivider = detailPlayer.findViewById(R.id.divider);
        TextView tvTotal = detailPlayer.findViewById(R.id.total);
        tvCurrent.setTypeface(Typeface.createFromAsset(getAssets(),"roboto_medium.ttf"));
        tvDivider.setTypeface(Typeface.createFromAsset(getAssets(),"roboto_medium.ttf"));
        tvTotal.setTypeface(Typeface.createFromAsset(getAssets(),"roboto_medium.ttf"));

        try {
            //detailPlayer.getGSYVideoManager().start();
            //detailPlayer.getStartButton().setVisibility(View.GONE);
            detailPlayer.setDismissControlTime(2500);
            detailPlayer.startPlayLogic();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

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
//                .setVideoTitle(getNameFromPath(videoPath))
        //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
                .setAutoFullWithSize(false)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setHideKey(true)
                .setLockLand(false)
                .setShowPauseCover(false)
                .setStartAfterPrepared(true)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setCacheWithPlay(true)
                .setGSYStateUiListener(new GSYStateUiListener() {
                    @Override
                    public void onStateChanged(int state) {
                        if(state == CURRENT_STATE_AUTO_COMPLETE){
                            if (!dismissPageWhenFinishPlay) {
//                                detailPlayer.getCurrentPlayer().seekTo(1);
                                detailPlayer.onPrepared();
                                return;
                            }
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
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
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /**
     * 设置状态栏透明
     */
    public void setTranslucentStatus(int statusBarPlaceColor) {

        // 5.0以上系统状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(statusBarPlaceColor);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置沉浸式状态栏
     *
     * @param fontIconDark 状态栏字体和图标颜色是否为深色
     */
    public void setImmersiveStatusBar(boolean fontIconDark, int statusBarPlaceColor) {
        setTranslucentStatus(statusBarPlaceColor);
        if (fontIconDark) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setStatusBarFontIconDark(true);
            }
        }
    }

    /**
     * 设置Android状态栏的字体颜色，状态栏为亮色的时候字体和图标是黑色，状态栏为暗色的时候字体和图标为白色
     *
     * @param dark 状态栏字体是否为深色
     */
    public void setStatusBarFontIconDark(boolean dark) {
        // 小米MIUI
        try {
            Window window = getWindow();
            Class clazz = getWindow().getClass();
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {       //清除黑色字体
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
        } catch (Exception e) {
            //ExceptionReporterHelper.reportException(e);
        }

        // 魅族FlymeUI
        try {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
        } catch (Exception e) {
            //ExceptionReporterHelper.reportException(e);
        }
        // android6.0+系统
        // 这个设置和在xml的style文件中用这个<item name="android:windowLightStatusBar">true</item>属性是一样的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            int ui = decor.getSystemUiVisibility();
            if (dark) {
                //设置状态栏中字体的颜色为黑色
                ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                //设置状态栏中字体颜色为白色
                ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decor.setSystemUiVisibility(ui);
        }
    }

}
