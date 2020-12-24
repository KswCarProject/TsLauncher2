package com.android.launcher2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

class AllAppsList {
    public static final int DEFAULT_APPLICATIONS_NUMBER = 42;
    public ArrayList<ApplicationInfo> added = new ArrayList<>(42);
    public ArrayList<ApplicationInfo> data = new ArrayList<>(42);
    private IconCache mIconCache;
    public ArrayList<ApplicationInfo> modified = new ArrayList<>();
    public ArrayList<ApplicationInfo> removed = new ArrayList<>();

    public AllAppsList(IconCache iconCache) {
        this.mIconCache = iconCache;
    }

    public void add(ApplicationInfo info) {
        if (!findActivity(this.data, info.componentName)) {
            this.data.add(info);
            this.added.add(info);
        }
    }

    public void clear() {
        this.data.clear();
        this.added.clear();
        this.removed.clear();
        this.modified.clear();
    }

    public int size() {
        return this.data.size();
    }

    public ApplicationInfo get(int index) {
        return this.data.get(index);
    }

    public void addPackage(Context context, String packageName) {
        List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
        if (matches.size() > 0) {
            for (ResolveInfo info : matches) {
                add(new ApplicationInfo(context.getPackageManager(), info, this.mIconCache, (HashMap<Object, CharSequence>) null));
            }
        }
    }

    public void removePackage(String packageName) {
        List<ApplicationInfo> data2 = this.data;
        for (int i = data2.size() - 1; i >= 0; i--) {
            ApplicationInfo info = data2.get(i);
            if (packageName.equals(info.intent.getComponent().getPackageName())) {
                this.removed.add(info);
                data2.remove(i);
            }
        }
        this.mIconCache.flush();
    }

    public void updatePackage(Context context, String packageName) {
        List<ResolveInfo> matches = findActivitiesForPackage(context, packageName);
        if (matches.size() > 0) {
            for (int i = this.data.size() - 1; i >= 0; i--) {
                ApplicationInfo applicationInfo = this.data.get(i);
                ComponentName component = applicationInfo.intent.getComponent();
                if (packageName.equals(component.getPackageName()) && !findActivity(matches, component)) {
                    this.removed.add(applicationInfo);
                    this.mIconCache.remove(component);
                    this.data.remove(i);
                }
            }
            int count = matches.size();
            for (int i2 = 0; i2 < count; i2++) {
                ResolveInfo info = matches.get(i2);
                ApplicationInfo applicationInfo2 = findApplicationInfoLocked(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
                if (applicationInfo2 == null) {
                    add(new ApplicationInfo(context.getPackageManager(), info, this.mIconCache, (HashMap<Object, CharSequence>) null));
                } else {
                    this.mIconCache.remove(applicationInfo2.componentName);
                    this.mIconCache.getTitleAndIcon(applicationInfo2, info, (HashMap<Object, CharSequence>) null);
                    this.modified.add(applicationInfo2);
                }
            }
            return;
        }
        for (int i3 = this.data.size() - 1; i3 >= 0; i3--) {
            ApplicationInfo applicationInfo3 = this.data.get(i3);
            ComponentName component2 = applicationInfo3.intent.getComponent();
            if (packageName.equals(component2.getPackageName())) {
                this.removed.add(applicationInfo3);
                this.mIconCache.remove(component2);
                this.data.remove(i3);
            }
        }
    }

    private static List<ResolveInfo> findActivitiesForPackage(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        mainIntent.setPackage(packageName);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        return apps != null ? apps : new ArrayList<>();
    }

    private static boolean findActivity(List<ResolveInfo> apps, ComponentName component) {
        String className = component.getClassName();
        for (ResolveInfo info : apps) {
            if (info.activityInfo.name.equals(className)) {
                return true;
            }
        }
        return false;
    }

    private static boolean findActivity(ArrayList<ApplicationInfo> apps, ComponentName component) {
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            if (apps.get(i).componentName.equals(component)) {
                return true;
            }
        }
        return false;
    }

    private ApplicationInfo findApplicationInfoLocked(String packageName, String className) {
        Iterator<ApplicationInfo> it = this.data.iterator();
        while (it.hasNext()) {
            ApplicationInfo info = it.next();
            ComponentName component = info.intent.getComponent();
            if (packageName.equals(component.getPackageName()) && className.equals(component.getClassName())) {
                return info;
            }
        }
        return null;
    }
}
