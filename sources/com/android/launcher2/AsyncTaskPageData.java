package com.android.launcher2;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.Iterator;

/* compiled from: AppsCustomizePagedView */
class AsyncTaskPageData {
    AsyncTaskCallback doInBackgroundCallback;
    ArrayList<Bitmap> generatedImages;
    ArrayList<Object> items;
    int maxImageHeight;
    int maxImageWidth;
    int page;
    AsyncTaskCallback postExecuteCallback;
    ArrayList<Bitmap> sourceImages;

    /* compiled from: AppsCustomizePagedView */
    enum Type {
        LoadWidgetPreviewData
    }

    AsyncTaskPageData(int p, ArrayList<Object> l, ArrayList<Bitmap> si, AsyncTaskCallback bgR, AsyncTaskCallback postR) {
        this.page = p;
        this.items = l;
        this.sourceImages = si;
        this.generatedImages = new ArrayList<>();
        this.maxImageHeight = -1;
        this.maxImageWidth = -1;
        this.doInBackgroundCallback = bgR;
        this.postExecuteCallback = postR;
    }

    AsyncTaskPageData(int p, ArrayList<Object> l, int cw, int ch, AsyncTaskCallback bgR, AsyncTaskCallback postR) {
        this.page = p;
        this.items = l;
        this.generatedImages = new ArrayList<>();
        this.maxImageWidth = cw;
        this.maxImageHeight = ch;
        this.doInBackgroundCallback = bgR;
        this.postExecuteCallback = postR;
    }

    /* access modifiers changed from: package-private */
    public void cleanup(boolean cancelled) {
        if (this.sourceImages != null) {
            if (cancelled) {
                Iterator<Bitmap> it = this.sourceImages.iterator();
                while (it.hasNext()) {
                    it.next().recycle();
                }
            }
            this.sourceImages.clear();
        }
        if (this.generatedImages != null) {
            if (cancelled) {
                Iterator<Bitmap> it2 = this.generatedImages.iterator();
                while (it2.hasNext()) {
                    it2.next().recycle();
                }
            }
            this.generatedImages.clear();
        }
    }
}
