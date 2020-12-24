package com.android.launcher2;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.launcher.R;

public class VelociMeter extends RelativeLayout {
    private float mDegree;
    private Drawable mDot;
    private Drawable mPointer;
    private float mSpeed;
    private int mTextId;
    private ImageView mVelociDot;
    private ImageView mVelociPointer;
    private TextView mVelociText;

    public VelociMeter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttributes(context, attrs);
    }

    public void updateSpeed(float speed) {
        float degree = (this.mDegree * speed) / this.mSpeed;
        if (this.mVelociPointer.getAnimation() != null) {
            this.mVelociPointer.getAnimation().cancel();
        }
        float rotate = this.mVelociPointer.getRotation();
        float tmp = (degree - rotate) - this.mDegree;
        if (tmp > 180.0f) {
            rotate += 360.0f;
            this.mVelociPointer.setRotation(rotate);
        } else if (tmp < -180.0f) {
            rotate -= 360.0f;
            this.mVelociPointer.setRotation(rotate);
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.mVelociPointer, "rotation", new float[]{rotate, degree - this.mDegree});
        objectAnimator.setDuration(150);
        objectAnimator.start();
        if (this.mVelociText != null) {
            this.mVelociText.setText(new StringBuilder().append((int) speed).toString());
        }
    }

    private RelativeLayout.LayoutParams getViewParamsByAttrs(TypedArray a, int leftId, int topId) {
        int marginLeft = a.getInt(leftId, -1);
        int marginTop = a.getInt(topId, -1);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
        if (marginLeft == -1 && marginTop == -1) {
            params.addRule(13);
        } else if (marginLeft == -1) {
            params.addRule(14);
            params.setMargins(0, marginTop, 0, 0);
        } else if (marginTop == -1) {
            params.addRule(15);
            params.setMargins(marginLeft, 0, 0, 0);
        } else {
            params.setMargins(marginLeft, marginTop, 0, 0);
        }
        return params;
    }

    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VelociMeter);
        this.mPointer = a.getDrawable(0);
        if (this.mPointer != null) {
            this.mVelociPointer = new ImageView(context);
            this.mVelociPointer.setBackgroundDrawable(this.mPointer);
            addView(this.mVelociPointer, getViewParamsByAttrs(a, 1, 2));
            this.mDot = a.getDrawable(3);
            if (this.mDot != null) {
                this.mVelociDot = new ImageView(context);
                this.mVelociDot.setBackgroundDrawable(this.mDot);
                addView(this.mVelociDot, getViewParamsByAttrs(a, 4, 5));
            }
            this.mTextId = a.getResourceId(6, -1);
            this.mDegree = a.getFloat(8, -1.0f);
            if (this.mDegree == -1.0f) {
                throw new IllegalArgumentException("The degree attribute is required.");
            }
            this.mSpeed = a.getFloat(7, -1.0f);
            if (this.mSpeed == -1.0f) {
                throw new IllegalArgumentException("The speed attribute is required.");
            }
            this.mVelociPointer.setRotation(-this.mDegree);
            a.recycle();
            return;
        }
        throw new IllegalArgumentException("The pointer attribute is required.");
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        if (this.mTextId != -1) {
            this.mVelociText = (TextView) findViewById(this.mTextId);
            this.mVelociText.setText("0");
        }
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
