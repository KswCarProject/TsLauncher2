package com.android.launcher2;

import android.graphics.Bitmap;

/* compiled from: AppsCustomizePagedView */
class BitmapCache extends WeakReferenceThreadLocal<Bitmap> {
    BitmapCache() {
    }

    /* access modifiers changed from: protected */
    public Bitmap initialValue() {
        return null;
    }
}
