package com.android.launcher2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.launcher.R;
import java.util.TimeZone;

public class TsAnalogClock extends View {
    private static final String TAG = "[wcb]TsAnalogClock";
    private boolean mAttached;
    /* access modifiers changed from: private */
    public Time mCalendar;
    private boolean mChanged = false;
    private Drawable mDial;
    private Drawable mDot;
    private final Handler mHandler = new Handler();
    private float mHour;
    private Drawable mHourHand;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.TIMEZONE_CHANGED")) {
                TsAnalogClock.this.mCalendar = new Time(TimeZone.getTimeZone(intent.getStringExtra("time-zone")).getID());
            }
            TsAnalogClock.this.onTimeChanged();
        }
    };
    private int mMarginLeft = 0;
    private int mMarginTop = 0;
    private float mMinutes;
    private Drawable mMinutesHand;
    private float mSecond;
    private Drawable mSecondHand;
    private Runnable timeUpdate = new Runnable() {
        public void run() {
            TsAnalogClock.this.onTimeChanged();
            TsAnalogClock.this.postDelayed(this, 200);
        }
    };

    public TsAnalogClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TsAnalogClock);
        this.mSecondHand = a.getDrawable(1);
        this.mDot = a.getDrawable(4);
        this.mDial = a.getDrawable(0);
        if (this.mDial == null) {
            throw new IllegalArgumentException("The dial attribute is required and must refered.");
        }
        this.mMinutesHand = a.getDrawable(3);
        if (this.mMinutesHand == null) {
            throw new IllegalArgumentException("The minuteshand attribute is required and must refered.");
        }
        this.mHourHand = a.getDrawable(2);
        if (this.mHourHand == null) {
            throw new IllegalArgumentException("The hourhand attribute is required and must refered.");
        }
        this.mMarginLeft = a.getInt(5, -1);
        this.mMarginTop = a.getInt(6, -1);
        a.recycle();
    }

    private void setBound(Drawable drawable) {
        int x = (getRight() - getLeft()) / 2;
        int y = (getBottom() - getTop()) / 2;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Rect bound = new Rect(x - (width / 2), y - (height / 2), (width / 2) + x, (height / 2) + y);
        if (this.mMarginLeft != -1) {
            bound.left = this.mMarginLeft;
            bound.right = bound.left + width;
        }
        if (this.mMarginTop != -1) {
            bound.top = this.mMarginTop;
            bound.bottom = bound.top + height;
        }
        drawable.setBounds(bound);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mChanged) {
            this.mChanged = false;
        }
        int x = (getRight() - getLeft()) / 2;
        int y = (getBottom() - getTop()) / 2;
        canvas.save();
        setBound(this.mDial);
        this.mDial.draw(canvas);
        canvas.save();
        canvas.rotate(this.mHour, (float) x, (float) y);
        setBound(this.mHourHand);
        this.mHourHand.draw(canvas);
        canvas.restore();
        canvas.save();
        canvas.rotate(this.mMinutes, (float) x, (float) y);
        setBound(this.mMinutesHand);
        this.mMinutesHand.draw(canvas);
        canvas.restore();
        if (this.mSecondHand != null) {
            canvas.save();
            canvas.rotate(this.mSecond, (float) x, (float) y);
            setBound(this.mSecondHand);
            this.mSecondHand.draw(canvas);
            canvas.restore();
        }
        if (this.mDot != null) {
            canvas.save();
            setBound(this.mDot);
            this.mDot.draw(canvas);
            canvas.restore();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        Log.d("TAG", "onAttachedToWindow");
        super.onAttachedToWindow();
        if (!this.mAttached) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.TIME_TICK");
            filter.addAction("android.intent.action.TIME_SET");
            filter.addAction("android.intent.action.TIMEZONE_CHANGED");
            getContext().registerReceiver(this.mIntentReceiver, filter, (String) null, this.mHandler);
            postDelayed(this.timeUpdate, 200);
        }
        this.mCalendar = new Time();
        onTimeChanged();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        Log.d("TAG", "onDetachedFromWindow");
        super.onDetachedFromWindow();
        if (this.mAttached) {
            getContext().unregisterReceiver(this.mIntentReceiver);
            removeCallbacks(this.timeUpdate);
            this.mAttached = false;
        }
    }

    /* access modifiers changed from: private */
    public void onTimeChanged() {
        this.mCalendar.setToNow();
        int hour = this.mCalendar.hour;
        int minute = this.mCalendar.minute;
        int second = this.mCalendar.second;
        this.mSecond = (float) (second * 6);
        this.mMinutes = ((float) (minute * 6)) + (((float) second) / 60.0f);
        this.mHour = (float) (((hour % 12) * 30) + (minute / 2));
        this.mChanged = true;
        invalidate();
    }
}
