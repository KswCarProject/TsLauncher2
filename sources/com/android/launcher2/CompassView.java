package com.android.launcher2;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.launcher.R;

public class CompassView extends RelativeLayout {
    private String language;
    private ImageView mAboveDial;
    private Drawable mAboveDrawable;
    private ImageView mBelowDial;
    private Drawable mBelowDrawable;
    private ImageView mDial;
    private Drawable mDialDrawable_en;
    private Drawable mDialDrawable_zh;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            CompassView.this.updateLocal();
        }
    };

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources r = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CompassView);
        this.mDialDrawable_zh = a.getDrawable(0);
        this.mDialDrawable_en = a.getDrawable(1);
        if (!isInEditMode() && this.mDialDrawable_zh == null && this.mDialDrawable_en == null) {
            this.mDialDrawable_en = r.getDrawable(R.drawable.main_compass_dial);
        }
        this.mAboveDrawable = a.getDrawable(2);
        this.mBelowDrawable = a.getDrawable(3);
        a.recycle();
    }

    public void update(float angle) {
        rotateAnimation(angle);
    }

    /* access modifiers changed from: private */
    public void updateLocal() {
        if (!getContext().getResources().getConfiguration().locale.getLanguage().endsWith("zh") || this.mDialDrawable_zh == null) {
            this.mDial.setBackgroundDrawable(this.mDialDrawable_en);
        } else {
            this.mDial.setBackgroundDrawable(this.mDialDrawable_zh);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
        params.addRule(13);
        if (this.mBelowDrawable != null) {
            this.mBelowDial = new ImageView(getContext());
            this.mBelowDial.setBackgroundDrawable(this.mBelowDrawable);
            addView(this.mBelowDial, params);
        }
        this.mDial = new ImageView(getContext());
        updateLocal();
        addView(this.mDial, params);
        if (this.mAboveDrawable != null) {
            this.mAboveDial = new ImageView(getContext());
            this.mAboveDial.setBackgroundDrawable(this.mAboveDrawable);
            addView(this.mAboveDial, params);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        getContext().registerReceiver(this.receiver, filter);
    }

    private void rotateAnimation(float degree) {
        if (this.mDial != null) {
            if (this.mDial.getAnimation() != null) {
                this.mDial.getAnimation().cancel();
            }
            float rotate = this.mDial.getRotation();
            float tmp = degree - rotate;
            if (tmp > 180.0f) {
                rotate += 360.0f;
                this.mDial.setRotation(rotate);
            } else if (tmp < -180.0f) {
                rotate -= 360.0f;
                this.mDial.setRotation(rotate);
            }
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.mDial, "rotation", new float[]{rotate, degree});
            objectAnimator.setDuration(150);
            objectAnimator.start();
        }
    }
}
