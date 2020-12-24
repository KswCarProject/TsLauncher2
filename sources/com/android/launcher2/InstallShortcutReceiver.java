package com.android.launcher2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.Toast;
import com.android.launcher.R;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class InstallShortcutReceiver extends BroadcastReceiver {
    public static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final int INSTALL_SHORTCUT_IS_DUPLICATE = -1;
    private static final int INSTALL_SHORTCUT_NO_SPACE = -2;
    private static final int INSTALL_SHORTCUT_SUCCESSFUL = 0;
    public static final String NEW_APPS_LIST_KEY = "apps.new.list";
    public static final String NEW_APPS_PAGE_KEY = "apps.new.page";
    public static final int NEW_SHORTCUT_BOUNCE_DURATION = 450;
    public static final int NEW_SHORTCUT_STAGGER_DELAY = 75;
    public static final String SHORTCUT_MIMETYPE = "com.android.launcher/shortcut";
    private static ArrayList<PendingInstallShortcutInfo> mInstallQueue = new ArrayList<>();
    private static boolean mUseInstallQueue = false;

    private static class PendingInstallShortcutInfo {
        Intent data;
        Intent launchIntent;
        String name;

        public PendingInstallShortcutInfo(Intent rawData, String shortcutName, Intent shortcutIntent) {
            this.data = rawData;
            this.name = shortcutName;
            this.launchIntent = shortcutIntent;
        }
    }

    public void onReceive(Context context, Intent data) {
        Intent intent;
        boolean launcherNotLoaded = false;
        if (ACTION_INSTALL_SHORTCUT.equals(data.getAction()) && (intent = (Intent) data.getParcelableExtra("android.intent.extra.shortcut.INTENT")) != null) {
            String name = data.getStringExtra("android.intent.extra.shortcut.NAME");
            if (name == null) {
                try {
                    PackageManager pm = context.getPackageManager();
                    name = pm.getActivityInfo(intent.getComponent(), 0).loadLabel(pm).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    return;
                }
            }
            if (LauncherModel.getCellCountX() <= 0 || LauncherModel.getCellCountY() <= 0) {
                launcherNotLoaded = true;
            }
            PendingInstallShortcutInfo info = new PendingInstallShortcutInfo(data, name, intent);
            if (mUseInstallQueue || launcherNotLoaded) {
                mInstallQueue.add(info);
            } else {
                processInstallShortcut(context, info);
            }
        }
    }

    static void enableInstallQueue() {
        mUseInstallQueue = true;
    }

    static void disableAndFlushInstallQueue(Context context) {
        mUseInstallQueue = false;
        flushInstallQueue(context);
    }

    static void flushInstallQueue(Context context) {
        Iterator<PendingInstallShortcutInfo> iter = mInstallQueue.iterator();
        while (iter.hasNext()) {
            processInstallShortcut(context, iter.next());
            iter.remove();
        }
    }

    private static void processInstallShortcut(Context context, PendingInstallShortcutInfo pendingInfo) {
        SharedPreferences sp = context.getSharedPreferences(LauncherApplication.getSharedPreferencesKey(), 0);
        Intent data = pendingInfo.data;
        Intent intent = pendingInfo.launchIntent;
        String name = pendingInfo.name;
        int[] result = new int[1];
        boolean found = false;
        synchronized (((LauncherApplication) context.getApplicationContext())) {
            ArrayList<ItemInfo> items = LauncherModel.getItemsInLocalCoordinates(context);
            boolean exists = LauncherModel.shortcutExists(context, name, intent);
            for (int si = 0; si < Launcher.SCREEN_COUNT && !found; si++) {
                found = installShortcut(context, data, items, name, intent, si, exists, sp, result);
            }
        }
        if (found) {
            return;
        }
        if (result[0] == -2) {
            Toast.makeText(context, context.getString(R.string.completely_out_of_space), 0).show();
        } else if (result[0] == -1) {
            Toast.makeText(context, context.getString(R.string.shortcut_duplicate, new Object[]{name}), 0).show();
        }
    }

    private static boolean installShortcut(Context context, Intent data, ArrayList<ItemInfo> items, String name, Intent intent, int screen, boolean shortcutExists, SharedPreferences sharedPrefs, int[] result) {
        int[] tmpCoordinates = new int[2];
        if (!findEmptyCell(context, items, tmpCoordinates, screen)) {
            result[0] = -2;
        } else if (intent != null) {
            if (intent.getAction() == null) {
                intent.setAction("android.intent.action.VIEW");
            } else if (intent.getAction().equals("android.intent.action.MAIN") && intent.getCategories() != null && intent.getCategories().contains("android.intent.category.LAUNCHER")) {
                intent.addFlags(270532608);
            }
            if (data.getBooleanExtra("duplicate", true) || !shortcutExists) {
                int newAppsScreen = sharedPrefs.getInt(NEW_APPS_PAGE_KEY, screen);
                Set<String> newApps = new HashSet<>();
                if (newAppsScreen == screen) {
                    newApps = sharedPrefs.getStringSet(NEW_APPS_LIST_KEY, newApps);
                }
                synchronized (newApps) {
                    newApps.add(intent.toUri(0).toString());
                }
                final Set<String> set = newApps;
                final SharedPreferences sharedPreferences = sharedPrefs;
                final int i = screen;
                new Thread("setNewAppsThread") {
                    public void run() {
                        synchronized (set) {
                            sharedPreferences.edit().putInt(InstallShortcutReceiver.NEW_APPS_PAGE_KEY, i).putStringSet(InstallShortcutReceiver.NEW_APPS_LIST_KEY, set).commit();
                        }
                    }
                }.start();
                if (((LauncherApplication) context.getApplicationContext()).getModel().addShortcut(context, data, -100, screen, tmpCoordinates[0], tmpCoordinates[1], true) == null) {
                    return false;
                }
            } else {
                result[0] = -1;
            }
            return true;
        }
        return false;
    }

    private static boolean findEmptyCell(Context context, ArrayList<ItemInfo> items, int[] xy, int screen) {
        int xCount = LauncherModel.getCellCountX();
        int yCount = LauncherModel.getCellCountY();
        boolean[][] occupied = (boolean[][]) Array.newInstance(Boolean.TYPE, new int[]{xCount, yCount});
        for (int i = 0; i < items.size(); i++) {
            ItemInfo item = items.get(i);
            if (item.container == -100 && item.screen == screen) {
                int cellX = item.cellX;
                int cellY = item.cellY;
                int spanX = item.spanX;
                int spanY = item.spanY;
                int x = cellX;
                while (x >= 0 && x < cellX + spanX && x < xCount) {
                    int y = cellY;
                    while (y >= 0 && y < cellY + spanY && y < yCount) {
                        occupied[x][y] = true;
                        y++;
                    }
                    x++;
                }
            }
        }
        return CellLayout.findVacantCell(xy, 1, 1, xCount, yCount, occupied);
    }
}
