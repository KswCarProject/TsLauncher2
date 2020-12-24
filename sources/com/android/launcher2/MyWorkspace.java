package com.android.launcher2;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import com.ts.main.common.ITsCommon;

public class MyWorkspace extends LibWorkspace {
    private static final String ACTION_NAVIBAR_DISMISS = "com.android.launcher.ACTION_NAVIBAR_DISMISS";
    private static final String ACTION_NAVIBAR_SHOW = "com.android.launcher.ACTION_NAVIBAR_SHOW";
    private static MyWorkspace myDefault = new MyWorkspace();
    private ActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public ITsCommon mCommService;
    private ServiceConnection mConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyWorkspace.this.mCommService = ITsCommon.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName name) {
            MyWorkspace.this.mCommService = null;
        }
    };
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            MyWorkspace.this.checkAllAppVisibility();
            MyWorkspace.this.mHandler.sendEmptyMessageDelayed(0, 200);
        }
    };
    private boolean mIsAllAppVisible = false;
    private String mLastClassName = "";
    private Launcher mLauncher;

    public static MyWorkspace GetInstance() {
        return myDefault;
    }

    public MyWorkspace() {
        userdef_widget_x = 0;
        userdef_widget_y = 0;
        userdef_widget_sx = 6;
        userdef_widget_sy = 1;
    }

    public void Scroll(CellLayout cl, float scrollProgress) {
        float rotation = scrollProgress * 90.0f;
        cl.setRotationY(rotation);
        cl.setCameraDistance(5000.0f);
        cl.setPivotX((rotation > 0.0f ? 0.8f : 0.2f) * ((float) cl.getMeasuredWidth()));
        cl.setPivotY(((float) cl.getMeasuredHeight()) * 0.2f);
        cl.setOverScrollAmount(Math.abs(scrollProgress), scrollProgress < 0.0f);
        cl.setOverscrollTransformsDirty(true);
    }

    /* access modifiers changed from: private */
    public void checkAllAppVisibility() {
        boolean isVisible = true;
        if (this.mLauncher != null) {
            ComponentName cn = this.mActivityManager.getRunningTasks(1).get(0).topActivity;
            String packageName = cn.getPackageName();
            String className = cn.getClassName();
            if ((!packageName.equals("com.ts.dvdplayer") || className.equals("com.ts.dvdplayer.USBActivity")) && !className.startsWith("com.ts.bt") && !className.equals("com.ts.main.radio.RadioMainActivity") && !className.equals("com.ts.can.benc.withcd.CanBencWithCDExdActivity")) {
                this.mLastClassName = className;
                if (!this.mLauncher.isAllAppsVisible() || !packageName.equals("com.android.launcher")) {
                    isVisible = false;
                }
                if (this.mIsAllAppVisible != isVisible) {
                    if (isVisible) {
                        Log.d("HAHA", "ACTION_NAVIBAR_SHOW");
                        this.mLauncher.sendBroadcast(new Intent(ACTION_NAVIBAR_SHOW));
                    } else {
                        Log.d("HAHA", "ACTION_NAVIBAR_DISMISS");
                        this.mLauncher.sendBroadcast(new Intent(ACTION_NAVIBAR_DISMISS));
                    }
                    this.mIsAllAppVisible = isVisible;
                }
            } else if (!this.mLastClassName.equals(className)) {
                Log.d("HAHA", String.valueOf(className) + " : ACTION_NAVIBAR_SHOW");
                if (!className.equals("com.ts.can.benc.withcd.CanBencWithCDExdActivity")) {
                    this.mLauncher.sendBroadcast(new Intent(ACTION_NAVIBAR_SHOW));
                }
                this.mIsAllAppVisible = true;
                this.mLastClassName = className;
            }
        }
    }

    public void setupViews(Launcher launcher) {
        this.mLauncher = launcher;
        bindCommonService();
        this.mActivityManager = (ActivityManager) this.mLauncher.getSystemService("activity");
        this.mHandler.sendEmptyMessage(0);
    }

    private void bindCommonService() {
        Intent intent = new Intent("android.intent.action.MAIN_UI");
        intent.setPackage("com.ts.MainUI");
        this.mLauncher.bindService(intent, this.mConn, 0);
    }

    public void sendPowerKey() {
        if (this.mCommService == null) {
            bindCommonService();
            return;
        }
        try {
            this.mCommService.SendMcuKey(6);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public int calPageNum() {
        return 1;
    }

    public void onBtnAllAppClick() {
        this.mLauncher.onClickAllAppsButton((View) null);
    }
}
