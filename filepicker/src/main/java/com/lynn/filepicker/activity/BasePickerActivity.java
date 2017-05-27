package com.lynn.filepicker.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.adapter.BasePickerAdapter;
import com.lynn.filepicker.entity.BaseFile;
import com.lynn.filepicker.entity.Folder;
import com.lynn.filepicker.entity.event.FileClickEvent;
import com.lynn.filepicker.entity.event.FolderClickEvent;
import com.lynn.filepicker.mvp.BasePickerPresenter;
import com.lynn.filepicker.mvp.PickerContract;
import com.lynn.filepicker.widget.FolderDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;
import static com.lynn.filepicker.activity.AudioPickerActivity.REQUEST_CODE_TAKE_AUDIO;
import static com.lynn.filepicker.activity.ImageBrowserActivity.IMAGE_BROWSER_LIST;
import static com.lynn.filepicker.activity.ImageBrowserActivity.IMAGE_BROWSER_SELECTED_NUMBER;

public abstract class BasePickerActivity<T extends BaseFile> extends AppCompatActivity implements PickerContract.IPickerView {
    protected BasePickerPresenter mPresenter;
    protected Toolbar mToolBar;
    protected MenuItem mMenuDone;
    protected MenuItem mMenuRecord;
    protected int mMaxNumber;
    protected BasePickerAdapter<T> mAdapter;
    protected RecyclerView mRecyclerView;
    public static final int COLUMN_NUMBER = 3;

    protected TextView mTvTime;

    private RelativeLayout mRlBottomContainer;

    protected TextView mTvFolder;
    protected FolderDialog mFolderDialog;
    protected ProgressBar mProgressBar;
    private ObjectAnimator mShowTimeAnim;
    private ObjectAnimator mHideTimeAnim;
    private Runnable mHideAnimTask;
    private float mStartY;

