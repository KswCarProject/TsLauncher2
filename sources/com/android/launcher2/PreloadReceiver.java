package com.android.launcher2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class PreloadReceiver extends BroadcastReceiver {
    public static final String EXTRA_WORKSPACE_NAME = "com.android.launcher.action.EXTRA_WORKSPACE_NAME";
    private static final boolean LOGD = false;
    private static final String TAG = "Launcher.PreloadReceiver";

    public void onReceive(Context context, Intent intent) {
        final LauncherProvider provider = ((LauncherApplication) context.getApplicationContext()).getLauncherProvider();
        if (provider != null) {
            String name = intent.getStringExtra(EXTRA_WORKSPACE_NAME);
            final int workspaceResId = !TextUtils.isEmpty(name) ? context.getResources().getIdentifier(name, "xml", "com.android.launcher") : 0;
            new Thread(new Runnable() {
                public void run() {
                    provider.loadDefaultFavoritesIfNecessary(workspaceResId);
                }
            }).start();
        }
    }
}
