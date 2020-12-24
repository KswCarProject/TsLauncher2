package com.android.launcher2;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.android.launcher.R;
import java.util.HashMap;

public class IconCache {
    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;
    private static final String TAG = "Launcher.IconCache";
    private Class<R.drawable> cls = R.drawable.class;
    private final HashMap<ComponentName, CacheEntry> mCache = new HashMap<>(50);
    private final LauncherApplication mContext;
    private final Bitmap mDefaultIcon;
    private int mIconDpi;
    private final PackageManager mPackageManager;

    private static class CacheEntry {
        public Bitmap icon;
        public String title;

        private CacheEntry() {
        }

        /* synthetic */ CacheEntry(CacheEntry cacheEntry) {
            this();
        }
    }

    public IconCache(LauncherApplication context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        this.mIconDpi = ((ActivityManager) context.getSystemService("activity")).getLauncherLargeIconDensity();
        this.mDefaultIcon = makeDefaultIcon();
    }

    public Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(), 17629184);
    }

    public Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawableForDensity(iconId, this.mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }
        return d != null ? d : getFullResDefaultActivityIcon();
    }

    public Drawable getFullResIcon(String packageName, int iconId) {
        Resources resources;
        try {
            resources = this.mPackageManager.getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources == null || iconId == 0) {
            return getFullResDefaultActivityIcon();
        }
        return getFullResIcon(resources, iconId);
    }

    public Drawable getFullResIcon(ResolveInfo info) {
        return getFullResIcon(info.activityInfo);
    }

    public Drawable getFullResIcon(ActivityInfo info) {
        Resources resources;
        int iconId;
        try {
            resources = this.mPackageManager.getResourcesForApplication(info.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources == null || (iconId = info.getIconResource()) == 0) {
            return getFullResDefaultActivityIcon();
        }
        return getFullResIcon(resources, iconId);
    }

    private Bitmap makeDefaultIcon() {
        Drawable d = getFullResDefaultActivityIcon();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1), Math.max(d.getIntrinsicHeight(), 1), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap((Bitmap) null);
        return b;
    }

    public void remove(ComponentName componentName) {
        synchronized (this.mCache) {
            this.mCache.remove(componentName);
        }
    }

    public void flush() {
        synchronized (this.mCache) {
            this.mCache.clear();
        }
    }

    public void getTitleAndIcon(ApplicationInfo application, ResolveInfo info, HashMap<Object, CharSequence> labelCache) {
        synchronized (this.mCache) {
            CacheEntry entry = cacheLocked(application.componentName, info, labelCache);
            application.title = entry.title;
            application.iconBitmap = entry.icon;
        }
    }

    public Bitmap getIcon(Intent intent) {
        Bitmap bitmap;
        synchronized (this.mCache) {
            ResolveInfo resolveInfo = this.mPackageManager.resolveActivity(intent, 0);
            ComponentName component = intent.getComponent();
            if (resolveInfo == null || component == null) {
                bitmap = this.mDefaultIcon;
            } else {
                bitmap = cacheLocked(component, resolveInfo, (HashMap<Object, CharSequence>) null).icon;
            }
        }
        return bitmap;
    }

    public Bitmap getIcon(ComponentName component, ResolveInfo resolveInfo, HashMap<Object, CharSequence> labelCache) {
        synchronized (this.mCache) {
            if (resolveInfo == null || component == null) {
                return null;
            }
            Bitmap bitmap = cacheLocked(component, resolveInfo, labelCache).icon;
            return bitmap;
        }
    }

    public boolean isDefaultIcon(Bitmap icon) {
        return this.mDefaultIcon == icon;
    }

    private CacheEntry cacheLocked(ComponentName componentName, ResolveInfo info, HashMap<Object, CharSequence> labelCache) {
        Drawable mBackground;
        CacheEntry entry = this.mCache.get(componentName);
        if (entry == null) {
            entry = new CacheEntry((CacheEntry) null);
            this.mCache.put(componentName, entry);
            ComponentName key = LauncherModel.getComponentNameFromResolveInfo(info);
            if (labelCache == null || !labelCache.containsKey(key)) {
                entry.title = info.loadLabel(this.mPackageManager).toString();
                if (labelCache != null) {
                    labelCache.put(key, entry.title);
                }
            } else {
                entry.title = labelCache.get(key).toString();
            }
            if (entry.title == null) {
                entry.title = info.activityInfo.name;
            }
            if (entry.title.startsWith("Test Version")) {
                entry.title = "DVR";
            }
            Log.d(TAG, "ClassName = " + componentName.getClassName().toLowerCase().replace('.', '_'));
            Resources res = this.mContext.getResources();
            if (res.getInteger(R.integer.myiconother) == 1) {
                try {
                    mBackground = res.getDrawable(this.cls.getDeclaredField(componentName.getClassName().toLowerCase().replace('.', '_')).getInt((Object) null));
                } catch (Exception e) {
                    mBackground = getFullResIcon(info);
                }
            } else {
                mBackground = getFullResIcon(info);
            }
            int iconbacks = res.getInteger(R.integer.iconbacks);
            if (iconbacks > 0) {
                String str = componentName.getClassName().toLowerCase();
                int id = res.getIdentifier(String.format("icon_back%02d", new Object[]{Integer.valueOf((calculateColour(str.substring(str.lastIndexOf(46) + 1)) % (iconbacks - 1)) + 1)}), "drawable", this.mContext.getPackageName());
                Bitmap iconBack = null;
                if (id != 0) {
                    iconBack = BitmapFactory.decodeResource(res, id);
                }
                entry.icon = Utilities.createIconBitmap(mBackground, this.mContext, iconBack);
            } else {
                entry.icon = Utilities.createIconBitmap(mBackground, (Context) this.mContext);
            }
        }
        return entry;
    }

    private int calculateColour(String str) {
        int value = 0;
        for (byte b : str.getBytes()) {
            value ^= b;
        }
        return value;
    }

    public HashMap<ComponentName, Bitmap> getAllIcons() {
        HashMap<ComponentName, Bitmap> set;
        synchronized (this.mCache) {
            set = new HashMap<>();
            for (ComponentName cn : this.mCache.keySet()) {
                set.put(cn, this.mCache.get(cn).icon);
            }
        }
        return set;
    }
}
