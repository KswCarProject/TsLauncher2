package com.android.launcher2;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import java.util.HashSet;
import java.util.Iterator;

public class LauncherAnimUtils {
    static HashSet<Animator> sAnimators = new HashSet<>();
    static Animator.AnimatorListener sEndAnimListener = new Animator.AnimatorListener() {
        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            LauncherAnimUtils.sAnimators.remove(animation);
        }

        public void onAnimationCancel(Animator animation) {
            LauncherAnimUtils.sAnimators.remove(animation);
        }
    };

    public static void cancelOnDestroyActivity(Animator a) {
        sAnimators.add(a);
        a.addListener(sEndAnimListener);
    }

    public static void onDestroyActivity() {
        Iterator<Animator> it = new HashSet<>(sAnimators).iterator();
        while (it.hasNext()) {
            Animator a = it.next();
            if (a.isRunning()) {
                a.cancel();
            } else {
                sAnimators.remove(a);
            }
        }
    }

    public static AnimatorSet createAnimatorSet() {
        AnimatorSet anim = new AnimatorSet();
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ValueAnimator ofFloat(float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ObjectAnimator ofFloat(Object target, String propertyName, float... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setPropertyName(propertyName);
        anim.setFloatValues(values);
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ObjectAnimator ofPropertyValuesHolder(Object target, PropertyValuesHolder... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setValues(values);
        cancelOnDestroyActivity(anim);
        return anim;
    }
}
