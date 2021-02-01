package com.luck.pictureselector;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {
    static ExecutorService service;
    static Map<Integer,IDoInBackground> callbacks = new HashMap<>();

     static void setDoInBackground(int jobId, IDoInBackground doInBackground) {
        callbacks.put(jobId,doInBackground);
    }

    static IDoInBackground doInBackground;


    @Override
    public boolean onStartJob(final JobParameters params) {
        PersistableBundle bundle = params.getExtras();
        Log.d("job","MyJobService-onStartJob:"+bundle+", thread:"+Thread.currentThread().getName());
        if(callbacks.containsKey(params.getJobId())){
            callbacks.get(params.getJobId()).run(bundle);
            callbacks.remove(params.getJobId());
        }
        /*if(service == null){
            service = Executors.newSingleThreadExecutor();
        }
        service.execute(new Runnable() {
            @Override
            public void run() {
                if(callbacks.containsKey(params.getJobId())){
                    callbacks.get(params.getJobId()).run(bundle);
                    callbacks.remove(params.getJobId());
                }
            }
        });*/
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("job","MyJobService-onStopJob:");
        return true;//返回false表示停止后不再重试执行
    }



    public interface IDoInBackground{
        void run(PersistableBundle bundle);
    }








    private static ComponentName mServiceComponent;
    private static  int mJobId;
    static JobScheduler mJobScheduler;



    public static void doInBg(Context context,PersistableBundle bundle,IDoInBackground doInBackground) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//根据JobService创建一个ComponentName对象
            if(mServiceComponent == null){
                mServiceComponent = new ComponentName(context, MyJobService.class);
            }
            int jobId = mJobId++;
            JobInfo.Builder builder = new JobInfo.Builder(jobId, mServiceComponent);
            //builder.setMinimumLatency(500);//设置延迟调度时间
            //builder.setOverrideDeadline(2000);//设置该Job截至时间，在截至时间前肯定会执行该Job
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);//设置所需网络类型
            builder.setRequiresDeviceIdle(false);//设置在DeviceIdle时执行Job
            builder.setRequiresCharging(false);//设置在充电时执行Job
           /* PersistableBundle bundle = new PersistableBundle();
            bundle.putString("dir",dir.getAbsolutePath());
            bundle.putString("fileName",fileName);*/
            builder.setExtras(bundle);//设置一个额外的附加项

            JobInfo  mJobInfo = builder.build();

            if(mJobScheduler == null){
                mJobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            }
            MyJobService.setDoInBackground(jobId,doInBackground);

            mJobScheduler.schedule(mJobInfo);//调度Job
            /*mBuilder = new JobInfo.Builder(id,new ComponentName(this, MyJobService.class));

            JobInfo  mJobInfo = mBuilder.build();
            mJobScheduler.schedule(builder.build());//调度Job
            mJobScheduler.cancel(jobId);//取消特定Job
            mJobScheduler.cancelAll();//取消应用所有的Job*/
        }

    }
}