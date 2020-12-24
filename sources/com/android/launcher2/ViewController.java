package com.android.launcher2;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import java.util.ArrayList;
import java.util.List;

public class ViewController {
    /* access modifiers changed from: private */
    public View mAimView;
    private AnimatorSet mAinmatSet = new AnimatorSet();
    private AnimatWithViewChange mAnimatWithViewChange;
    /* access modifiers changed from: private */
    public View mCurrentView;
    private int mShowId = 0;
    private ViewChangeAnimation mViewAnimation;
    private ViewFilter mViewFilter;
    private List<View> mViewList = new ArrayList();

    public interface AnimatWithViewChange {
        void animatWithViewChange(View view, View view2);
    }

    public interface ViewChangeAnimation {
        void viewChangeAnimation(View view, View view2);
    }

    public interface ViewFilter {
        boolean viewFilter(int i);
    }

    public void setViewFilter(ViewFilter filter) {
        this.mViewFilter = filter;
    }

    public void setViewChangeAnimation(ViewChangeAnimation animation) {
        this.mViewAnimation = animation;
    }

    public void setAnimatWithViewChange(AnimatWithViewChange animation) {
        this.mAnimatWithViewChange = animation;
    }

    public void showPrev() {
        int cnt = this.mViewList.size();
        if (cnt > 1) {
            int id = ((this.mShowId + cnt) - 1) % cnt;
            if (this.mViewFilter != null) {
                for (int i = 0; i < cnt && !this.mViewFilter.viewFilter(this.mViewList.get(id).getId()); i++) {
                    id = ((id + cnt) - 1) % cnt;
                }
            }
            showViewChange(id);
        }
    }

    public void showNext() {
        int cnt = this.mViewList.size();
        if (cnt > 1) {
            int id = (this.mShowId + 1) % cnt;
            if (this.mViewFilter != null) {
                for (int i = 0; i < cnt && !this.mViewFilter.viewFilter(this.mViewList.get(id).getId()); i++) {
                    id = (id + 1) % cnt;
                }
            }
            showViewChange(id);
        }
    }

    public void show(int ViewId) {
        int i = 0;
        while (i < this.mViewList.size()) {
            if (this.mViewList.get(i).getId() != ViewId || (this.mViewFilter != null && !this.mViewFilter.viewFilter(ViewId))) {
                i++;
            } else {
                showViewChange(i);
                return;
            }
        }
    }

    public void addView(View view) {
        if (!this.mViewList.contains(view)) {
            this.mViewList.add(view);
        }
    }

    private void showViewChange(int show) {
        if (this.mShowId == show) {
            this.mCurrentView = this.mViewList.get(this.mShowId);
            if (this.mCurrentView.getVisibility() != 0) {
                for (View view : this.mViewList) {
                    view.setVisibility(8);
                }
                this.mCurrentView.setVisibility(0);
                return;
            }
            return;
        }
        this.mCurrentView = this.mViewList.get(this.mShowId);
        this.mAimView = this.mViewList.get(show);
        this.mShowId = show;
        if (this.mViewAnimation != null) {
            this.mViewAnimation.viewChangeAnimation(this.mCurrentView, this.mAimView);
            return;
        }
        this.mAimView.setVisibility(0);
        this.mAimView.setAlpha(0.0f);
        if (this.mAinmatSet.isRunning()) {
            this.mAinmatSet.end();
        }
        ObjectAnimator fadout = ObjectAnimator.ofFloat(this.mCurrentView, "alpha", new float[]{1.0f, 0.0f});
        ObjectAnimator fadin = ObjectAnimator.ofFloat(this.mAimView, "alpha", new float[]{0.0f, 1.0f});
        this.mAinmatSet.setDuration(1000);
        this.mAinmatSet.playTogether(new Animator[]{fadout, fadin});
        this.mAinmatSet.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mAinmatSet.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                ViewController.this.mCurrentView.setVisibility(8);
                ViewController.this.mCurrentView.setAlpha(1.0f);
                ViewController.this.mAimView.setAlpha(1.0f);
                ViewController.this.mAimView.setVisibility(0);
            }

            public void onAnimationCancel(Animator animation) {
                ViewController.this.mCurrentView.setVisibility(8);
                ViewController.this.mCurrentView.setAlpha(1.0f);
                ViewController.this.mAimView.setAlpha(1.0f);
                ViewController.this.mAimView.setVisibility(0);
            }
        });
        this.mAinmatSet.start();
        if (this.mAnimatWithViewChange != null) {
            this.mAnimatWithViewChange.animatWithViewChange(this.mCurrentView, this.mAimView);
        }
    }
}
