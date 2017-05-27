package com.lynn.filepicker.config;

/**
 * Created by liuke on 2017/5/12.
 */

public class VideoPickerConfig extends BasePickerConfig {

    private boolean isNeedCamera;

    private VideoPickerConfig(Builder builder) {
        super(builder);
        isNeedCamera = builder.isNeedCamera;
    }
    public boolean isNeedCamera() {
        return isNeedCamera;
    }

    public static class Builder extends BasePickerConfig.Builder {
        private boolean isNeedCamera;
        public Builder() {
            super();
        }

        public Builder isNeedCamera(boolean isNeedCamera) {
            this.isNeedCamera = isNeedCamera;
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

        public VideoPickerConfig build() {
            return new VideoPickerConfig(this);
        }
    }
}
