package com.android.launcher2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class PagedViewIcon extends TextView {
    private static final float PRESS_ALPHA = 0.4f;
    private static final String TAG = "PagedViewIcon";
    private Bitmap mIcon;
    private boolean mLockDrawableState;
    private PressedCallback mPressedCallback;

    public interface PressedCallback {
        void iconPressed(PagedViewIcon pagedViewIcon);
    }

    public PagedViewIcon(Context context) {
        this(context, (AttributeSet) null);
    }

    public PagedViewIcon(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagedViewIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mLockDrawableState = false;
    }

    public void applyFromApplicationInfo(ApplicationInfo info, boolean scaleUp, PressedCallback cb) {
        this.mIcon = info.iconBitmap;
        this.mPressedCallback = cb;
        setCompoundDrawablesWithIntrinsicBounds((Drawable) null, new FastBitmapDrawable(this.mIcon), (Drawable) null, (Drawable) null);
        setText(info.title);
        setTag(info);
    }

    public void lockDrawableState() {
        this.mLockDrawableState = true;
    }

    public void resetDrawableState() {
        this.mLockDrawableState = false;
        post(new Runnable() {
            public void run() {
                PagedViewIcon.this.refreshDrawableState();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        if (isPressed()) {
            setAlpha(PRESS_ALPHA);
            if (this.mPressedCallback != null) {
                this.mPressedCallback.iconPressed(this);
            }
        } else if (!this.mLockDrawableState) {
            setAlpha(1.0f);
        }
    }
}
