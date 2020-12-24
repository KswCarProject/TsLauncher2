package com.android.launcher2;

import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class PageViewAnimation {
    private static float CAMERA_DISTANCE = 6500.0f;
    public static final int PAGEVIEW_ANIMATION_BLOCKS = 7;
    public static final int PAGEVIEW_ANIMATION_LAYERED = 2;
    public static final int PAGEVIEW_ANIMATION_NORMAL = 0;
    public static final int PAGEVIEW_ANIMATION_PAGETURN = 4;
    public static final int PAGEVIEW_ANIMATION_ROTATE = 3;
    public static final int PAGEVIEW_ANIMATION_ROTATEBYCENTERPOINT = 6;
    public static final int PAGEVIEW_ANIMATION_ROTATEBYLEFTTOPPOINT = 5;
    public static final int PAGEVIEW_ANIMATION_TURNTABLE = 1;
    private static final String TAG = "PageViewAnimation";
    private static float TRANSITION_MAX_ROTATION = 24.0f;
    private static float TRANSITION_PIVOT = 0.65f;
    private static float TRANSITION_SCALE_FACTOR = 0.74f;
    private static AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);
    private static PageViewAnimation mInstance;
    private static DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator(4.0f);
    private static ZInterpolator mZInterpolator = new ZInterpolator(0.5f);
    private float mAlpha = 1.0f;
    private int mAnimaType = 1;
    private float mPivotX = 0.0f;
    private float mPivotY = 0.0f;
    private float mRotation = 0.0f;
    private float mRotationY = 0.0f;
    private float mScale = 1.0f;
    private float mTranslationX = 0.0f;

    public static PageViewAnimation getInstance() {
        if (mInstance == null) {
            synchronized (PageViewAnimation.class) {
                if (mInstance == null) {
                    mInstance = new PageViewAnimation();
                }
            }
        }
        return mInstance;
    }

    public void setPageViewAnime(int type) {
        this.mAnimaType = type;
    }

    public int getPageViewAnime() {
        return this.mAnimaType;
    }

    public void pageViewAnime(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        this.mAnimaType = 0;
        switch (this.mAnimaType) {
            case 1:
                this.mRotation = (-TRANSITION_MAX_ROTATION) * scrollProgress;
                this.mPivotX = ((float) v.getMeasuredWidth()) * 0.5f;
                this.mPivotY = (float) v.getMeasuredHeight();
                break;
            case 2:
                float minScrollProgress = Math.min(0.0f, scrollProgress);
                this.mTranslationX = ((float) v.getMeasuredWidth()) * minScrollProgress;
                float interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(minScrollProgress));
                this.mScale = (1.0f - interpolatedProgress) + (TRANSITION_SCALE_FACTOR * interpolatedProgress);
                if (scrollProgress >= 0.0f) {
                    this.mAlpha = mLeftScreenAlphaInterpolator.getInterpolation(1.0f - scrollProgress);
                    break;
                } else {
                    this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
                    break;
                }
            case 3:
                this.mPivotX = ((float) v.getMeasuredWidth()) * 0.5f;
                this.mPivotY = (float) v.getMeasuredHeight();
                this.mRotationY = -90.0f * scrollProgress;
                this.mTranslationX = ((float) v.getMeasuredWidth()) * scrollProgress;
                this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
                break;
            case 4:
                this.mPivotX = 0.0f;
                this.mPivotY = 0.0f;
                this.mRotationY = -90.0f * scrollProgress;
                this.mTranslationX = ((float) v.getMeasuredWidth()) * scrollProgress;
                this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
                break;
            case 5:
                this.mPivotX = 0.0f;
                this.mPivotY = 0.0f;
                this.mRotation = -90.0f * scrollProgress;
                this.mTranslationX = ((float) v.getMeasuredWidth()) * scrollProgress;
                this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
                break;
            case 6:
                this.mPivotX = ((float) v.getMeasuredWidth()) * 0.5f;
                this.mPivotY = ((float) v.getMeasuredHeight()) * 0.5f;
                this.mRotation = -90.0f * scrollProgress;
                this.mTranslationX = ((float) v.getMeasuredWidth()) * scrollProgress;
                this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
                break;
            case 7:
                if (scrollProgress < 0.0f) {
                    this.mPivotX = 0.0f;
                } else {
                    this.mPivotX = (float) v.getMeasuredWidth();
                }
                this.mRotationY = -90.0f * scrollProgress;
                break;
        }
        if (count > 0) {
            overScrollAnimation(scrollProgress, i, count, density, v);
        }
        setViewAttr(v);
        showOrHideView(v);
    }

    public void setTurntableAnim(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        this.mRotation = (-TRANSITION_MAX_ROTATION) * scrollProgress;
        this.mPivotX = ((float) v.getMeasuredWidth()) * 0.5f;
        this.mPivotY = (float) v.getMeasuredHeight();
        overScrollAnimation(scrollProgress, i, count, density, v);
        setViewAttr(v);
        showOrHideView(v);
    }

    public void setLayeredAnim(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        float minScrollProgress = Math.min(0.0f, scrollProgress);
        this.mTranslationX = ((float) v.getMeasuredWidth()) * minScrollProgress;
        float interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(minScrollProgress));
        this.mScale = (1.0f - interpolatedProgress) + (TRANSITION_SCALE_FACTOR * interpolatedProgress);
        if (scrollProgress < 0.0f) {
            this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
        } else {
            this.mAlpha = mLeftScreenAlphaInterpolator.getInterpolation(1.0f - scrollProgress);
        }
        overScrollAnimation(scrollProgress, i, count, density, v);
        setViewAttr(v);
        showOrHideView(v);
    }

    public void setNormalAnim(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        overScrollAnimation(scrollProgress, i, count, density, v);
        setViewAttr(v);
        showOrHideView(v);
    }

    public void setRotateAnim(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        this.mPivotX = ((float) v.getMeasuredWidth()) * 0.5f;
        this.mPivotY = (float) v.getMeasuredHeight();
        this.mRotationY = -90.0f * scrollProgress;
        this.mTranslationX = ((float) v.getMeasuredWidth()) * scrollProgress;
        this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
        overScrollAnimation(scrollProgress, i, count, density, v);
        setViewAttr(v);
        showOrHideView(v);
    }

    public void setPageTurnAnim(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        this.mPivotX = 0.0f;
        this.mPivotY = 0.0f;
        this.mRotationY = -90.0f * scrollProgress;
        this.mTranslationX = ((float) v.getMeasuredWidth()) * scrollProgress;
        this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
        overScrollAnimation(scrollProgress, i, count, density, v);
        setViewAttr(v);
        showOrHideView(v);
    }

    public void setRotateByLeftTopPointAnim(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        this.mPivotX = 0.0f;
        this.mPivotY = 0.0f;
        this.mRotation = -90.0f * scrollProgress;
        this.mTranslationX = ((float) v.getMeasuredWidth()) * scrollProgress;
        this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
        overScrollAnimation(scrollProgress, i, count, density, v);
        setViewAttr(v);
        showOrHideView(v);
    }

    public void setRotateByCenterPointAnim(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        this.mPivotX = ((float) v.getMeasuredWidth()) * 0.5f;
        this.mPivotY = ((float) v.getMeasuredHeight()) * 0.5f;
        this.mRotation = -90.0f * scrollProgress;
        this.mTranslationX = ((float) v.getMeasuredWidth()) * scrollProgress;
        this.mAlpha = mAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
        overScrollAnimation(scrollProgress, i, count, density, v);
        setViewAttr(v);
        showOrHideView(v);
    }

    public void setBlocksAnim(float scrollProgress, int i, int count, float density, View v) {
        resetAttr(v);
        if (scrollProgress < 0.0f) {
            this.mPivotX = 0.0f;
        } else {
            this.mPivotX = (float) v.getMeasuredWidth();
        }
        this.mRotationY = -90.0f * scrollProgress;
        overScrollAnimation(scrollProgress, i, count, density, v);
        setViewAttr(v);
        showOrHideView(v);
    }

    private void setViewAttr(View v) {
        v.setPivotY(this.mPivotY);
        v.setPivotX(this.mPivotX);
        v.setRotation(this.mRotation);
        v.setRotationY(this.mRotationY);
        v.setTranslationX(this.mTranslationX);
        v.setScaleX(this.mScale);
        v.setScaleY(this.mScale);
        v.setAlpha(this.mAlpha);
    }

    private void showOrHideView(View v) {
        if (this.mAlpha == 0.0f) {
            v.setVisibility(4);
        } else if (v.getVisibility() != 0) {
            v.setVisibility(0);
        }
    }

    private void resetAttr(View v) {
        this.mTranslationX = 0.0f;
        this.mScale = 1.0f;
        this.mAlpha = 1.0f;
        this.mRotation = 0.0f;
        this.mRotationY = 0.0f;
        this.mPivotX = ((float) v.getMeasuredWidth()) * 0.5f;
        this.mPivotY = ((float) v.getMeasuredHeight()) * 0.5f;
    }

    public void overScrollAnimation(float scrollProgress, int i, int count, float density, View v) {
        boolean isOverscrollingFirstPage;
        boolean isOverscrollingLastPage = true;
        float xPivot = TRANSITION_PIVOT;
        if (scrollProgress < 0.0f) {
            isOverscrollingFirstPage = true;
        } else {
            isOverscrollingFirstPage = false;
        }
        if (scrollProgress <= 0.0f) {
            isOverscrollingLastPage = false;
        }
        int pageWidth = v.getMeasuredWidth();
        int pageHeight = v.getMeasuredHeight();
        v.setCameraDistance(CAMERA_DISTANCE * density);
        if (i == 0 && isOverscrollingFirstPage) {
            this.mPivotX = ((float) pageWidth) * xPivot;
            this.mPivotY = ((float) pageHeight) / 2.0f;
            this.mRotation = 0.0f;
            this.mRotationY = (-TRANSITION_MAX_ROTATION) * scrollProgress;
            this.mScale = 1.0f;
            this.mAlpha = 1.0f;
            this.mTranslationX = 0.0f;
        } else if (i == count - 1 && isOverscrollingLastPage) {
            this.mPivotX = ((float) pageWidth) * xPivot;
            this.mPivotY = ((float) pageHeight) / 2.0f;
            this.mRotation = 0.0f;
            this.mRotationY = (-TRANSITION_MAX_ROTATION) * scrollProgress;
            this.mScale = 1.0f;
            this.mAlpha = 1.0f;
            this.mTranslationX = 0.0f;
        }
    }

    private static class ZInterpolator implements TimeInterpolator {
        private float focalLength;

        public ZInterpolator(float foc) {
            this.focalLength = foc;
        }

        public float getInterpolation(float input) {
            return (1.0f - (this.focalLength / (this.focalLength + input))) / (1.0f - (this.focalLength / (this.focalLength + 1.0f)));
        }
    }
}
