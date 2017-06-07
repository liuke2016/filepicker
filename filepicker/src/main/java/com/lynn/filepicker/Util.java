package com.lynn.filepicker;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.lynn.filepicker.mvp.PickerContract;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;


public class Util {
    public static float DEFAULT_ALPHA = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 0.2f : 0.3f;
    private static final int COLOR_INVALID_VAL = -1;

    private static Application mContext;


    public static Context getContext() {
        if (mContext == null) {
            Application app = null;
            try {
                app = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
                if (app == null)
                    throw new IllegalStateException("Static initialization of Applications must be on main thread.");
            } catch (final Exception e) {
                try {
                    app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            } finally {
                mContext = app;
            }
        }
        return mContext;
    }

    public static boolean detectIntent(Context ctx, Intent intent) {
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(
                intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static String getDurationString(long duration) {
//        long days = duration / (1000 * 60 * 60 * 24);
        long hours = (duration % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (duration % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (duration % (1000 * 60)) / 1000;

        String hourStr = (hours < 10) ? "0" + hours : hours + "";
        String minuteStr = (minutes < 10) ? "0" + minutes : minutes + "";
        String secondStr = (seconds < 10) ? "0" + seconds : seconds + "";

        if (hours != 0) {
            return hourStr + ":" + minuteStr + ":" + secondStr;
        } else {
            return minuteStr + ":" + secondStr;
        }
    }

    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    public static void immersiveStatusBar(Activity activity, Toolbar toolbar) {
        immersiveStatusBar(activity, DEFAULT_ALPHA, toolbar);
    }


    public static void immersiveStatusBar(Activity activity, @FloatRange(from = 0.0, to = 1.0) float alpha, Toolbar toolbar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            systemUiVisibility |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        ViewGroup decorView = (ViewGroup) window.getDecorView();
        ViewGroup contentView = (ViewGroup) window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        View rootView = contentView.getChildAt(0);
        int statusBarHeight = getStatusBarHeight(window.getContext());
        if (rootView != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) rootView.getLayoutParams();
            if (!ViewCompat.getFitsSystemWindows(rootView)) {
                ViewCompat.setFitsSystemWindows(rootView, true);
            }
            lp.topMargin = -statusBarHeight;
            rootView.setLayoutParams(lp);
            TypedValue typedValue = new TypedValue();
            activity.getTheme().resolveAttribute(android.R.attr.textAppearanceLarge, typedValue, true);
            int[] attribute = new int[]{android.R.attr.actionBarSize};
            TypedArray array = activity.obtainStyledAttributes(typedValue.resourceId, attribute);
            int actionBarSize = array.getDimensionPixelSize(0 /* index */, -1 /* default size */);
            array.recycle();
            toolbar.getLayoutParams().height = actionBarSize + statusBarHeight;
            toolbar.setPadding(0, statusBarHeight, 0, 0);
        }

        setTranslucentView(decorView, alpha);
    }

    public static void tintStatusBar(Activity activity) {
        tintStatusBar(activity, COLOR_INVALID_VAL, DEFAULT_ALPHA);
    }


    public static void tintStatusBar(Activity activity, @ColorInt int statusBarColor, @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }
        Window window = activity.getWindow();
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        ViewGroup contentView = (ViewGroup) window.getDecorView().findViewById(Window.ID_ANDROID_CONTENT);
        View rootView = contentView.getChildAt(0);
        if (rootView != null && !ViewCompat.getFitsSystemWindows(rootView)) {
            ViewCompat.setFitsSystemWindows(rootView, true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            if (statusBarColor != COLOR_INVALID_VAL) {
                window.setStatusBarColor(statusBarColor);
            }
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setStatusBar(decorView, statusBarColor, true);
            setTranslucentView(decorView, alpha);
        }


    }


    public static void setTranslucentView(ViewGroup container,
                                          @FloatRange(from = 0.0, to = 1.0) float alpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View translucentView = container.findViewById(R.id.

                    translucent_view);
            if (translucentView == null) {
                translucentView = new View(container.getContext());
                translucentView.setId(R.id.translucent_view);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.getContext()));
                container.addView(translucentView, lp);
            }

            translucentView.setBackgroundColor(Color.argb((int) (alpha * 255), 0, 0, 0));
        }
    }


    private static void setStatusBar(ViewGroup container, @ColorInt int statusBarColor, boolean visible) {
        setStatusBar(container, statusBarColor, visible, false);
    }


    private static void setStatusBar(ViewGroup container, @ColorInt int statusBarColor, boolean visible, boolean addToFirst) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View statusBarView = container.findViewById(R.id.statusbar_view);
            if (statusBarView == null) {
                statusBarView = new View(container.getContext());
                statusBarView.setId(R.id.statusbar_view);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(container.getContext()));
                if (addToFirst) {
                    container.addView(statusBarView, 0, lp);
                } else {
                    container.addView(statusBarView, lp);
                }
            }

            statusBarView.setBackgroundColor(statusBarColor);
            statusBarView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }


    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = context.getResources().getDimensionPixelSize(resId);
        }
        return result;
    }


    public static void launchCamera(final RequestPermission requestPermission, final PickerContract.IPickerView view) {
        RxPermissions rxPermissions = new RxPermissions((Activity) view);
        boolean isPermissionsGranted =
                rxPermissions
                        .isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                        rxPermissions
                                .isGranted(Manifest.permission.CAMERA);

        if (isPermissionsGranted) {
            requestPermission.onRequestPermissionSuccess();
        } else {
            rxPermissions
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE
                            , Manifest.permission.CAMERA)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(@NonNull Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                requestPermission.onRequestPermissionSuccess();
                            } else {
                                view.showMessage(((Activity) view).getResources().getString(R.string.request_permission_fail));
                            }
                        }
                    });
        }
    }

    public static void launchRecorder(final RequestPermission requestPermission, final PickerContract.IPickerView view) {
        RxPermissions rxPermissions = new RxPermissions((Activity) view);
        boolean isPermissionsGranted =
                rxPermissions
                        .isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                        rxPermissions
                                .isGranted(Manifest.permission.RECORD_AUDIO);
        if (isPermissionsGranted) {
            requestPermission.onRequestPermissionSuccess();
        } else {
            rxPermissions
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE
                            , Manifest.permission.RECORD_AUDIO)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(@NonNull Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                requestPermission.onRequestPermissionSuccess();
                            } else {
                                view.showMessage(((Activity) view).getResources().getString(R.string.request_permission_fail));
                            }
                        }
                    });
        }
    }

    public static void externalStorage(final RequestPermission requestPermission, final PickerContract.IPickerView view) {
        RxPermissions rxPermissions = new RxPermissions((Activity) view);
        boolean isPermissionsGranted =
                rxPermissions
                        .isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (isPermissionsGranted) {
            requestPermission.onRequestPermissionSuccess();
        } else {
            rxPermissions
                    .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(@NonNull Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                requestPermission.onRequestPermissionSuccess();
                            } else {
                                view.showMessage(((Activity) view).getResources().getString(R.string.request_permission_fail));
                            }
                        }
                    });
        }
    }

    public static String extractName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String extractDirectory(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }


    private static void clearImageDiskCache() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.get(getContext()).clearDiskCache();
                    }
                }).start();
            } else {
                Glide.get(getContext()).clearDiskCache();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void clearImageMemoryCache() {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Glide.get(getContext()).clearMemory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearImageAllCache() {
        clearImageDiskCache();
        clearImageMemoryCache();
    }

    public interface RequestPermission {
        void onRequestPermissionSuccess();
    }
}
