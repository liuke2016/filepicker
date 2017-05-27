package com.lynn.filepicker.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.entity.ImageFile;
import com.lynn.filepicker.entity.event.EditImageEvent;
import com.lynn.filepicker.entity.event.ImageBrowserPickEvent;
import com.lynn.filepicker.widget.HackyViewPager;
import com.lynn.filepicker.widget.HideAbleToolbar;
import com.lynn.filepicker.widget.photoview.PhotoView;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class ImageBrowserActivity extends AppCompatActivity {
    public static final String IMAGE_BROWSER_INIT_INDEX = "ImageBrowserInitIndex";
    public static final String IMAGE_BROWSER_LIST = "ImageBrowserList";
    public static final String IMAGE_BROWSER_SELECTED_NUMBER = "ImageBrowserSelectedNumber";
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private int initIndex = 0;
    private int mCurrentIndex = 0;

    private HideAbleToolbar mTbImagePick;
    private ArrayList<ImageFile> mList = new ArrayList<>();
    private ImageView mSelectView;
    private MenuItem mMenuDone;

    private ArrayList<ImageFile> mSelectedList;
    private Disposable mDisposable;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_image_browser);
        mMaxNumber = FilePicker.getPickerConfig().getMaxNumber();
        mCurrentNumber = getIntent().getIntExtra(IMAGE_BROWSER_SELECTED_NUMBER, 0);
        initIndex = getIntent().getIntExtra(IMAGE_BROWSER_INIT_INDEX, 0);
        mCurrentIndex = initIndex;
        mList = getIntent().getParcelableArrayListExtra(IMAGE_BROWSER_LIST);
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Boolean granted) throws Exception {
                        if (granted) {
                            initView();
                        } else {
                            finish();
                        }
                    }
                });

    }

    private void initView() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int[] attribute = new int[]{android.R.attr.actionBarSize};
        TypedArray typedArray = getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
        float actionBarSize = typedArray.getDimension(0, -1);

        final HackyViewPager viewPager = new HackyViewPager(this);
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewPager.setBackgroundColor(Color.BLACK);
        addContentView(viewPager, layoutParams1);

        Context context = new ContextThemeWrapper(this, R.style.ToolbarTheme);
        mTbImagePick = new HideAbleToolbar(context);
        mTbImagePick.setBackgroundColor(getResources().getColor(R.color.BgToolBar));
        ViewGroup.LayoutParams layoutParams2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) actionBarSize);
        addContentView(mTbImagePick, layoutParams2);
        Util.immersiveStatusBar(this, mTbImagePick);
        mTbImagePick.setTitle(initIndex + 1 + "/" + mList.size());
        int toolBarColor = FilePicker.getPickerConfig().getSteepToolBarColor();
        if (toolBarColor != 0) {
            mTbImagePick.setBackgroundColor(toolBarColor);
        }
        int toolBarTitleTextColor = FilePicker.getPickerConfig().getToolBarTextColor();
        if (toolBarTitleTextColor != 0) {
            mTbImagePick.setTitleTextColor(toolBarTitleTextColor);
        }
        setSupportActionBar(mTbImagePick);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mTbImagePick.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThis();
            }
        });


        RelativeLayout rlBottomContainer = new RelativeLayout(this);
        rlBottomContainer.setBackgroundColor(getResources().getColor(R.color.BgToolBar));
        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dip2px(this, 50));
        layoutParams3.gravity = Gravity.BOTTOM;

        TextView tvEdit = new TextView(this);
        tvEdit.setText(R.string.edit);
        tvEdit.setTextColor(Color.WHITE);
        tvEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvEdit.setPadding(Util.dip2px(this, 10), 0, 0, 0);
        tvEdit.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rlBottomContainer.addView(tvEdit, layoutParams4);
        tvEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageBrowserActivity.this, ImageEditActivity.class);
                intent.putExtra("path", mList.get(mCurrentIndex).getPath());
                intent.putExtra("index", mCurrentIndex);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_in, 0);
            }
        });

        mSelectView = new ImageView(this);
        mSelectView.setImageResource(R.drawable.selector_cbx);
        mSelectView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        RelativeLayout.LayoutParams layoutParams5 = new RelativeLayout.LayoutParams(Util.dip2px(this, 60), ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams5.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlBottomContainer.addView(mSelectView, layoutParams5);
        mSelectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected() && isUpToMax()) {
                    Snackbar.make(getWindow().getDecorView(), getString(R.string.most_pick) + mMaxNumber + getString(R.string.files), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (v.isSelected()) {
                    mList.get(mCurrentIndex).setSelected(false);
                    mSelectedList.remove(mList.get(mCurrentIndex));
                    mCurrentNumber--;
                    v.setSelected(false);
                } else {
                    mList.get(mCurrentIndex).setSelected(true);
                    mSelectedList.add(mList.get(mCurrentIndex));
                    mCurrentNumber++;
                    v.setSelected(true);
                }
                mMenuDone.setTitle(getString(R.string.confirm) + "(" + mCurrentNumber + "/" + mMaxNumber + ")");
                RxBus.getDefault().post(new ImageBrowserPickEvent(v.isSelected(), mList.get(mCurrentIndex)));
            }
        });

        addContentView(rlBottomContainer, layoutParams3);
        mTbImagePick.doAnimTogetherWith(rlBottomContainer);

        mSelectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected() && isUpToMax()) {
                    Snackbar.make(getWindow().getDecorView(), getString(R.string.most_pick) + mMaxNumber + getString(R.string.files), Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (v.isSelected()) {
                    mList.get(mCurrentIndex).setSelected(false);
                    mSelectedList.remove(mList.get(mCurrentIndex));
                    mCurrentNumber--;
                    v.setSelected(false);
                } else {
                    mList.get(mCurrentIndex).setSelected(true);
                    mSelectedList.add(mList.get(mCurrentIndex));
                    mCurrentNumber++;
                    v.setSelected(true);
                }
                mMenuDone.setTitle(getString(R.string.confirm) + "(" + mCurrentNumber + "/" + mMaxNumber + ")");
                RxBus.getDefault().post(new ImageBrowserPickEvent(v.isSelected(), mList.get(mCurrentIndex)));
            }
        });

        viewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        final ImageBrowserAdapter imageBrowserAdapter = new ImageBrowserAdapter();
        viewPager.setAdapter(imageBrowserAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentIndex = position;
                mSelectView.setSelected(mList.get(mCurrentIndex).isSelected());
                mTbImagePick.setTitle(mCurrentIndex + 1 + "/" + mList.size());
            }
        });

        viewPager.setCurrentItem(initIndex, false);
        viewPager.setOffscreenPageLimit(0);
        mSelectView.setSelected(mList.get(mCurrentIndex).isSelected());

        viewPager.setOnSingleTapConfirmedListener(new HackyViewPager.OnSingleTapConfirmedListener() {
            @Override
            public void onSingleTagConfirmed(MotionEvent e) {
                mTbImagePick.toggle();
            }
        });


        mSelectedList = new ArrayList<>();
        for (ImageFile file : mList) {
            if (file.isSelected()) {
                mSelectedList.add(file);
            }
        }
        mDisposable = RxBus.getDefault().toObservable(EditImageEvent.class)
                .subscribe(new Consumer<EditImageEvent>() {
                    @Override
                    public void accept(EditImageEvent editImageEvent) throws Exception {

                        for (int i = 0; i < viewPager.getChildCount(); i++) {
                            if (viewPager.getChildAt(i).getGlobalVisibleRect(new Rect())) {
                                Glide.with(ImageBrowserActivity.this)
                                        .load(mList.get(mCurrentIndex).getPath())
                                        .crossFade()
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .placeholder(R.mipmap.ic_place_holder)
                                        .into((ImageView) viewPager.getChildAt(i));
                                break;
                            }
                        }

                    }
                });
    }


    @Override
    protected void onDestroy() {
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.zoom_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mMenuDone = menu.findItem(R.id.action_done);
        menu.findItem(R.id.action_record).setVisible(false);
        mMenuDone.setTitle(getString(R.string.confirm) + "(" + mCurrentNumber + "/" + mMaxNumber + ")");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            finishThis();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isUpToMax() {
        return mCurrentNumber >= mMaxNumber;
    }

    private void finishThis() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(FilePicker.RESULT_PICK, mList);
        intent.putExtra(IMAGE_BROWSER_SELECTED_NUMBER, mCurrentNumber);
        intent.putParcelableArrayListExtra("selectedList", mSelectedList);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finishThis();
    }

    private class ImageBrowserAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            final PhotoView view = new PhotoView(ImageBrowserActivity.this);
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(ImageBrowserActivity.this)
                    .load(mList.get(position).getPath())
                    .crossFade()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.mipmap.ic_place_holder)
                    .into(view);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }


}
