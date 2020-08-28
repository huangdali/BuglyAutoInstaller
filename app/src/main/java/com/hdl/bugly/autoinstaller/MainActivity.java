package com.hdl.bugly.autoinstaller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.hdl.bugly.autoinstaller.service.TimerTaskService;
import com.hdl.bugly.autoinstaller.utils.MyUtils;
import com.hdl.elog.ELog;
import com.tencent.bugly.beta.Beta;

import hdl.com.lib.runtimepermissions.HPermissions;
import hdl.com.lib.runtimepermissions.PermissionsResultAction;

public class MainActivity extends AppCompatActivity {

    private TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
        startService(new Intent(this, TimerTaskService.class));
        tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText(String.format("V%s", MyUtils.getVersionName(this)));
    }

    /**
     * 请求权限[如不是手机端app，可以考虑将app/build.gradle->targetSdkVersion设置为22或以下，这样就可以不用请求权限]
     */
    private void requestPermission() {
        HPermissions.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
                ELog.e("授权通过");
            }

            @Override
            public void onDenied(String permission) {
                ELog.e("授权拒绝");
            }
        });
    }

    /**
     * 手动点击检测更新，一般不使用，由于bugly的自动检测机制比较慢，对于需要短时间内实现升级的场景，可写定时任务去调用 {@link Beta.checkAppUpgrade();} ，参考 {@link com.hdl.bugly.autoinstaller.service.TimerTaskService} 实现
     *
     * @param view
     */
    public void checkUpdate(View view) {
        Beta.checkAppUpgrade();
    }
}