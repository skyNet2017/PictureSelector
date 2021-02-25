package com.hss01248.media.localvideoplayer;

import android.content.Context;
import android.content.Intent;

import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager;

import java.util.List;

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

    public static void startPreviewInList(Context context, List<String> sources, int currentPosition){
        //EXOPlayer内核，支持格式更多
        PlayerFactory.setPlayManager(SystemPlayerManager.class);
        PictureVideoPlayByGSYActivity.setVideos(sources);
        Intent intent = new Intent(context,PictureVideoPlayByGSYActivity.class);
        intent.putExtra(PictureVideoPlayByGSYActivity.PATH,sources.get(currentPosition));
        intent.putExtra(PictureVideoPlayByGSYActivity.POSITION,currentPosition);
        intent.putExtra(PictureVideoPlayByGSYActivity.IS_VIEW_LIST,true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
