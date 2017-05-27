package com.lynn.filepicker.config;

/**
 * Created by liuke on 2017/5/12.
 */

public class OtherFilePickerConfig extends BasePickerConfig {

    private String[] suffix;

    private OtherFilePickerConfig(Builder builder) {
        super(builder);
        suffix = builder.suffix;
    }

    public String[] getSuffix() {
        return suffix;
    }

    public static class Builder extends BasePickerConfig.Builder {
        private String[] suffix;


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
        public Builder suffix(String[] suffix) {
            this.suffix = suffix;
            return this;
        }
        public OtherFilePickerConfig build() {
            return new OtherFilePickerConfig(this);
        }
    }
}
