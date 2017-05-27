package com.lynn.filepicker.config;

import android.support.annotation.ColorInt;

/**
 * Created by liuke on 2017/4/7.
 */

public class BasePickerConfig {

    @ColorInt
    private int steepToolBarColor;
    @ColorInt
    private int toolBarTextColor;
    private int maxNumber;

    BasePickerConfig(Builder builder) {
        this.steepToolBarColor = builder.steepToolBarColor;
        this.toolBarTextColor = builder.toolBarTextColor;
        this.maxNumber = builder.maxNumber;
    }


    public int getSteepToolBarColor() {
        return steepToolBarColor;
    }

    public int getToolBarTextColor() {
        return toolBarTextColor;
    }

    public int getMaxNumber() {
        return maxNumber;
    }


    public static class Builder {

        @ColorInt
        int steepToolBarColor;
        @ColorInt
        int toolBarTextColor;
        int maxNumber = 9;

        Builder() {

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



    }


}
