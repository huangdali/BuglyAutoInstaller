## 使用Bugly实现静默安装/自动安装app

### 前言

针对门禁、闸机、广告机等无人值守的Android终端app，如何自动升级一直是一个比较麻烦的事情；现在给出一个相对比较成熟的方案供大家参考；

以前你的升级方案可能是这样的：

- 集成bugly，发布新版本时由工作人员到现场手动检测更新
- 自建应用升级后台管理，发布新版本时由工作人员到现场手动检测更新
- 对于有root权限的，通过shell命令实现静默安装app
- 第三种和第二种的结合
- U盘安装

第四种方式基本可以实现后端发布升级，终端机自动检测更新（或通过推送实现）并自动更新app，实测中你可能发现该方式并没有那么稳定，总是有那么些设备未能升级成功；或者针对没有root的设备没法实现此功能。


### 推荐的方案

#### AutoInstaller

推荐之前先来看看一个非常实用的开源项目 [AutoInstaller](https://github.com/a-voyager/AutoInstaller) ，AutoInstaller 一个可以让您应用的自动更新功能更加优雅的静默安装库 !

特性：

- 只需要一行代码搞定您应用的后台静默下载和静默（自动）安装  【AutoInstaller.getDefault(this).install(APK_FILE_PATH);】
- 两种自动安装方式: ROOT静默安装 和 辅助功能自动模拟点击安装 ，并且能够自动选择可用方式

![image](https://github.com/a-voyager/AutoInstaller/raw/master/imgs/GIF.gif)

#### Bugly+AutoInstaller

使用Bugly的好处不言而喻，方便发布版本，还有热修复功能，集成简单；不用自己写升级管理后台，还有完善的错误日志收集；但是Bugly是不支持自动升级的，需要用户点击升级后方可下载安装，并且安装完成后需要用户点击打开才可以实现打开新版应用；

那是否可以实现在Bugly的后台发布更新包，通过AutoInstaller自动安装app呢？答案是肯定的，只需要将Bugly的更新默认设置成自定义模式，终端自动检测bugly的升级策略，发现有新策略时将apk的下载地址传给AutoInstaller实现自动下载安装apk。

是不是很完美；不管你是root过的设备还是没有root过的设备，基本都能实现自动升级（静默安装）；

到这里还没有完，AutoInstaller只能实现静默安装，安装完成后是无法自动打开app的，还需要处理一下，下面提供两种解决方案：

- 监听应用被覆盖安装的广播，然后打开app；
- 开发一个看门狗软件，主程序定时向看门狗软件发送心跳（通过aidl通讯），看门狗软件定时检测心跳超时时间，超时了就重启系统；

稳妥一点的话可以两种方式结合使用；本demo暂时只实现第一种方式，如你需要看门狗的方式欢迎留言，后期我会贴出看门狗的源码。

实现步骤：

添加相关依赖  app/build.gradle

```groovy
//静默安装
    implementation 'com.github.a-voyager:AutoInstaller:v1.0'
    //bugly
    implementation 'com.tencent.bugly:crashreport_upgrade:latest.release'
    implementation 'com.tencent.bugly:nativecrashreport:latest.release'
```

配置权限 AndroidManifest.xml

```xml
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.READ_LOGS" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

配置bugly的activity

```xml
    <activity
            android:name="com.tencent.bugly.beta.ui.BetaActivity"
            android:configChanges="keyboardHidden|orientation|screenSize|locale"
            android:theme="@android:style/Theme.Translucent" />
```
程序启动类

```xml
    <application
            android:name=".base.MyApp"
    ...>
    </application>
```

MyApp具体代码

```java
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

```

安装完成后的监听---打开app   

SysEventReceiver
```java
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

```

 SysEventReceiver记得在AndroidManifest.xml中注册

```xml

    <!--接收覆盖安装事件的Receiver-->
        <receiver
            android:name=".receiver.SysEventReceiver"
            android:enabled="true"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.PACKAGE_REPLACED" />
                <data android:scheme="package" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <category android:name="android.intent.category.HOME" />
            </intent-filter>
        </receiver>

```

定时检测更新服务 TimerTaskService

```java
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

```

至此基本实现发布升级后Android终端机自动更新/静默更新


#### 自建升级后台管理平台+AutoInstaller

此种方式跟上一方案思路是一致的，自建升级后台管理平台比bugly的方式扩展性更高，当然缺点就是要自己开发，本文不做展开，不懂的可以留言。