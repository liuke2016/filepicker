package com.lynn.filepicker.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.adapter.OtherFilesPickerAdapter;
import com.lynn.filepicker.config.OtherFilePickerConfig;
import com.lynn.filepicker.entity.OtherFile;
import com.lynn.filepicker.mvp.OtherFilePickerPresenter;
import com.lynn.filepicker.widget.DividerListItemDecoration;

import java.util.ArrayList;

public class OtherFilesPickerActivity extends BasePickerActivity<OtherFile> {
    private String[] mSuffix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OtherFilePickerConfig config = (OtherFilePickerConfig) FilePicker.getPickerConfig();
        mSuffix = config.getSuffix();
        super.onCreate(savedInstanceState);
        mContentView.setBackgroundColor(Color.WHITE);
        mAdapter = new OtherFilesPickerAdapter(this,new ArrayList<OtherFile>());
        mRecyclerView.setAdapter(mAdapter);
        setEmptyTextViewTextColor(Color.BLACK);
        setEmptyImageView(R.mipmap.ic_empty);
    }

    @Override
    protected Uri getExternalContentUri() {
        return MediaStore.Files.getContentUri("external");
    }

    @Override
    protected void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new DividerListItemDecoration(this,
                LinearLayoutManager.VERTICAL, R.drawable.divider_rv_file));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.pick_other_file);
    }

    @Override
    protected void loadFiles(boolean isRefresh) {
        mPresenter = new OtherFilePickerPresenter(this,mSuffix);
        mPresenter.loadAllFiles(isRefresh);
    }
}
