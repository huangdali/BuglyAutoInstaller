package com.hdl.bugly.autoinstaller.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hdl.bugly.autoinstaller.MainActivity;
import com.hdl.elog.ELog;


/**
 * 用于应用覆盖安装后启动主Activity的Receiver
 */
public class SysEventReceiver extends BroadcastReceiver {

    private final static String TAG = "SysEventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return;
        }
        switch (intent.getAction()) {
            case Intent.ACTION_PACKAGE_REPLACED:
                ELog.e("安装包被替换了");
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                ELog.e("安装包被替换了");
            case Intent.ACTION_BOOT_COMPLETED:
                ELog.e("重启完成");
                Intent mainIntent = new Intent(context, MainActivity.class);
                mainIntent.setAction(Intent.ACTION_MAIN);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                context.startActivity(mainIntent);
                break;
            default:
                break;
        }
    }
}
