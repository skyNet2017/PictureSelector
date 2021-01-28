package com.luck.picture.lib.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hss01248.videocompress.CompressHepler;
import com.hss01248.videocompress.VideoInfo;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.tools.JumpUtils;
import com.yalantis.ucrop.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;

public class CompressCompareActivity extends AppCompatActivity {

    public static void start(Activity activity,String originalFile,String compressedFile,long startTime){
        Intent intent = new Intent(activity,CompressCompareActivity.class);
        intent.putExtra("originalFile",originalFile);
        intent.putExtra("compressedFile",compressedFile);
        intent.putExtra("startTime",startTime);
        activity.startActivity(intent);

    }

    String originalFile, compressedFile;
    ImageView iv1,iv2;
    TextView tv1,tv2,tv_title;
    VideoInfo info1,info2;
    long startTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        originalFile = getIntent().getStringExtra("originalFile");
        compressedFile = getIntent().getStringExtra("compressedFile");
        startTime = getIntent().getLongExtra("startTime",0);
        setContentView(R.layout.video_activity_compare);
        iv1 = findViewById(R.id.iv_1);
        iv2 = findViewById(R.id.iv_2);
        tv1 = findViewById(R.id.tv_1);
        tv2 = findViewById(R.id.tv_2);
        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(tv_title.getText()+"\n压缩耗时:"+(System.currentTimeMillis() - startTime)/1000+"s");

        if (PictureSelectionConfig.imageEngine != null) {
            PictureSelectionConfig.imageEngine.loadGridImage(this, originalFile, iv1);
            PictureSelectionConfig.imageEngine.loadGridImage(this, compressedFile, iv2);
        }
        info1 = VideoInfo.getInfo(originalFile);
        info2 = VideoInfo.getInfo(compressedFile);
        tv1.setText(info1.toString());
        tv2.setText(info2.toString());


    }

    public void replace(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtils.copyFile(new FileInputStream(compressedFile),originalFile);
                    new File(compressedFile).delete();
                    CompressHepler.refreshMediaCenter(getApplication(),originalFile);
                    toast("覆盖成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    toast("覆盖失败:"+e.getMessage());

                }

            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if(new File(compressedFile).exists()){
            CompressHepler.refreshMediaCenter(getApplication(),compressedFile);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if(new File(compressedFile).exists()){
            CompressHepler.refreshMediaCenter(getApplication(),compressedFile);
        }
        super.onDestroy();
    }

    public void cancel(View view) {
        new File(compressedFile).delete();
        toast("已删除压缩的文件");
    }

    public void keepBoth(View view) {
        CompressHepler.refreshMediaCenter(getApplication(),compressedFile);
        toast("ok");
    }

    private void toast(String ok) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CompressCompareActivity.this,ok,Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void view1(View view) {
        JumpUtils.previeVideo(this,originalFile);

    }

    public void view2(View view) {
        JumpUtils.previeVideo(this,compressedFile);
    }
}
