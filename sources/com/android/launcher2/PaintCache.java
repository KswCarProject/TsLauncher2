package com.android.launcher2;

import android.graphics.Paint;

/* compiled from: AppsCustomizePagedView */
class PaintCache extends WeakReferenceThreadLocal<Paint> {
    PaintCache() {
    }

    /* access modifiers changed from: protected */
    public Paint initialValue() {
        return null;
    }
}
