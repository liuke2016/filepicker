package com.lynn.filepicker.config;

import android.os.Environment;
import android.text.TextUtils;

import com.lynn.filepicker.Util;
import com.lynn.filepicker.db.IDao;

/**
 * Created by liuke on 2017/5/12.
 */

public class ImagePickerConfig extends BasePickerConfig {
    public static final String DEFAULT_EDIT_SAVE_PATH = Util.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    private boolean isNeedCamera;
    private boolean isNeedEdit;
    private String editSavePath;
    private IDao imageDao;

    private ImagePickerConfig(Builder builder) {
        super(builder);
        isNeedCamera = builder.isNeedCamera;
        editSavePath = builder.editSavePath;
        isNeedEdit = builder.isNeedEdit;
        imageDao = builder.imageDao;
    }

    public boolean isNeedCamera() {
        return isNeedCamera;
    }

    public String getEditSavePath() {
        return editSavePath;
    }

    public boolean isNeedEdit() {
        return isNeedEdit;
    }

    public IDao getImageDao() {
        return imageDao;
    }

    public static class Builder extends BasePickerConfig.Builder {
        private boolean isNeedCamera;
        private String editSavePath;
        private boolean isNeedEdit;
        private IDao imageDao;
        public Builder() {
            super();
        }

        public Builder isNeedCamera(boolean isNeedCamera) {
            this.isNeedCamera = isNeedCamera;
            return this;
        }

        public Builder editSavePath(String editSavePath) {
            this.editSavePath = editSavePath;
            return this;
        }
        public Builder isNeedEdit(boolean isNeedEdit){
            this.isNeedEdit = isNeedEdit;
            return this;
        }
        public Builder steepToolBarColor(int steepToolBarColor) {
            this.steepToolBarColor = steepToolBarColor;
            return this;
        }

        public Builder toolBarTextColor(int toolBarTextColor) {
            this.toolBarTextColor = toolBarTextColor;
            return this;
        }

        public Builder maxNumber(int maxNumber) {
            this.maxNumber = maxNumber;
            return this;
        }
        public Builder imageDao(IDao imageDao){
            this.imageDao = imageDao;
            return this;
        }
        public ImagePickerConfig build() {
            if (TextUtils.isEmpty(editSavePath)) editSavePath = DEFAULT_EDIT_SAVE_PATH;
            return new ImagePickerConfig(this);
        }
    }
}
