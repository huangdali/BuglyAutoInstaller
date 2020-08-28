package com.hdl.bugly.autoinstaller.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.hdl.bugly.autoinstaller.utils.MyUtils;
import com.hdl.elog.ELog;
import com.tencent.bugly.beta.Beta;

import java.util.Calendar;

/**
 * 定时任务服务，此处采用监听android系统自带的分钟广播（每分钟会发送一次广播）来实现定时任务，减少资源占用
 *
 * @author HDL
 */
public class TimerTaskService extends Service {
    /**
     * 重启小时
     */
    private static final int REBOOT_TIME_HOUR = 3;
    /**
     * 重启分钟
     */
    private static final int REBOOT_TIME_MINUTE = 30;
    /**
     * 检测更新的时间间隔
     */
    private static final int SYNC_DATA_TIME = 10;


    public TimerTaskService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initTimePrompt();
    }

    /**
     * 整点报时
     */
    private void initTimePrompt() {
        IntentFilter timeFilter = new IntentFilter();
        //分钟广播，一分钟回调一次
        timeFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mTimeReceiver, timeFilter);
    }

    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            ELog.e("WatchDogService", "hour = " + hour);
            if (hour == REBOOT_TIME_HOUR && min == REBOOT_TIME_MINUTE) {
                ELog.e("到03：30了，准备重启系统");
                MyUtils.reboot();
            } else if (min % SYNC_DATA_TIME == 0) {
                //每10分钟同步一次数据
                ELog.e("每10分钟检测一次更新");
                Beta.checkAppUpgrade();
            }
        }
    };

    @Override
    public void onDestroy() {
        Intent sevice = new Intent(this, TimerTaskService.class);
        this.startService(sevice);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
