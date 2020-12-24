package com.android.launcher2;

import android.graphics.Rect;

/* compiled from: AppsCustomizePagedView */
class RectCache extends WeakReferenceThreadLocal<Rect> {
    RectCache() {
    }

    /* access modifiers changed from: protected */
    public Rect initialValue() {
        return new Rect();
    }
}
