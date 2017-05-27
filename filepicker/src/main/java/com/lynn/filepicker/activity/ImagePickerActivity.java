package com.lynn.filepicker.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.RxBus;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.adapter.ImagePickerAdapter;
import com.lynn.filepicker.config.ImagePickerConfig;
import com.lynn.filepicker.entity.BaseFile;
import com.lynn.filepicker.entity.ImageFile;
import com.lynn.filepicker.entity.event.EditImageEvent;
import com.lynn.filepicker.entity.event.FolderClickEvent;
import com.lynn.filepicker.entity.event.ImageBrowserPickEvent;
import com.lynn.filepicker.mvp.ImagePickerPresenter;
import com.lynn.filepicker.widget.DividerGridItemDecoration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

import static android.os.Environment.DIRECTORY_DCIM;

public class ImagePickerActivity extends BasePickerActivity<ImageFile> {
    protected boolean isNeedCamera;
    private Disposable mPickImageWhenPreview;
    public static final int REQUEST_CODE_TAKE_IMAGE = 0x101;
    public static final int REQUEST_CODE_BROWSER_IMAGE = 0x102;
    private Disposable mEditComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ImagePickerConfig config = (ImagePickerConfig) FilePicker.getPickerConfig();
        isNeedCamera = config.isNeedCamera();
        super.onCreate(savedInstanceState);
        mTvPreview.setVisibility(View.VISIBLE);
        final ImagePickerAdapter imagePickerAdapter = new ImagePickerAdapter(this, new ArrayList<ImageFile>(), isNeedCamera);
        imagePickerAdapter.setNeedCamera(isNeedCamera);
        mAdapter = imagePickerAdapter;
        mRecyclerView.setAdapter(mAdapter);
        setEmptyImageView(R.mipmap.ic_camera);
        setEmptyTvTip(getString(R.string.click_to_take_photo));
        setEmptyTextViewTextColor(Color.WHITE);
        setEmptyTvTipTextColor(Color.parseColor("#727272"));
        setEmptyImageViewAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.launchCamera(new Util.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
                        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath()
                                + "/IMG_" + timeStamp + ".jpg");
                        imagePickerAdapter.mImagePath = file.getAbsolutePath();
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        startActivityForResult(intent, REQUEST_CODE_TAKE_IMAGE);
                    }
                }, ImagePickerActivity.this);
            }
        });
    }

    @Override
    protected Uri getExternalContentUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        mPickImageWhenPreview = RxBus.getDefault().toObservable(ImageBrowserPickEvent.class)
                .subscribe(new Consumer<ImageBrowserPickEvent>() {
                    @Override
                    public void accept(@NonNull ImageBrowserPickEvent imageBrowserPickEvent) throws Exception {
                        ImageFile imageFile = imageBrowserPickEvent.getFile();
                        for (BaseFile baseFile : mAdapter.getDataSet()) {
                            if (baseFile.getId() == imageFile.getId()) {
                                baseFile.setSelected(imageFile.isSelected());
                            }
                        }
                        ArrayList<ImageFile> selectedList = mAdapter.getSelectedList();
                        for (int i = 0; i < selectedList.size(); i++) {
                            BaseFile baseFile = selectedList.get(i);
                            if (baseFile.getId() == imageFile.getId()) {
                                if (!imageFile.isSelected()) {
                                    selectedList.remove(i);
                                    break;
                                }
                            }
                            if (i == selectedList.size() - 1 && imageFile.isSelected()) {
                                selectedList.add(imageFile);
                                break;
                            }
                        }
                    }
                });

        mEditComplete = RxBus.getDefault().toObservable(EditImageEvent.class)
                .subscribe(new Consumer<EditImageEvent>() {
                    @Override
                    public void accept(EditImageEvent editImageEvent) throws Exception {
                        mAdapter.notifyItemChanged(editImageEvent.getIndex());
                    }
                });
    }

    @Override
    protected void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, COLUMN_NUMBER));
        recyclerView.setBackgroundColor(getResources().getColor(R.color.BgRv));
        recyclerView.addItemDecoration(new DividerGridItemDecoration(this));
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.pick_image);
    }

    @Override
    protected void loadFiles(boolean isRefresh) {
        mPresenter = new ImagePickerPresenter(this);
        mPresenter.loadAllFiles(isRefresh);
    }

    @Override
    protected void onSelectFile() {
        if (mAdapter.getSelectedList().size() > 0) {
            mTvPreview.setTextColor(Color.WHITE);
            mTvPreview.setText(getString(R.string.preview) + "(" + mAdapter.getSelectedList().size() + ")");
            mTvPreview.setEnabled(true);
        } else {
            mTvPreview.setTextColor(Color.GRAY);
            mTvPreview.setText(R.string.preview);
            mTvPreview.setEnabled(false);
        }
    }

    @Override
    protected void onChangeFolder(FolderClickEvent folderClickEvent) {
        ImagePickerAdapter imagePickerAdapter = (ImagePickerAdapter) mAdapter;
        if (isNeedCamera) {
            if (folderClickEvent.getPosition() > 0) {
                imagePickerAdapter.setNeedCamera(false);
            } else {
                imagePickerAdapter.setNeedCamera(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImagePickerAdapter imagePickerAdapter = (ImagePickerAdapter) mAdapter;
        switch (requestCode) {
            case REQUEST_CODE_TAKE_IMAGE:
                if (resultCode == RESULT_OK) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File file = new File(imagePickerAdapter.mImagePath);
                    Uri contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);
                    showLoading();
//                    loadFiles(true);
                    mMenuDone.setTitle(getString(R.string.confirm) + "(" + mAdapter.getSelectedList().size() + "/" + mMaxNumber + ")");
                }
                break;
            case REQUEST_CODE_BROWSER_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (mAdapter.getSelectedList().size() > 0) {
                        mTvPreview.setTextColor(Color.WHITE);
                        mTvPreview.setText(getString(R.string.preview) + "(" + mAdapter.getSelectedList().size() + ")");
                        mTvPreview.setEnabled(true);
                    } else {
                        mTvPreview.setTextColor(Color.GRAY);
                        mTvPreview.setText(R.string.preview);
                        mTvPreview.setEnabled(false);
                    }
                    imagePickerAdapter.notifyDataSetChanged();
                    mMenuDone.setTitle(getString(R.string.confirm) + "(" + imagePickerAdapter.getSelectedList().size() + "/" + mMaxNumber + ")");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mPickImageWhenPreview != null) {
            mPickImageWhenPreview.dispose();
            mPickImageWhenPreview = null;
        }
        if (mEditComplete != null) {
            mEditComplete.dispose();
            mEditComplete = null;
        }
        super.onDestroy();
    }
}
