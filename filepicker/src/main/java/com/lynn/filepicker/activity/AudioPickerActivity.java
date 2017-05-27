package com.lynn.filepicker.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.adapter.AudioPickerAdapter;
import com.lynn.filepicker.config.AudioPickerConfig;
import com.lynn.filepicker.entity.AudioFile;
import com.lynn.filepicker.entity.Folder;
import com.lynn.filepicker.entity.event.FolderClickEvent;
import com.lynn.filepicker.mvp.AudioPickerPresenter;
import com.lynn.filepicker.widget.DividerListItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioPickerActivity extends BasePickerActivity<AudioFile> {
    protected boolean isNeedRecorder;
    public static final int REQUEST_CODE_TAKE_AUDIO = 0x101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AudioPickerConfig config = (AudioPickerConfig) FilePicker.getPickerConfig();
        isNeedRecorder = config.isNeedRecord();
        super.onCreate(savedInstanceState);
        mContentView.setBackgroundColor(Color.WHITE);
        mAdapter = new AudioPickerAdapter(this,new ArrayList<AudioFile>());
        mRecyclerView.setAdapter(mAdapter);
        Drawable drawable = getResources().getDrawable(R.mipmap.ic_record_audio);
        Drawable wrap = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrap,Color.GRAY);
        setEmptyImageView(wrap);
        setEmptyTvTip(getString(R.string.click_to_record_audio));
        setEmptyTextViewTextColor(Color.BLACK);
        setEmptyImageViewAction(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.launchRecorder(new Util.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                        startActivityForResult(intent, REQUEST_CODE_TAKE_AUDIO);
                    }
                }, AudioPickerActivity.this);
            }
        });
    }

    @Override
    protected Uri getExternalContentUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new DividerListItemDecoration(this,
                LinearLayoutManager.VERTICAL, R.drawable.divider_rv_file));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.pick_audio);
    }

    @Override
    protected void onChangeFolder(FolderClickEvent folderClickEvent) {
        if (isNeedRecorder) {
            if (folderClickEvent.getPosition() > 0) {
                mMenuRecord.setVisible(false);
            } else {
                mMenuRecord.setVisible(true);
            }
        }
    }

    @Override
    protected void loadFiles(boolean isRefresh) {
        mPresenter = new AudioPickerPresenter(this);
        mPresenter.loadAllFiles(isRefresh);
    }

    @Override
    public void showAllFiles(List<Folder> folders,boolean isRefresh) {
        super.showAllFiles(folders,isRefresh);
        mMenuRecord.setVisible(isNeedRecorder);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TAKE_AUDIO && resultCode == RESULT_OK) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File file = new File(getAudioFilePathFromUri(data.getData()));
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            showLoading();
        }
    }
    private String getAudioFilePathFromUri(Uri uri) {
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
        String s = cursor.getString(index);
        cursor.close();
        return s;
    }
}
