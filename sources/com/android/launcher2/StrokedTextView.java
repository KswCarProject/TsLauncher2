package com.android.launcher2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.launcher.R;

public class StrokedTextView extends TextView {
    private Bitmap mCache;
    private final Canvas mCanvas = new Canvas();
    private final Paint mPaint = new Paint();
    private int mStrokeColor;
    private float mStrokeWidth;
    private int mTextColor;
    private boolean mUpdateCachedBitmap;

    public StrokedTextView(Context context) {
        super(context);
        init(context, (AttributeSet) null, 0);
    }

    public StrokedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public StrokedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokedTextView, defStyle, 0);
        this.mStrokeColor = a.getColor(0, ViewCompat.MEASURED_STATE_MASK);
        this.mStrokeWidth = a.getFloat(2, 0.0f);
        this.mTextColor = a.getColor(1, -1);
        a.recycle();
        this.mUpdateCachedBitmap = true;
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    /* access modifiers changed from: protected */
    public void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        this.mUpdateCachedBitmap = true;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) {
            this.mCache = null;
            return;
        }
        this.mUpdateCachedBitmap = true;
        this.mCache = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mCache != null) {
            if (this.mUpdateCachedBitmap) {
                int w = getMeasuredWidth();
                int h = getMeasuredHeight();
                String text = getText().toString();
                Rect textBounds = new Rect();
                Paint textPaint = getPaint();
                int textWidth = (int) textPaint.measureText(text);
                textPaint.getTextBounds("x", 0, 1, textBounds);
                this.mCanvas.setBitmap(this.mCache);
                this.mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                int drawableLeft = getPaddingLeft();
                int drawableTop = getPaddingTop();
                Drawable[] drawables = getCompoundDrawables();
                for (int i = 0; i < drawables.length; i++) {
                    if (drawables[i] != null) {
                        drawables[i].setBounds(drawableLeft, drawableTop, drawables[i].getIntrinsicWidth() + drawableLeft, drawables[i].getIntrinsicHeight() + drawableTop);
                        drawables[i].draw(this.mCanvas);
                    }
                }
                int left = (w - getPaddingRight()) - textWidth;
                int bottom = (textBounds.height() + h) / 2;
                this.mPaint.setStrokeWidth(this.mStrokeWidth);
                this.mPaint.setColor(this.mStrokeColor);
                this.mPaint.setTextSize(getTextSize());
                this.mCanvas.drawText(text, (float) left, (float) bottom, this.mPaint);
                this.mPaint.setStrokeWidth(0.0f);
                this.mPaint.setColor(this.mTextColor);
                this.mCanvas.drawText(text, (float) left, (float) bottom, this.mPaint);
                this.mUpdateCachedBitmap = false;
            }
            canvas.drawBitmap(this.mCache, 0.0f, 0.0f, this.mPaint);
            return;
        }
        super.onDraw(canvas);
    }
}
