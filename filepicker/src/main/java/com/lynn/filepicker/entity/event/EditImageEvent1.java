package com.lynn.filepicker.entity.event;

/**
 * Created by liuke on 2017/5/24.
 */

public class EditImageEvent1 {

    private int index;

    public EditImageEvent1(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
