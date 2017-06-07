package com.lynn.filepicker.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.signature.StringSignature;
import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.config.ImagePickerConfig;
import com.lynn.filepicker.db.ImageDao;
import com.lynn.filepicker.entity.ImageFile;
import com.lynn.filepicker.entity.event.EditImageEvent1;
import com.lynn.filepicker.entity.event.EditImageEvent2;
import com.lynn.filepicker.entity.event.ImageBrowserPickEvent;
import com.lynn.filepicker.widget.HackyViewPager;
import com.lynn.filepicker.widget.HideAbleToolbar;
import com.lynn.filepicker.widget.SpacesItemDecoration;
import com.lynn.filepicker.widget.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

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

    private ArrayList<ImageFile> mSelectedThumbnailList = new ArrayList<>();
    private ArrayList<ImageFile> mSelectedList = new ArrayList<>();
    private ArrayList<String> mSelectedPosition = new ArrayList<>();
    private Disposable mDisposable;
    private RecyclerView mRecyclerView;
    private ImageThumbnailAdapter mImageThumbnailAdapter = new ImageThumbnailAdapter();
    private HackyViewPager mViewPager;
    private boolean mIsPreview;
    private ImageDao mImageDao;
    private TextView mTvDone;
    private TextView mTvTitle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMaxNumber = FilePicker.getPickerConfig().getMaxNumber();
        mCurrentNumber = getIntent().getIntExtra(IMAGE_BROWSER_SELECTED_NUMBER, 0);
        initIndex = getIntent().getIntExtra(IMAGE_BROWSER_INIT_INDEX, 0);
        mIsPreview = getIntent().getBooleanExtra("is_preview", false);
        mCurrentIndex = initIndex;
        mList = getIntent().getParcelableArrayListExtra(IMAGE_BROWSER_LIST);
        mImageDao = new ImageDao(Util.getContext());
        initView();

    }

    private void initView() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int[] attribute = new int[]{android.R.attr.actionBarSize};
        TypedArray typedArray = getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
        float actionBarSize = typedArray.getDimension(0, -1);

        mViewPager = new HackyViewPager(this);
        ViewGroup.LayoutParams layoutParams1 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mViewPager.setBackgroundColor(Color.BLACK);
        addContentView(mViewPager, layoutParams1);

        Context context = new ContextThemeWrapper(this, R.style.ToolbarTheme);
        mTbImagePick = new HideAbleToolbar(context);
        mTbImagePick.setBackgroundColor(getResources().getColor(R.color.BgToolBar));
        ViewGroup.LayoutParams layoutParams2 = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) actionBarSize);

        mTvTitle = new TextView(this);
        mTvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mTvTitle.setGravity(Gravity.CENTER_VERTICAL);
        mTvTitle.setTextColor(Color.WHITE);
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mTvTitle.setText(initIndex + 1 + "/" + mList.size());
        mTbImagePick.addView(mTvTitle);

        mTvDone = new TextView(this);
        Toolbar.LayoutParams layoutParams5 = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams5.gravity = Gravity.RIGHT;
        layoutParams5.rightMargin = Util.dip2px(8);
        mTvDone.setGravity(Gravity.CENTER_VERTICAL);
        mTvDone.setTextColor(Color.WHITE);
        mTvDone.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
        mTvDone.setText(getString(R.string.confirm) + "(" + mCurrentNumber + "/" + mMaxNumber + ")");
        mTvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThis();
            }
        });
        mTbImagePick.addView(mTvDone,layoutParams5);
        int toolBarColor = FilePicker.getPickerConfig().getSteepToolBarColor();
        if (toolBarColor != 0) {
            mTbImagePick.setBackgroundColor(toolBarColor);
        }
        Drawable icBack = getResources().getDrawable(R.mipmap.ic_back);
        int toolBarTitleTextColor = FilePicker.getPickerConfig().getToolBarTextColor();
        if (toolBarTitleTextColor != 0) {
            mTbImagePick.setTitleTextColor(toolBarTitleTextColor);
            mTvTitle.setTextColor(toolBarTitleTextColor);
            mTvDone.setTextColor(toolBarTitleTextColor);
            DrawableCompat.setTint(icBack,toolBarTitleTextColor);
        }
        mTbImagePick.setNavigationIcon(icBack);
        mTbImagePick.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThis();
            }
        });

        addContentView(mTbImagePick, layoutParams2);
        Util.immersiveStatusBar(this, mTbImagePick);

        LinearLayout llBottomContainer = new LinearLayout(this);
        llBottomContainer.setBackgroundColor(getResources().getColor(R.color.BgToolBar));
        llBottomContainer.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams3.gravity = Gravity.BOTTOM;


        mRecyclerView = new RecyclerView(this);
        mRecyclerView.setPadding(Util.dip2px(10), Util.dip2px(10), Util.dip2px(10), Util.dip2px(10));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dip2px(90)));
        mRecyclerView.setItemAnimator(new CustomItemAnimator());
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(Util.dip2px(5)));
        mRecyclerView.setAdapter(mImageThumbnailAdapter);
        llBottomContainer.addView(mRecyclerView);

        for (int i = 0; i < mList.size(); i++) {
            ImageFile file = mList.get(i);
            if (file.isSelected()) {
                mSelectedList.add(file);
                mSelectedThumbnailList.add(file);
                mSelectedPosition.add(i + "");
            }
        }

        if (mSelectedThumbnailList.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mImageThumbnailAdapter.notifyDataSetChanged();
        }

        View cut = new View(this);
        cut.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
        cut.setBackgroundColor(Color.GRAY);
        llBottomContainer.addView(cut);

        RelativeLayout rlBottomContainer = new RelativeLayout(this);
        rlBottomContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dip2px(45)));

        ImagePickerConfig config = (ImagePickerConfig) FilePicker.getPickerConfig();
        if(config.isNeedEdit()){
            TextView tvEdit = new TextView(this);
            tvEdit.setText(R.string.edit);
            tvEdit.setTextColor(Color.WHITE);
            tvEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            tvEdit.setPadding(Util.dip2px(10), 0, 0, 0);
            tvEdit.setGravity(Gravity.CENTER_VERTICAL);
            tvEdit.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            rlBottomContainer.addView(tvEdit);
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
        }


        mSelectView = new ImageView(this);
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_selected},getResources().getDrawable(R.mipmap.ic_checked));
        drawable.addState(new int[]{},getResources().getDrawable(R.mipmap.ic_uncheck));
        mSelectView.setImageDrawable(drawable);
        mSelectView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(Util.dip2px(60), ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlBottomContainer.addView(mSelectView, layoutParams4);
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
                    if (!mIsPreview) {
                        mSelectedThumbnailList.remove(mList.get(mCurrentIndex));
                        mSelectedPosition.remove(mCurrentIndex + "");
                    }
                    mCurrentNumber--;
                    v.setSelected(false);
                    if (mSelectedList.size() == 0) {
                        if (!mIsPreview)
                            mRecyclerView.setVisibility(View.GONE);
                        else
                            mImageThumbnailAdapter.notifyDataSetChanged();
                    } else {
                        mImageThumbnailAdapter.notifyDataSetChanged();
                    }
                } else {
                    mList.get(mCurrentIndex).setSelected(true);
                    mSelectedList.add(mList.get(mCurrentIndex));
                    if (!mIsPreview) {
                        mSelectedThumbnailList.add(mList.get(mCurrentIndex));
                        mSelectedPosition.add(mCurrentIndex + "");
                    }
                    mCurrentNumber++;
                    v.setSelected(true);
                    mImageThumbnailAdapter.notifyDataSetChanged();
                    if (mSelectedList.size() == 1) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerView.scrollToPosition(mSelectedPosition.size() - 1);
                    }
                }
                mTvDone.setText(getString(R.string.confirm) + "(" + mCurrentNumber + "/" + mMaxNumber + ")");
                RxBus.getDefault().post(new ImageBrowserPickEvent(v.isSelected(), mList.get(mCurrentIndex)));
            }
        });
        llBottomContainer.addView(rlBottomContainer);

        addContentView(llBottomContainer, layoutParams3);
        mTbImagePick.doAnimTogetherWith(llBottomContainer);


        mViewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        final ImageBrowserAdapter imageBrowserAdapter = new ImageBrowserAdapter();
        mViewPager.setAdapter(imageBrowserAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                mTvTitle.setText(mCurrentIndex + 1 + "/" + mList.size());
                mImageThumbnailAdapter.notifyDataSetChanged();
                for (int i = 0; i < mSelectedPosition.size(); i++) {
                    int p = Integer.valueOf(mSelectedPosition.get(i));
                    if (position == p) {
                        mRecyclerView.scrollToPosition(i);
                    }
                }
            }
        });

        mViewPager.setCurrentItem(initIndex, false);
        mViewPager.setOffscreenPageLimit(0);
        mSelectView.setSelected(mList.get(mCurrentIndex).isSelected());

        mViewPager.setOnSingleTapConfirmedListener(new HackyViewPager.OnSingleTapConfirmedListener() {
            @Override
            public void onSingleTagConfirmed(MotionEvent e) {
                mTbImagePick.toggle();
            }
        });


        mDisposable = RxBus.getDefault().toObservable(EditImageEvent1.class)
                .subscribe(new Consumer<EditImageEvent1>() {
                    @Override
                    public void accept(EditImageEvent1 editImageEvent1) throws Exception {
                        ImageFile file = mList.get(editImageEvent1.getIndex());
                        file.setEditCount(file.getEditCount() + 1);
                        file.setEditedPath(((ImagePickerConfig) FilePicker.getPickerConfig()).getEditSavePath() + File.separator + new File(file.getPath()).getName());
                        if (file.getEditCount() == 1) {
                            mImageDao.insert(file);
                        } else {
                            mImageDao.update(file);
                        }
                        RxBus.getDefault().post(new EditImageEvent2(editImageEvent1.getIndex()));
                        for (int i = 0; i < mViewPager.getChildCount(); i++) {
                            if (mViewPager.getChildAt(i).getGlobalVisibleRect(new Rect())) {
                                Glide.with(ImageBrowserActivity.this)
                                        .load(file.getEditedPath())
                                        .signature(new StringSignature(file.getEditCount() + ""))
                                        .crossFade()
                                        .placeholder(R.mipmap.ic_place_holder)
                                        .into((ImageView) mViewPager.getChildAt(i));
                                break;
                            }
                        }
                        mImageThumbnailAdapter.notifyDataSetChanged();

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
            ImageFile file = mList.get(position);
            RequestManager requestManager = Glide.with(ImageBrowserActivity.this);
            DrawableRequestBuilder requestBuilder;
            if (file.getEditCount() > 0) {
                requestBuilder = requestManager.load(file.getEditedPath())
                        .signature(new StringSignature(file.getEditCount() + ""));
            } else {
                requestBuilder = requestManager.load(file.getPath());
            }
            requestBuilder
                    .crossFade()
                    .placeholder(R.mipmap.ic_place_holder).into(view);
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

    private class ImageThumbnailAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            FrameLayout itemView = new FrameLayout(ImageBrowserActivity.this);
            ImageView imageView = new ImageView(ImageBrowserActivity.this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(Util.dip2px(70), Util.dip2px(70)));
            itemView.addView(imageView);

            View shadow = new View(ImageBrowserActivity.this);
            shadow.setLayoutParams(new ViewGroup.LayoutParams(Util.dip2px(70), Util.dip2px(70)));
            shadow.setBackgroundColor(Color.parseColor("#AAFFFFFF"));
            itemView.addView(shadow);
            shadow.setVisibility(View.GONE);

            ImageView ivBlock = new ImageView(ImageBrowserActivity.this);
            ivBlock.setLayoutParams(new ViewGroup.LayoutParams(Util.dip2px(70), Util.dip2px(70)));
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(Color.TRANSPARENT);
            drawable.setStroke(Util.dip2px(2), Color.GREEN);
            ivBlock.setImageDrawable(drawable);
            itemView.addView(ivBlock);

            return new ImageThumbnailHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ImageThumbnailHolder viewHolder = (ImageThumbnailHolder) holder;

            ImageFile file = mSelectedThumbnailList.get(position);

            RequestManager requestManager = Glide.with(ImageBrowserActivity.this);
            DrawableRequestBuilder requestBuilder;
            if (file.getEditCount() > 0) {
                requestBuilder = requestManager.load(file.getEditedPath())
                        .signature(new StringSignature(file.getEditCount() + ""));
            } else {
                requestBuilder = requestManager.load(file.getPath());
            }
            requestBuilder.centerCrop()
                    .crossFade()
                    .placeholder(R.mipmap.ic_place_holder).into(viewHolder.imageView);

            if (mList.get(mCurrentIndex).getId() == file.getId()) {
                viewHolder.ivBlock.setVisibility(View.VISIBLE);
            } else {
                viewHolder.ivBlock.setVisibility(View.GONE);
            }
            if (mIsPreview) {
                viewHolder.shadow.setVisibility(file.isSelected() ? View.GONE : View.VISIBLE);
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(Integer.valueOf(mSelectedPosition.get(position)), false);
                    mRecyclerView.scrollToPosition(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSelectedThumbnailList.size();
        }

        private class ImageThumbnailHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            View shadow;
            ImageView ivBlock;

            public ImageThumbnailHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) ((ViewGroup) itemView).getChildAt(0);
                shadow = ((ViewGroup) itemView).getChildAt(1);
                ivBlock = (ImageView) ((ViewGroup) itemView).getChildAt(2);
            }
        }
    }

    private class CustomItemAnimator extends DefaultItemAnimator {
        @Override
        public boolean animateAdd(RecyclerView.ViewHolder holder) {
            return super.animateAdd(holder);
        }
    }
}
