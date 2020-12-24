package com.android.launcher2;

import android.graphics.Canvas;

/* compiled from: AppsCustomizePagedView */
class CanvasCache extends WeakReferenceThreadLocal<Canvas> {
    CanvasCache() {
    }

    /* access modifiers changed from: protected */
    public Canvas initialValue() {
        return new Canvas();
    }
}
