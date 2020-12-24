package com.android.launcher2;

import java.lang.ref.WeakReference;

/* compiled from: AppsCustomizePagedView */
abstract class WeakReferenceThreadLocal<T> {
    private ThreadLocal<WeakReference<T>> mThreadLocal = new ThreadLocal<>();

    /* access modifiers changed from: package-private */
    public abstract T initialValue();

    public void set(T t) {
        this.mThreadLocal.set(new WeakReference(t));
    }

    public T get() {
        WeakReference<T> reference = this.mThreadLocal.get();
        if (reference == null) {
            T obj = initialValue();
            this.mThreadLocal.set(new WeakReference(obj));
            return obj;
        }
        T obj2 = reference.get();
        if (obj2 == null) {
            obj2 = initialValue();
            this.mThreadLocal.set(new WeakReference(obj2));
        }
        return obj2;
    }
}
