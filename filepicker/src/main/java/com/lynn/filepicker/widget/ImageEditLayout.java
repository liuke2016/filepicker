package com.lynn.filepicker.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.lynn.filepicker.R;
import com.lynn.filepicker.Util;

import java.io.File;

/**
 * Created by liuke on 2017/5/23.
 */

public class ImageEditLayout extends FrameLayout {
    private static final String[] colors = {"#FFFFFF", "#FF0000", "#A05000", "#50A000", "#00FF00", "#00A050", "#0050A0", "#0000FF", "#000000"};
    private int mCurrentColor = Color.WHITE;
    private ImageView mIvBack;
    private int screenWidth, screenHeight;
    private PaintConfigLayout mPaintConfigLayout;
    private TuyaView mTuyaView;
    private String mPath;

    public PaintConfigLayout getPaintConfigLayout() {
        return mPaintConfigLayout;
    }

    public ImageEditLayout(@NonNull Context context) {
        this(context, null);
    }

    public ImageEditLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenHeight = context.getResources().getDisplayMetrics().heightPixels;


        mTuyaView = new TuyaView(context, screenWidth, screenHeight);
        mTuyaView.setPaintColor(mCurrentColor);
        FrameLayout.LayoutParams lp2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mTuyaView, lp2);

        mPaintConfigLayout = new PaintConfigLayout(context);
        FrameLayout.LayoutParams lp1 = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dip2px(50));
        lp1.gravity = Gravity.BOTTOM;
        addView(mPaintConfigLayout, lp1);

    }


    public void saveImage(String path) {
        mTuyaView.saveToSDCard(path + File.separator + new File(mPath).getName());
    }


    public void setImage(String path) {
        mPath = path;
        Glide.with(getContext()).load(path)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.mipmap.ic_place_holder).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mTuyaView.setBitmap(resource);
            }
        });
    }


    class PaintConfigLayout extends RelativeLayout {

        private boolean mIsTouchColor;
        private int mLength;
        private int mStartX;
        private int mCurrentX;

        public PaintConfigLayout(Context context) {
            super(context);

            Drawable drawable = DrawableCompat.wrap(context.getResources().getDrawable(R.mipmap.ic_back_space));
            DrawableCompat.setTintList(drawable, new ColorStateList(new int[][]{new int[]{android.R.attr.state_pressed},new int[]{}},new int[]{Color.GREEN,Color.WHITE}));
            mIvBack = new ImageView(context);
            mIvBack.setImageDrawable(drawable);
            mIvBack.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            RelativeLayout.LayoutParams ivBackLp = new LayoutParams(Util.dip2px(50), ViewGroup.LayoutParams.MATCH_PARENT);
            ivBackLp.addRule(ALIGN_PARENT_RIGHT);
            ivBackLp.addRule(CENTER_VERTICAL);
            addView(mIvBack, ivBackLp);

            mIvBack.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTuyaView.undo();
                }
            });

            setClickable(true);
            setBackgroundColor(context.getResources().getColor(R.color.BgToolBar));

            mCurrentColor = Color.parseColor("#FFFFFF");
            mStartX = Util.dip2px(25 + 4 + 20);

        }


        public int getCurrentColor() {
            return mCurrentColor;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (ev.getX() > mStartX && ev.getX() < mIvBack.getX() - 10) {
                        mIsTouchColor = true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mIsTouchColor && ev.getX() > mStartX && ev.getX() < mIvBack.getX() - 10) {
                        int i = (int) ((ev.getX() - mStartX) / mLength);
                        mCurrentColor = Color.parseColor(colors[i]);
                        mCurrentX = (int) ev.getX();
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mIsTouchColor) {
                        int upX = (int) ev.getX();
                        if (upX < mStartX) {
                            upX = mStartX;
                        } else if (upX > mIvBack.getX() - 10) {
                            upX = (int) (mIvBack.getX() - 10);
                        }
                        doXAnim(mStartX + ((upX - mStartX) / mLength) * mLength + mLength / 2);
                    }
                    break;
            }
            return true;
        }

        private void doXAnim(int endX) {
            ValueAnimator xAnimator = ValueAnimator.ofInt(mCurrentX, endX);
            int distanceX = Math.abs(endX - mCurrentX);
            if (distanceX < 100) {
                xAnimator.setDuration(100);
            } else if (distanceX > 500) {
                xAnimator.setDuration(500);
            } else {
                xAnimator.setDuration(distanceX);
            }

            xAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int x = (int) animation.getAnimatedValue();
                    int i = (x - mStartX) / mLength;
                    mCurrentColor = Color.parseColor(colors[i]);
                    mTuyaView.setPaintColor(mCurrentColor);
                    mCurrentX = x;
                    invalidate();
                }
            });
            xAnimator.start();
        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            mLength = (int) (mIvBack.getX() - Util.dip2px(25 + 4 + 20)) / colors.length;
            mCurrentX = mStartX + mLength / 2;
            super.onWindowFocusChanged(hasWindowFocus);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setColor(mCurrentColor);
            paint.setAntiAlias(true);
            canvas.drawCircle(Util.dip2px(25), getHeight() / 2, Util.dip2px(8), paint);

            canvas.drawRoundRect(new RectF(mCurrentX - Util.dip2px(3), getHeight() / 2 - Util.dip2px(10), mCurrentX + Util.dip2px(3), getHeight() / 2 + Util.dip2px(10)), Util.dip2px(5), Util.dip2px(5), paint);

            int startX = mStartX;
            for (int i = 0; i < colors.length; i++) {
                paint.setColor(Color.parseColor(colors[i]));
                Rect rect = new Rect(startX, getHeight() / 2 - Util.dip2px(4), startX + mLength, getHeight() / 2 + Util.dip2px(4));
                canvas.drawRect(rect, paint);
                startX += mLength;
            }

            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Util.dip2px(1));
            canvas.drawRoundRect(new RectF(mCurrentX - Util.dip2px(3), getHeight() / 2 - Util.dip2px(10), mCurrentX + Util.dip2px(3), getHeight() / 2 + Util.dip2px(10)), Util.dip2px(5), Util.dip2px(5), paint);
        }
    }
}
