package com.hdl.bugly.autoinstaller.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * 工具类集合
 * Created by HDL on 2020/6/26.
 */

public class MyUtils {

    /**
     * 获取版本名，默认1.0.0
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "1.0.0";
        try {
            PackageInfo applicationInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = applicationInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 命令行重启系统
     */
    public static void reboot() {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("reboot\n");
            os.flush();
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
