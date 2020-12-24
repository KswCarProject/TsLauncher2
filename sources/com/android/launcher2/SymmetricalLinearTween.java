package com.android.launcher2;

import android.os.Handler;
import android.os.SystemClock;

class SymmetricalLinearTween {
    private static final int FPS = 30;
    private static final int FRAME_TIME = 33;
    long mBase;
    TweenCallback mCallback;
    boolean mDirection;
    int mDuration;
    Handler mHandler;
    boolean mRunning;
    Runnable mTick = new Runnable() {
        public void run() {
            long base = SymmetricalLinearTween.this.mBase;
            long diff = SystemClock.uptimeMillis() - base;
            int duration = SymmetricalLinearTween.this.mDuration;
            float val = ((float) diff) / ((float) duration);
            if (!SymmetricalLinearTween.this.mDirection) {
                val = 1.0f - val;
            }
            if (val > 1.0f) {
                val = 1.0f;
            } else if (val < 0.0f) {
                val = 0.0f;
            }
            float old = SymmetricalLinearTween.this.mValue;
            SymmetricalLinearTween.this.mValue = val;
            SymmetricalLinearTween.this.mCallback.onTweenValueChanged(val, old);
            long next = base + ((long) ((((int) (diff / 33)) + 1) * 33));
            if (diff < ((long) duration)) {
                SymmetricalLinearTween.this.mHandler.postAtTime(this, next);
            }
            if (diff >= ((long) duration)) {
                SymmetricalLinearTween.this.mCallback.onTweenFinished();
                SymmetricalLinearTween.this.mRunning = false;
            }
        }
    };
    float mValue;

    public SymmetricalLinearTween(boolean initial, int duration, TweenCallback callback) {
        this.mValue = initial ? 1.0f : 0.0f;
        this.mDirection = initial;
        this.mDuration = duration;
        this.mCallback = callback;
        this.mHandler = new Handler();
    }

    public void start(boolean direction) {
        start(direction, SystemClock.uptimeMillis());
    }

    public void start(boolean direction, long baseTime) {
        if (direction != this.mDirection) {
            if (!this.mRunning) {
                this.mBase = baseTime;
                this.mRunning = true;
                this.mCallback.onTweenStarted();
                this.mHandler.postAtTime(this.mTick, SystemClock.uptimeMillis() + 33);
            } else {
                long now = SystemClock.uptimeMillis();
                this.mBase = (now + (now - this.mBase)) - ((long) this.mDuration);
            }
            this.mDirection = direction;
        }
    }
}
