package com.lynn.filepicker.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.lynn.filepicker.RxBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TuyaView extends View {
    private GestureDetector mDetector;
    private Context context;
    private Bitmap mBitmapInit;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private List<DrawPath> savePath;
    private List<DrawPath> deletePath;
    private DrawPath dp;
    private float screenWidth, screenHeight;
    private int currentColor = Color.RED;
    private int currentSize = 5;
    private int currentStyle = 1;
    private Paint mMaskPaint;
    private boolean startDraw = false;
    private boolean needZoomView = false;

    private int mode = 0;
    private static final int DRAW = 1;
    private static final int ZOOM = 2;

    private float oldDist = 1f;
    private PointF zoomMidPoint;
    private float mScale = 1f;
    private float mDownX;
    private float mDownY;

    private class DrawPath {
        public Path path;
        public Paint paint;
    }

    public TuyaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = manager.getDefaultDisplay().getWidth();
        screenHeight = manager.getDefaultDisplay().getHeight();
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();
    }

    public TuyaView(Context context, int w, int h) {
        super(context);
        this.context = context;
        screenWidth = w;
        screenHeight = h;
        savePath = new ArrayList<DrawPath>();
        deletePath = new ArrayList<DrawPath>();

        mDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                RxBus.getDefault().post(new SingleTapConfirmedEvent());
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        screenHeight = MeasureSpec.getSize(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void initCanvas() {

        setPaintStyle();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        if (mBitmapInit != null) {
            drawBitmapToCanvas(mBitmapInit);
        }
    }

    private void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        if (currentStyle == 1) {
            mPaint.setStrokeWidth(currentSize);
            mPaint.setColor(currentColor);
        } else {
            mPaint.setAlpha(0);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mPaint.setColor(Color.TRANSPARENT);
            mPaint.setStrokeWidth(50);
        }

        mMaskPaint = new Paint();
        mMaskPaint.setStrokeWidth(3);
        mMaskPaint.setTextAlign(Paint.Align.RIGHT);
        mMaskPaint.setColor(Color.GREEN);
        mMaskPaint.setTextSize(40);
    }

    @Override
    public void onDraw(Canvas canvas) {
        initCanvas();
        Iterator iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = (DrawPath) iter.next();
            if (null != drawPath && null != mCanvas) {
                mCanvas.drawPath(drawPath.path, drawPath.paint);
            }
        }
        if (mBitmap != null) {
            canvas.translate(getWidth() / 2 - mBitmap.getWidth() / 2, getHeight() / 2 - mBitmap.getHeight() / 2);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            if (mPath != null) {
                canvas.drawPath(mPath, mPaint);
            }
        }

    }

    public boolean isNeedZoomView() {
        return needZoomView;
    }

    public void setNeedZoomView(boolean needZoomView) {
        this.needZoomView = needZoomView;
    }


    public void undo() {
        if (savePath != null && savePath.size() > 0) {
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            deletePath.add(drawPath);
            savePath.remove(savePath.size() - 1);
            redrawOnBitmap();
        }
    }


    public void redo() {
        if (savePath != null && savePath.size() > 0) {
            savePath.clear();
            redrawOnBitmap();
        }
    }

    private void redrawOnBitmap() {
        initCanvas();
        Iterator iter = savePath.iterator();
        while (iter.hasNext()) {
            DrawPath drawPath = (DrawPath) iter.next();
            mCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();
    }


    public void recover() {
        if (deletePath.size() > 0) {
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            savePath.add(dp);
            mCanvas.drawPath(dp.path, dp.paint);
            deletePath.remove(deletePath.size() - 1);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        float x = event.getX() - (getWidth() / 2 - mBitmap.getWidth() / 2);
        float y = event.getY() - (getHeight() / 2 - mBitmap.getHeight() / 2);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAW;
                startDraw = true;
                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAW) {
                    if (Math.abs(mDownX - x) > 10 || Math.abs(mDownY - y) > 10) {
                        RxBus.getDefault().post(new StartDrawEvent());
                    }
                    if (startDraw) {
//                        double v = Math.sqrt((mDownX - x) * (mDownX - x) + (mDownY - y) * (mDownY - y));
//                        if (v > 15) {

//                        }
                        mPath = new Path();
                        dp = new DrawPath();
                        dp.paint = mPaint;
                        dp.path = mPath;
                        touch_start(x, y);
                        startDraw = false;
                    }
                    touch_move(x, y);
                } else if (mode == ZOOM) {
                    if (needZoomView) {
                        handleZoom(event);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mode == DRAW) {
                    touch_up();
                }
                mode = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (needZoomView)
                    mode = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (!needZoomView) break;
                startDraw = false;
                if (mode == DRAW) {
                    mPath = new Path();
                    dp = new DrawPath();
                    dp.paint = mPaint;
                    dp.path = mPath;
                    invalidate();
                }
                mode = ZOOM;
                oldDist = getFingerSpacing(event);
                zoomMidPoint = mid(event);
                break;
        }
        return true;

    }

    private void touch_start(float x, float y) {
        if (mPath == null) return;
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        invalidate();
    }

    private void touch_move(float x, float y) {
        if (mPath == null) return;
        float dx = Math.abs(x - mX);
        float dy = Math.abs(mY - y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            //mPath.lineTo(mX,mY);
            mX = x;
            mY = y;
        }
        invalidate();
    }

    private void touch_up() {
        if (mPath == null) return;
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        savePath.add(dp);
        mPath = null;
        invalidate();
    }


    private void handleZoom(MotionEvent event) {
        float newDist = getFingerSpacing(event);
        if (newDist > 20f) {
            if (newDist > oldDist) {
                mScale += 0.05f;
            } else if (newDist < oldDist) {
                mScale -= 0.05f;
            }

            if (mScale <= 1f) {
                mScale = 1f;
            } else if (mScale >= 5f) {
                mScale = 5f;
            }

            if (mScale >= 1f && mScale <= 5) {
                setPivotX(zoomMidPoint.x);
                setPivotY(zoomMidPoint.y);
                setScaleX(mScale);
                setScaleY(mScale);
            }
            oldDist = newDist;
        }
    }


    private static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


    private static PointF mid(MotionEvent event) {
        float midx = event.getX(1) + event.getX(0);
        float midy = event.getY(1) + event.getY(0);

        return new PointF(midx / 2, midy / 2);
    }

    public void saveToSDCard(String filePath) {
        mBitmap = toConformBitmap(mBitmap, rectF);
        compress(mBitmap, filePath, 100, 1024 * 300);
    }

    private void compress(Bitmap mBitmap, String filePath, int quality, int maxSize) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        try {
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (file.length() > maxSize) {
            quality -= 10;
            compress(mBitmap, filePath, quality, maxSize);
        }
    }

    private Bitmap toConformBitmap(Bitmap foreground, RectF imageRF) {
        if (imageRF == null) {
            int bgWidth = foreground.getWidth();
            int bgHeight = foreground.getHeight();
            Canvas cv = new Canvas(foreground);
            cv.drawBitmap(foreground, 0, 0, null);
            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
            return foreground;
        } else {
            int bgWidth = (int) imageRF.width();
            int bgHeight = (int) imageRF.height();
            Bitmap bitmap = Bitmap.createBitmap(foreground, (int) imageRF.left, (int) imageRF.top, (int) imageRF.width(), (int) imageRF.height());
            Canvas cv = new Canvas(bitmap);
            cv.drawBitmap(bitmap, 0, 0, null);
            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();
            return bitmap;
        }
    }

    public void selectPaintStyle(int which) {
        if (which == 0) {
            currentStyle = 1;
            setPaintStyle();
        }
        if (which == 1) {
            currentStyle = 2;
            setPaintStyle();
        }
    }

    public void selectPaintSize(int which) {
        //int size = Integer.parseInt(this.getResources().getStringArray(R.array.paintsize)[which]);
        currentSize = which;
        setPaintStyle();
    }

    public void setPaintColor(int color) {
        currentColor = color;
        setPaintStyle();
    }


    public void setBitmap(Uri uri) {
        ContentResolver cr = context.getContentResolver();
        try {
            mBitmapInit = BitmapFactory.decodeStream(cr.openInputStream(uri));
            calculateRect(mBitmapInit);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmapInit = bitmap;
        calculateRect(mBitmapInit);
        invalidate();
    }


    private RectF rectF;

    private void drawBitmapToCanvas(Bitmap bitmap) {
        mBitmap = Bitmap.createBitmap((int) rectF.width(), (int) rectF.height(), Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.argb(0, 0, 0, 0));
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.TRANSPARENT);
        if (rectF != null) {
            mCanvas.drawBitmap(bitmap, null, rectF, mBitmapPaint);
        } else {
            mCanvas.drawBitmap(bitmap, 0, 0, mBitmapPaint);
        }
    }


    public void calculateRect(Bitmap bitmap) {
        float bScale = (float) bitmap.getWidth() / bitmap.getHeight();
        float bitmapHeight = screenWidth / bScale;
        if (bitmapHeight < screenHeight) {
            float whiteHeight = screenHeight - bitmapHeight;
            rectF = new RectF(0, 0, screenWidth, bitmapHeight);
        } else if (bitmapHeight >= screenHeight) {
            float bitmapWidth = screenHeight * bScale;
            float whiteHeight = screenWidth - bitmapWidth;
            rectF = new RectF(0, 0, bitmapWidth, screenHeight);
        }
    }

    public class StartDrawEvent {

    }

    public class SingleTapConfirmedEvent {

    }
}
