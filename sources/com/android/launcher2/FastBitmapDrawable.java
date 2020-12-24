package com.android.launcher2;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;

class FastBitmapDrawable extends Drawable {
    private int mAlpha = MotionEventCompat.ACTION_MASK;
    private Bitmap mBitmap;
    private int mHeight;
    private final Paint mPaint = new Paint(2);
    private int mWidth;

    FastBitmapDrawable(Bitmap b) {
        this.mBitmap = b;
        if (b != null) {
            this.mWidth = this.mBitmap.getWidth();
            this.mHeight = this.mBitmap.getHeight() + 0;
            return;
        }
        this.mHeight = 0;
        this.mWidth = 0;
    }

    public void draw(Canvas canvas) {
        Rect r = getBounds();
        canvas.drawBitmap(this.mBitmap, (float) r.left, (float) r.top, this.mPaint);
    }

    public void setColorFilter(ColorFilter cf) {
        this.mPaint.setColorFilter(cf);
    }

    public int getOpacity() {
        return -3;
    }

    public void setAlpha(int alpha) {
        this.mAlpha = alpha;
        this.mPaint.setAlpha(alpha);
    }

    public void setFilterBitmap(boolean filterBitmap) {
        this.mPaint.setFilterBitmap(filterBitmap);
    }

    @SuppressLint({"Override"})
    public int getAlpha() {
        return this.mAlpha;
    }

    public int getIntrinsicWidth() {
        return this.mWidth;
    }

    public int getIntrinsicHeight() {
        return this.mHeight;
    }

    public int getMinimumWidth() {
        return this.mWidth;
    }

    public int getMinimumHeight() {
        return this.mHeight;
    }

    public void setBitmap(Bitmap b) {
        this.mBitmap = b;
        if (b != null) {
            this.mWidth = this.mBitmap.getWidth();
            this.mHeight = this.mBitmap.getHeight();
            return;
        }
        this.mHeight = 0;
        this.mWidth = 0;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }
}
