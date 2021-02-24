package com.hss01248.media.localvideoplayer;

import android.content.Context;
import android.content.Intent;

import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;

public class VideoPlayUtil {


    /**
     *
     * @param pathOrUri
     * @param useThirdPartyPlayer
     * @param dismissPageWhenFinishPlay
     */
    public static void startPreview(Context context, String pathOrUri, boolean useThirdPartyPlayer, boolean dismissPageWhenFinishPlay){
        //EXOPlayer内核，支持格式更多
        PlayerFactory.setPlayManager(SystemPlayerManager.class);
        if(useThirdPartyPlayer){
            //todo uri抛到外部
            return;
        }
        Intent intent = new Intent(context,PictureVideoPlayByGSYActivity.class);
        intent.putExtra(PictureVideoPlayByGSYActivity.PATH,pathOrUri);
        intent.putExtra(PictureVideoPlayByGSYActivity.TAG_DISMISSPAGEWHENFINISHPLAY,dismissPageWhenFinishPlay);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startPreviewInList(Context context, String pathOrUri, int sortType){
        //EXOPlayer内核，支持格式更多
        PlayerFactory.setPlayManager(SystemPlayerManager.class);

        Intent intent = new Intent(context,PictureVideoPlayByGSYActivity.class);
        intent.putExtra(PictureVideoPlayByGSYActivity.PATH,pathOrUri);
        intent.putExtra(PictureVideoPlayByGSYActivity.SORT_TYPE,sortType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
