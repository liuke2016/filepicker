package com.lynn.filepicker.config;

/**
 * Created by liuke on 2017/5/12.
 */

public class AudioPickerConfig extends BasePickerConfig {


    private boolean isNeedRecord;

    private AudioPickerConfig(Builder builder) {
        super(builder);
        isNeedRecord = builder.isNeedRecord;
    }
    public boolean isNeedRecord() {
        return isNeedRecord;
    }

    public static class Builder extends BasePickerConfig.Builder {
        private boolean isNeedRecord;


        public Builder() {

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

        public Builder isNeedRecord(boolean isNeedRecord) {
            this.isNeedRecord = isNeedRecord;
            return this;
        }
        public AudioPickerConfig build() {
            return new AudioPickerConfig(this);
        }
    }
}