    private boolean mIsLoadDataComplete;
    private Disposable mFileClickDisposable;
    private Disposable mFolderClickDisposable;
    protected TextView mTvPreview;
    protected LinearLayout mContentView;
    private LinearLayout mEmptyView;
    private ImageView mEmptyImageView;
    private TextView mEmptyTextView;
    private TextView mTvTip;
    private FileObserver mFileObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMaxNumber = FilePicker.getPickerConfig().getMaxNumber();
        Util.externalStorage(new Util.RequestPermission() {
            @Override
            public void onRequestPermissionSuccess() {
                loadFiles(false);
            }
        }, this);
        initView();
        initEvent();
        mFileObserver = new FileObserver(new Handler());
        getContentResolver().registerContentObserver(getExternalContentUri(), true,mFileObserver );
    }

    abstract protected Uri getExternalContentUri();

    private void initView() {
        setTheme(R.style.ToolbarTheme);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        int[] attribute = new int[]{android.R.attr.actionBarSize};
        TypedArray typedArray = getTheme().obtainStyledAttributes(typedValue.resourceId, attribute);
        float actionBarSize = typedArray.getDimension(0, -1);
        mContentView = new LinearLayout(this);
        mContentView.setOrientation(LinearLayout.VERTICAL);
        mToolBar = new Toolbar(this);
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.setTitle(getToolbarTitle());
        int toolBarColor = FilePicker.getPickerConfig().getSteepToolBarColor();
        if (toolBarColor != 0) {
            mToolBar.setBackgroundColor(toolBarColor);
        } else {
            mToolBar.setBackgroundColor(getResources().getColor(R.color.BgToolBar));
        }
        int toolBarTitleTextColor = FilePicker.getPickerConfig().getToolBarTextColor();
        if (toolBarTitleTextColor != 0) {
            mToolBar.setTitleTextColor(toolBarTitleTextColor);
        }
        LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) actionBarSize);
        mToolBar.setLayoutParams(tbLp);
        mContentView.addView(mToolBar);

        mRecyclerView = new RecyclerView(this);
        LinearLayout.LayoutParams rvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        mRecyclerView.setLayoutParams(rvLp);
        mContentView.addView(mRecyclerView);
        initRecyclerView(mRecyclerView);


        mRlBottomContainer = new RelativeLayout(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dip2px(this, 50));
        mRlBottomContainer.setBackgroundColor(Color.parseColor("#373B3E"));
        mRlBottomContainer.setVisibility(View.GONE);
        mTvFolder = new TextView(this);
        mTvFolder.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mTvFolder.setTextColor(Color.WHITE);
        mTvFolder.setGravity(Gravity.CENTER_VERTICAL);
        Drawable drawable = getResources().getDrawable(R.mipmap.ic_select_album);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mTvFolder.setCompoundDrawables(null, null, drawable, null);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams3.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams3.leftMargin = Util.dip2px(this, 15);
        mTvFolder.setLayoutParams(layoutParams3);
        mTvPreview = new TextView(this);
        mTvPreview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mTvPreview.setTextColor(Color.GRAY);
        mTvPreview.setGravity(Gravity.CENTER_VERTICAL);
        mTvPreview.setText(R.string.preview);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams4.addRule(RelativeLayout.CENTER_VERTICAL);
        layoutParams4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams4.rightMargin = Util.dip2px(this, 15);
        mTvPreview.setLayoutParams(layoutParams4);
        mTvPreview.setVisibility(View.GONE);
        mTvPreview.setEnabled(false);
        mRlBottomContainer.addView(mTvFolder);
        mRlBottomContainer.addView(mTvPreview);
        mContentView.addView(mRlBottomContainer, layoutParams2);

        setContentView(mContentView);
        Util.immersiveStatusBar(this, mToolBar);


        mTvTime = new TextView(this);
        mTvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        mTvTime.setTextColor(Color.WHITE);
        mTvTime.setPadding(Util.dip2px(this, 10), 0, 0, 0);
        mTvTime.setGravity(Gravity.CENTER_VERTICAL);
        mTvTime.setBackgroundColor(Color.parseColor("#66000000"));
        mTvTime.setAlpha(0);
        mTvTime.setVisibility(View.GONE);
        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dip2px(this, 25));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layoutParams1.topMargin = (int) actionBarSize + Util.getStatusBarHeight(this);
        } else {
            layoutParams1.topMargin = (int) actionBarSize;
        }

        addContentView(mTvTime, layoutParams1);


        mProgressBar = new ProgressBar(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        addContentView(mProgressBar, layoutParams);

        mEmptyView = new LinearLayout(this);
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setOrientation(LinearLayout.VERTICAL);
        mEmptyImageView = new ImageView(this);
        mEmptyImageView.setLayoutParams(new LinearLayout.LayoutParams(Util.dip2px(this, 76), Util.dip2px(this, 76)));
        mEmptyTextView = new TextView(this);
        mEmptyTextView.setGravity(Gravity.CENTER);
        mEmptyTextView.setPadding(Util.dip2px(this, 5), Util.dip2px(this, 5), Util.dip2px(this, 5), Util.dip2px(this, 5));
        mEmptyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mEmptyTextView.setText(R.string.empty);
        mTvTip = new TextView(this);
        mTvTip.setPadding(Util.dip2px(this, 5), Util.dip2px(this, 5), Util.dip2px(this, 5), Util.dip2px(this, 5));
        mTvTip.setTextColor(Color.parseColor("#727272"));
        mTvTip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        ViewGroup.LayoutParams tvLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTvTip.setLayoutParams(tvLp);
        mEmptyView.addView(mEmptyImageView);
        mEmptyView.addView(mEmptyTextView);
        mEmptyView.addView(mTvTip);
        addContentView(mEmptyView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mEmptyView.setVisibility(View.GONE);

        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFolderDialog = new FolderDialog(this);

        mShowTimeAnim = ObjectAnimator.ofFloat(mTvTime, View.ALPHA, 1.0f);
        mShowTimeAnim.setDuration(500);
        mShowTimeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mTvTime.setVisibility(View.VISIBLE);
            }
        });

        mHideTimeAnim = ObjectAnimator.ofFloat(mTvTime, View.ALPHA, 0);
        mHideTimeAnim.setDuration(250);
        mHideTimeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mTvTime.setVisibility(View.GONE);
            }
        });
        mHideAnimTask = new Runnable() {
            @Override
            public void run() {
                mHideTimeAnim.start();
            }
        };
    }

    protected abstract void initRecyclerView(RecyclerView recyclerView);

    protected abstract String getToolbarTitle();

    protected void initEvent() {
        mFolderDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mRecyclerView, View.ALPHA, 0.3f);
                animator.setDuration(200);
                animator.start();
            }
        });

        mFolderDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ObjectAnimator animator = ObjectAnimator.ofFloat(mRecyclerView, View.ALPHA, 1.0f);
                animator.setDuration(200);
                animator.start();
            }
        });

        mFolderClickDisposable = RxBus.getDefault().toObservable(FolderClickEvent.class)
                .subscribe(new Consumer<FolderClickEvent>() {
                    @Override
                    public void accept(@NonNull FolderClickEvent folderClickEvent) throws Exception {
                        Folder folder = folderClickEvent.getFolder();

                        onChangeFolder(folderClickEvent);

                        if (!folder.isSelected()) {
                            for (Folder f : folderClickEvent.getFolders()) {
                                f.setSelected(false);
                            }
                            folder.setSelected(true);
                            List<T> dataFiles = folder.getFiles();
                            for (BaseFile file : mAdapter.getSelectedList()) {
                                int index = dataFiles.indexOf(file);
                                if (index != -1) {
                                    dataFiles.get(index).setSelected(true);
                                }
                            }
                            mAdapter.refresh(dataFiles);
                            mFolderDialog.notifyDataSetChanged();
                            mTvFolder.setText(folder.getName() + "(" + folder.getFiles().size() + ")");
                        }
                        mFolderDialog.dismiss();
                    }
                });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int mScrollState;

            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
                mScrollState = scrollState;
                if (scrollState == SCROLL_STATE_IDLE) {
                    mTvTime.postDelayed(mHideAnimTask, 500);
                }
            }

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (mScrollState == SCROLL_STATE_SETTLING) {
                    setTvTimeText();
                }
            }
        });

        mTvFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFolderDialog.isShowing()) {
                    mFolderDialog.dismiss();
                } else {
                    mFolderDialog.show();
                }
            }
        });

        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //选择文件事件
        mFileClickDisposable = RxBus.getDefault().toObservable(FileClickEvent.class)
                .subscribe(new Consumer<FileClickEvent>() {
                    @Override
                    public void accept(@NonNull FileClickEvent fileClickEvent) throws Exception {
                        mMenuDone.setTitle(getString(R.string.confirm) + "(" + mAdapter.getSelectedList().size() + "/" + mMaxNumber + ")");

                        onSelectFile();

                    }
                });
        mTvPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BasePickerActivity.this, ImageBrowserActivity.class);
                intent.putParcelableArrayListExtra(IMAGE_BROWSER_LIST, mAdapter.getSelectedList());
                intent.putExtra(IMAGE_BROWSER_SELECTED_NUMBER, mAdapter.getSelectedList().size());
                startActivityForResult(intent, ImagePickerActivity.REQUEST_CODE_BROWSER_IMAGE);
                overridePendingTransition(R.anim.zoom_in, 0);
            }
        });
    }

    public void setEmptyImageView(Drawable drawable) {
        mEmptyImageView.setImageDrawable(drawable);
    }

    public void setEmptyImageView(@DrawableRes int id) {
        mEmptyImageView.setImageResource(id);
    }

    public void setEmptyTvTip(String text) {
        mTvTip.setText(text);
    }

    public void setEmptyTvTipTextColor(int color) {
        mTvTip.setTextColor(color);
    }

    public void setEmptyTextViewText(String text) {
        mEmptyTextView.setText(text);
    }

    public void setEmptyTextViewTextColor(int color) {
        mEmptyTextView.setTextColor(color);
    }

    public void setEmptyImageViewAction(View.OnClickListener onClickListener) {
        mEmptyImageView.setOnClickListener(onClickListener);
    }


    private void setTvTimeText() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int index = layoutManager.findFirstVisibleItemPosition() + 1 == mRecyclerView.getAdapter().getItemCount() ? mRecyclerView.getAdapter().getItemCount() - 1 : layoutManager.findFirstVisibleItemPosition() + 1;
        if (index >= 0) {
            BaseFile file = mAdapter.get(index);
            if (file != null) {
                mTvTime.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date(file.getDate() * 1000)).toString());
            }
        }
    }

    protected abstract void loadFiles(boolean isRefresh);


    protected void onSelectFile() {

    }

    protected void onChangeFolder(FolderClickEvent folderClickEvent) {

    }

    @Override
    public void showLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void showEmpty() {
        mEmptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideEmpty() {
        if (mEmptyView.getVisibility() == View.VISIBLE) {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void showAllFiles(final List<Folder> folders, boolean isRefresh) {

        mRlBottomContainer.setVisibility(View.VISIBLE);
        mMenuDone.setVisible(true);
        List<BaseFile> files = new ArrayList<>();
        files.addAll(folders.get(0).getFiles());
        if (isRefresh) {
            for (BaseFile file : files) {
                for (BaseFile file1 : mAdapter.getSelectedList()) {
                    if (file.getId() == file1.getId()) {
                        file.setSelected(true);
                    }
                }
            }
        }
        mAdapter.refresh(folders.get(0).getFiles());
        mFolderDialog.setData(folders);
        mTvFolder.setText(folders.get(0).getName() + "(" + folders.get(0).getFiles().size() + ")");
        mIsLoadDataComplete = true;
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsLoadDataComplete) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(mStartY - ev.getY()) > 15) {
                        mTvTime.removeCallbacks(mHideAnimTask);
                        if (mTvTime.getVisibility() == View.GONE || mHideTimeAnim.isRunning()) {
                            mHideTimeAnim.cancel();
                            mShowTimeAnim.start();
                        }
                        setTvTimeText();
                    }
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        mMenuRecord = menu.findItem(R.id.action_record);
        mMenuDone = menu.findItem(R.id.action_done);
        mMenuDone.setVisible(false);
        mMenuDone.setTitle(getString(R.string.confirm) + "(" + mAdapter.getSelectedList().size() + "/" + mMaxNumber + ")");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_done) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(FilePicker.RESULT_PICK, mAdapter.getSelectedList());
            setResult(RESULT_OK, intent);
            finish();
            return true;
        } else if (id == R.id.action_record) {
            Util.launchRecorder(new Util.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                    startActivityForResult(intent, REQUEST_CODE_TAKE_AUDIO);
                }
            }, this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.zoom_out);
    }

    @Override
    protected void onDestroy() {
        if (mFileClickDisposable != null) {
            mFileClickDisposable.dispose();
        }
        if (mFolderClickDisposable != null) {
            mFolderClickDisposable.dispose();
        }
        getContentResolver().unregisterContentObserver(mFileObserver);
        super.onDestroy();
    }

    private class FileObserver extends ContentObserver {

        FileObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            loadFiles(false);
        }
    }
}
