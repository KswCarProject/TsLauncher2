package com.android.launcher2;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.support.v4.content.IntentCompat;
import com.android.launcher.R;
import com.android.launcher2.LauncherSettings;
import com.yyw.ts90xhw.KeyDef;
import java.lang.ref.WeakReference;

public class LauncherApplication extends Application {
    private static boolean sIsScreenLarge = false;
    private static int sLongPressTimeout = KeyDef.RKEY_MEDIA_RDM;
    private static float sScreenDensity = 0.0f;
    private static final String sSharedPreferencesKey = "com.android.launcher2.prefs";
    private final ContentObserver mFavoritesObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            LauncherApplication.this.mModel.resetLoadedState(false, true);
            LauncherApplication.this.mModel.startLoaderFromBackground();
        }
    };
    public IconCache mIconCache;
    WeakReference<LauncherProvider> mLauncherProvider;
    public LauncherModel mModel;

    /* access modifiers changed from: protected */
    public void attachBaseContext(Context base) {
        MyWorkspace.GetInstance().SetContext(base);
        super.attachBaseContext(base);
    }

    public void onCreate() {
        super.onCreate();
        MyWorkspace.GetInstance().SetContext(this);
        Resources res = getResources();
        sIsScreenLarge = res.getBoolean(R.bool.is_large_screen);
        sScreenDensity = res.getDisplayMetrics().density;
        this.mIconCache = new IconCache(this);
        this.mModel = new LauncherModel(this, this.mIconCache);
        IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_CHANGED");
        filter.addDataScheme("package");
        registerReceiver(this.mModel, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter2.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter2.addAction("android.intent.action.LOCALE_CHANGED");
        filter2.addAction("android.intent.action.CONFIGURATION_CHANGED");
        registerReceiver(this.mModel, filter2);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("android.search.action.GLOBAL_SEARCH_ACTIVITY_CHANGED");
        registerReceiver(this.mModel, filter3);
        IntentFilter filter4 = new IntentFilter();
        filter4.addAction("android.search.action.SEARCHABLES_CHANGED");
        registerReceiver(this.mModel, filter4);
        getContentResolver().registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true, this.mFavoritesObserver);
    }

    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(this.mModel);
        getContentResolver().unregisterContentObserver(this.mFavoritesObserver);
    }

    /* access modifiers changed from: package-private */
    public LauncherModel setLauncher(Launcher launcher) {
        this.mModel.initialize(launcher);
        return this.mModel;
    }

    /* access modifiers changed from: package-private */
    public IconCache getIconCache() {
        return this.mIconCache;
    }

    /* access modifiers changed from: package-private */
    public LauncherModel getModel() {
        return this.mModel;
    }

    /* access modifiers changed from: package-private */
    public void setLauncherProvider(LauncherProvider provider) {
        this.mLauncherProvider = new WeakReference<>(provider);
    }

    /* access modifiers changed from: package-private */
    public LauncherProvider getLauncherProvider() {
        return (LauncherProvider) this.mLauncherProvider.get();
    }

    public static String getSharedPreferencesKey() {
        return sSharedPreferencesKey;
    }

    public static boolean isScreenLarge() {
        return sIsScreenLarge;
    }

    public static boolean isScreenLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    public static float getScreenDensity() {
        return sScreenDensity;
    }

    public static int getLongPressTimeout() {
        return sLongPressTimeout;
    }
}
