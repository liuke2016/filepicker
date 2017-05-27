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
import com.lynn.filepicker.Util;
import com.lynn.filepicker.adapter.VideoPickerAdapter;
import com.lynn.filepicker.config.VideoPickerConfig;
import com.lynn.filepicker.entity.VideoFile;
import com.lynn.filepicker.entity.event.FolderClickEvent;
import com.lynn.filepicker.mvp.VideoPickerPresenter;
import com.lynn.filepicker.widget.DividerGridItemDecoration;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DCIM;

public class VideoPickerActivity extends BasePickerActivity<VideoFile> {
    protected boolean isNeedCamera;
    public static final int REQUEST_CODE_TAKE_VIDEO = 0x101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VideoPickerConfig config = (VideoPickerConfig) FilePicker.getPickerConfig();
        isNeedCamera = config.isNeedCamera();
        super.onCreate(savedInstanceState);
        final VideoPickerAdapter videoPickerAdapter = new VideoPickerAdapter(this, new ArrayList<VideoFile>(), isNeedCamera);
        videoPickerAdapter.setNeedCamera(isNeedCamera);
        mAdapter = videoPickerAdapter;
        mRecyclerView.setAdapter(mAdapter);
        setEmptyImageView(R.mipmap.ic_camera);
        setEmptyTvTip(getString(R.string.click_to_record_video));
        setEmptyTextViewTextColor(Color.WHITE);
        setEmptyImageViewAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.launchCamera(new Util.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
                        File file = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath()
                                + "/VID_" + timeStamp + ".mp4");
                        videoPickerAdapter.mVideoPath = file.getAbsolutePath();
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
                    }
                }, VideoPickerActivity.this);
            }
        });
    }

    @Override
    protected Uri getExternalContentUri() {
        return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, COLUMN_NUMBER));
        recyclerView.setBackgroundColor(getResources().getColor(R.color.BgRv));
        recyclerView.addItemDecoration(new DividerGridItemDecoration(this));
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.pick_video);
    }


    @Override
    protected void onChangeFolder(FolderClickEvent folderClickEvent) {
        VideoPickerAdapter videoPickerAdapter = (VideoPickerAdapter) mAdapter;
        if (isNeedCamera) {
            if (folderClickEvent.getPosition() > 0) {
                videoPickerAdapter.setNeedCamera(false);
            } else {
                videoPickerAdapter.setNeedCamera(true);
            }
        }
    }

    @Override
    protected void loadFiles(boolean isRefresh) {
        mPresenter = new VideoPickerPresenter(this);
        mPresenter.loadAllFiles(isRefresh);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VideoPickerAdapter videoPickerAdapter = (VideoPickerAdapter) mAdapter;
        if (requestCode == REQUEST_CODE_TAKE_VIDEO && resultCode == RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(videoPickerAdapter.mVideoPath);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            showLoading();
//            loadFiles(true);
        }
    }
}
