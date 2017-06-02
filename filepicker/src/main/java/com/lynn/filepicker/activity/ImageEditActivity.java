package com.lynn.filepicker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.entity.event.EditImageEvent;
import com.lynn.filepicker.widget.HideAbleToolbar;
import com.lynn.filepicker.widget.ImageEditLayout;
import com.lynn.filepicker.widget.TuyaView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ImageEditActivity extends AppCompatActivity {

    private HideAbleToolbar mTbImageEdit;
    private Disposable mDisposable1;
    private Disposable mDisposable2;
    private ImageEditLayout mImageEditLayout;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_image_edit);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        mImageEditLayout = new ImageEditLayout(this);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(mImageEditLayout,layoutParams);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int[] attribute = new int[]{android.R.attr.actionBarSize};
        TypedArray typedArray = getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
        float actionBarSize = typedArray.getDimension(0, -1);

        Context context = new ContextThemeWrapper(this, R.style.ToolbarTheme);
        mTbImageEdit = new HideAbleToolbar(context);
        ViewGroup.LayoutParams layoutParams2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) actionBarSize);
        addContentView(mTbImageEdit, layoutParams2);
        Util.immersiveStatusBar(this, mTbImageEdit);
        mTbImageEdit.setTitle(R.string.edit_image);
        int toolBarColor = FilePicker.getPickerConfig().getSteepToolBarColor();
        if (toolBarColor != 0) {
            mTbImageEdit.setBackgroundColor(toolBarColor);
        }else{
            mTbImageEdit.setBackgroundColor(getResources().getColor(R.color.BgToolBar));

        }
        int toolBarTitleTextColor = FilePicker.getPickerConfig().getToolBarTextColor();
        if (toolBarTitleTextColor != 0) {
            mTbImageEdit.setTitleTextColor(toolBarTitleTextColor);
        }
        setSupportActionBar(mTbImageEdit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.action_record).setVisible(false);
        menu.findItem(R.id.action_done).setTitle(getString(R.string.save));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            Observable.just(mImageEditLayout.saveImage())
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
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            RxBus.getDefault().post(new EditImageEvent(getIntent().getIntExtra("index", 0)));
                            finish();
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressDialog.show();
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
