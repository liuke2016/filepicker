package com.lynn.filepicker.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.lynn.filepicker.Util;

/**
 * Created by liuke on 2017/5/23.
 */

public class HideAbleToolbar extends Toolbar {
    private static final boolean AUTO_HIDE = false;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private static final int ANIMATOR_DURATION = 250;

    private ObjectAnimator mFadeIn1;
    private ObjectAnimator mFadeOut1;
    private ObjectAnimator mFadeIn2;
    private ObjectAnimator mFadeOut2;

    private boolean mVisible = true;
    private  Runnable mHideStatusBarRunnable;
    private final Runnable mShowStatusBarRunnable = new Runnable() {
        @Override
        public void run() {
            mFadeIn1.start();
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private Window mWindow;

    public HideAbleToolbar(Context context) {
        this(context,null);
    }

    public HideAbleToolbar(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Activity activity = (Activity) ((ContextThemeWrapper)context).getBaseContext();
        mWindow = activity.getWindow();
        mHideStatusBarRunnable = new Runnable() {
            @SuppressLint("InlinedApi")
            @Override
            public void run() {
                mWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        };
        mFadeIn1 = ObjectAnimator.ofFloat(this, View.ALPHA, 1f);
        mFadeIn1.setInterpolator(null);
        mFadeIn1.setDuration(ANIMATOR_DURATION);
        mFadeOut1 = ObjectAnimator.ofFloat(this, View.ALPHA, 0);
        mFadeOut1.setInterpolator(null);
        mFadeOut1.setDuration(ANIMATOR_DURATION);
        mFadeIn1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(View.VISIBLE);
            }
        });
        mFadeOut1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
            }
        });
    }

    public void doAnimTogetherWith(final View togetherView){
        mFadeIn2 = ObjectAnimator.ofFloat(togetherView, View.ALPHA, 1f);
        mFadeIn2.setInterpolator(null);
        mFadeIn2.setDuration(ANIMATOR_DURATION + UI_ANIMATION_DELAY);
        mFadeOut2 = ObjectAnimator.ofFloat(togetherView, View.ALPHA, 0);
        mFadeOut2.setInterpolator(null);
        mFadeOut2.setDuration(ANIMATOR_DURATION + UI_ANIMATION_DELAY);
        mFadeOut1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                togetherView.setVisibility(View.GONE);
            }
        });
        mFadeIn2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                togetherView.setVisibility(View.VISIBLE);
            }
        });
    }


    public void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
        }
    }

    public void hide() {
        hide(UI_ANIMATION_DELAY);
    }

    private void hide(int delay) {
        if(mVisible){
            Util.setTranslucentView((ViewGroup) mWindow.getDecorView(), 0);
            mFadeOut1.start();
            if(mFadeOut2!=null){
                mFadeOut2.start();
            }
            mVisible = false;
            mWindow.getDecorView().removeCallbacks(mShowStatusBarRunnable);
            mWindow.getDecorView().postDelayed(mHideStatusBarRunnable, delay);
        }
    }

    @SuppressLint("InlinedApi")
    public void show() {
        Util.setTranslucentView((ViewGroup) mWindow.getDecorView(), Util.DEFAULT_ALPHA);
        mWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;
        if(mFadeIn2!=null){
            mFadeIn2.start();
        }
        mWindow.getDecorView().removeCallbacks(mHideStatusBarRunnable);
        mWindow.getDecorView().postDelayed(mShowStatusBarRunnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mWindow.getDecorView().removeCallbacks(mHideRunnable);
        mWindow.getDecorView().postDelayed(mHideRunnable, delayMillis);
    }

}
