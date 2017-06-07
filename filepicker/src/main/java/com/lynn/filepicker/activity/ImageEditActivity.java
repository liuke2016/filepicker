package com.lynn.filepicker.activity;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.config.ImagePickerConfig;
import com.lynn.filepicker.entity.event.EditImageEvent1;
import com.lynn.filepicker.widget.HideAbleToolbar;
import com.lynn.filepicker.widget.ImageEditLayout;
import com.lynn.filepicker.widget.TuyaView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ImageEditActivity extends AppCompatActivity {

    private HideAbleToolbar mTbImageEdit;
    private ImageEditLayout mImageEditLayout;
    private AlertDialog mProgressDialog;

    private Disposable mDisposable1;
    private Disposable mDisposable2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        mImageEditLayout = new ImageEditLayout(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(mImageEditLayout, layoutParams);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int[] attribute = new int[]{android.R.attr.actionBarSize};
        TypedArray typedArray = getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
        float actionBarSize = typedArray.getDimension(0, -1);

        Context context = new ContextThemeWrapper(this, R.style.ToolbarTheme);
        mTbImageEdit = new HideAbleToolbar(context);
        ViewGroup.LayoutParams layoutParams2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) actionBarSize);

        TextView tvTitle = new TextView(this);
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tvTitle.setGravity(Gravity.CENTER_VERTICAL);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        tvTitle.setText(R.string.edit_image);
        mTbImageEdit.addView(tvTitle);

        TextView tvDone = new TextView(this);
        Toolbar.LayoutParams layoutParams5 = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams5.gravity = Gravity.RIGHT;
        layoutParams5.rightMargin = Util.dip2px(8);
        tvDone.setGravity(Gravity.CENTER_VERTICAL);
        tvDone.setTextColor(Color.WHITE);
        tvDone.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
        tvDone.setText(R.string.save);
        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.clearImageAllCache();
                Observable.just(true)
                        .map(new Function<Boolean, Boolean>() {
                            @Override
                            public Boolean apply(@NonNull Boolean aBoolean) throws Exception {
                                ImagePickerConfig config = (ImagePickerConfig) FilePicker.getPickerConfig();
                                mImageEditLayout.saveImage(config.getEditSavePath());
                                return true;
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) throws Exception {
                                showProgressDialog();
                            }
                        })
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .doOnTerminate(new Action() {
                            @Override
                            public void run() throws Exception {
                                mProgressDialog.dismiss();
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                RxBus.getDefault().post(new EditImageEvent1(getIntent().getIntExtra("index", 0)));
                                finish();
                            }
                        });
            }
        });
        mTbImageEdit.addView(tvDone,layoutParams5);

        addContentView(mTbImageEdit, layoutParams2);
        Util.immersiveStatusBar(this, mTbImageEdit);
        int toolBarColor = FilePicker.getPickerConfig().getSteepToolBarColor();
        if (toolBarColor != 0) {
            mTbImageEdit.setBackgroundColor(toolBarColor);
        } else {
            mTbImageEdit.setBackgroundColor(getResources().getColor(R.color.BgToolBar));

        }
        Drawable icBack = getResources().getDrawable(R.mipmap.ic_back);
        int toolBarTitleTextColor = FilePicker.getPickerConfig().getToolBarTextColor();
        if (toolBarTitleTextColor != 0) {
            mTbImageEdit.setTitleTextColor(toolBarTitleTextColor);
            tvTitle.setTextColor(toolBarTitleTextColor);
            tvDone.setTextColor(toolBarTitleTextColor);
            DrawableCompat.setTint(icBack,toolBarTitleTextColor);
        }
        mTbImageEdit.setNavigationIcon(icBack);
        mTbImageEdit.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTbImageEdit.doAnimTogetherWith(mImageEditLayout.getPaintConfigLayout());

        mImageEditLayout.setImage(getIntent().getStringExtra("path"));

        mDisposable1 = RxBus.getDefault().toObservable(TuyaView.SingleTapConfirmedEvent.class)
                .subscribe(new Consumer<TuyaView.SingleTapConfirmedEvent>() {
                    @Override
                    public void accept(TuyaView.SingleTapConfirmedEvent singleTapConfirmedEvent) throws Exception {
                        mTbImageEdit.toggle();
                    }
                });

        mDisposable2 = RxBus.getDefault().toObservable(TuyaView.StartDrawEvent.class)
                .subscribe(new Consumer<TuyaView.StartDrawEvent>() {
                    @Override
                    public void accept(TuyaView.StartDrawEvent startDrawEvent) throws Exception {
                        mTbImageEdit.hide();
                    }
                });
    }



    private void showProgressDialog() {
        if (mProgressDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LinearLayout dialogView = new LinearLayout(this);
            dialogView.setOrientation(LinearLayout.VERTICAL);
            ProgressBar progressBar = new ProgressBar(this);
            dialogView.addView(progressBar);
            TextView tvMessage = new TextView(this);
            tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tvMessage.setTextColor(Color.WHITE);
            tvMessage.setText(R.string.saving);
            tvMessage.setGravity(Gravity.CENTER);
            tvMessage.setPadding(0, Util.dip2px(5), 0, Util.dip2px(5));
            tvMessage.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dialogView.addView(tvMessage);
            builder.setView(dialogView);
            mProgressDialog = builder.create();
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
        WindowManager.LayoutParams params = mProgressDialog.getWindow().getAttributes();
        params.width = (int) (Util.getScreenWidth()*0.4);
        mProgressDialog.getWindow().setAttributes(params);
        mProgressDialog.getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.BgToolBar));
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoom_out);
    }

    @Override
    protected void onDestroy() {
        mDisposable1.dispose();
        mDisposable1 = null;
        mDisposable2.dispose();
        mDisposable2 = null;
        super.onDestroy();
    }
}
