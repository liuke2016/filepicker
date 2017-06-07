package com.lynn.filepicker.mvp;

import com.lynn.filepicker.entity.ImageFile;

/**
 * Created by liuke on 2017/5/11.
 */

public class ImagePickerPresenter extends BasePickerPresenter<ImagePickerModel> {

    public ImagePickerPresenter(PickerContract.IPickerView iPickerView) {
        super(iPickerView);
        mModel = new ImagePickerModel();
    }
    public void syncImageFile(ImageFile file){
        mModel.syncImageFile(file);
    }
}
