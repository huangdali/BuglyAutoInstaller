package com.hdl.bugly.autoinstaller.base;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.hdl.elog.ELog;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.tencent.bugly.beta.upgrade.UpgradeStateListener;

import top.wuhaojie.installerlibrary.AutoInstaller;

public class MyApp extends Application {

    private AutoInstaller installer;

    @Override
    public void onCreate() {
        super.onCreate();
        initInstaller();
        initBugly();
    }

    private void initInstaller() {
        /* 方案一: 默认安装器 */
        installer = AutoInstaller.getDefault(this);
        installer.setOnStateChangedListener(new AutoInstaller.OnStateChangedListener() {
            @Override
            public void onStart() {
                ELog.e("开始");
            }

            @Override
            public void onComplete() {
                ELog.e("完成");
            }

            @Override
            public void onNeed2OpenService() {
                ELog.e("请打开辅助功能服务");
                Toast.makeText(getApplicationContext(), "请打开辅助功能服务", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 初始化bugly
     */
    private void initBugly() {
        //自动初始化
        Beta.autoInit = true;
        //wifi自动下载
        Beta.autoDownloadOnWifi = false;
        //自动检测更新
        Beta.autoCheckUpgrade = true;
        //延迟初始化时间
        Beta.initDelay = 5 * 1000;
        //自定义检测更新
        Beta.upgradeListener = (int ret, UpgradeInfo strategy, boolean isManual, boolean isSilence) -> {
            if (strategy != null) {
                ELog.e("MyApp", "onCreate: " + strategy);
                loadUpgradeInfo(strategy);
            } else {
                ELog.e("没有更新");
                Toast.makeText(MyApp.this, "没有更新", Toast.LENGTH_LONG).show();
            }
        };
        /* 设置更新状态回调接口 */
        Beta.upgradeStateListener = new UpgradeStateListener() {
            @Override
            public void onUpgradeSuccess(boolean isManual) {
//                ELog.e("更新成功");
                Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpgradeFailed(boolean isManual) {
                ELog.e("更新失败");
                Toast.makeText(getApplicationContext(), "更新失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpgrading(boolean isManual) {
                ELog.e("检测更新中  isManual = " + isManual);
                Toast.makeText(getApplicationContext(), "检测更新中", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadCompleted(boolean b) {
                ELog.e("下载完成 = b " + b);
                Toast.makeText(getApplicationContext(), "下载完成", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpgradeNoVersion(boolean isManual) {
                ELog.e("没有新版本 isManual = " + isManual);
                Toast.makeText(getApplicationContext(), "没有新版本", Toast.LENGTH_SHORT).show();
            }
        };
        Bugly.init(getApplicationContext(), "5706cac844", false);
    }

    /**
     * 加载更新信息
     *
     * @param upgradeInfo
     */
    private void loadUpgradeInfo(UpgradeInfo upgradeInfo) {
        StringBuilder info = new StringBuilder();
        info.append("id: ").append(upgradeInfo.id).append("\n");
        info.append("标题: ").append(upgradeInfo.title).append("\n");
        info.append("升级说明: ").append(upgradeInfo.newFeature).append("\n");
        info.append("versionCode: ").append(upgradeInfo.versionCode).append("\n");
        info.append("versionName: ").append(upgradeInfo.versionName).append("\n");
        info.append("发布时间: ").append(upgradeInfo.publishTime).append("\n");
        info.append("安装包Md5: ").append(upgradeInfo.apkMd5).append("\n");
        info.append("安装包下载地址: ").append(upgradeInfo.apkUrl).append("\n");
        info.append("安装包大小: ").append(upgradeInfo.fileSize).append("\n");
        info.append("弹窗间隔（ms）: ").append(upgradeInfo.popInterval).append("\n");
        info.append("弹窗次数: ").append(upgradeInfo.popTimes).append("\n");
        info.append("发布类型（0:测试 1:正式）: ").append(upgradeInfo.publishType).append("\n");
        info.append("弹窗类型（1:建议 2:强制 3:手工）: ").append(upgradeInfo.upgradeType).append("\n");
        info.append("图片地址：").append(upgradeInfo.imageUrl);
        Log.e("MyApp", "loadUpgradeInfo: " + info.toString());
        installer.installFromUrl(upgradeInfo.apkUrl);
    }
}
