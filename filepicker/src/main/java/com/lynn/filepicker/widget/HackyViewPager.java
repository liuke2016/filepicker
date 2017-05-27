package com.lynn.filepicker.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by liuke on 2017/4/10.
 */

public class HackyViewPager extends ViewPager {

    private GestureDetector detector;
    private OnSingleTapConfirmedListener mOnSingleTapConfirmedListener;

    public HackyViewPager(Context context) {
        this(context,null);
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(mOnSingleTapConfirmedListener!=null){
                    mOnSingleTapConfirmedListener.onSingleTagConfirmed(e);
                }
                return super.onSingleTapConfirmed(e);
            }
        });
    }
    public void setOnSingleTapConfirmedListener(OnSingleTapConfirmedListener onSingleTapConfirmedListener) {
        mOnSingleTapConfirmedListener = onSingleTapConfirmedListener;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
    public interface OnSingleTapConfirmedListener{
        void onSingleTagConfirmed(MotionEvent e);
    }
}