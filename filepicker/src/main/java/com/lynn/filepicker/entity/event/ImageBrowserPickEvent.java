package com.lynn.filepicker.entity.event;

import com.lynn.filepicker.entity.ImageFile;

/**
 * Created by liuke on 2017/5/15.
 */

public class ImageBrowserPickEvent {
    public ImageBrowserPickEvent(boolean isSelected, ImageFile file) {
        mIsSelected = isSelected;
        mFile = file;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    public ImageFile getFile() {
        return mFile;
    }

    public void setFile(ImageFile file) {
        mFile = file;
    }

    private boolean mIsSelected;
    private ImageFile mFile;
}
