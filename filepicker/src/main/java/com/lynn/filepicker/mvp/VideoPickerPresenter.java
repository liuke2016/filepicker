package com.lynn.filepicker.mvp;

/**
 * Created by liuke on 2017/5/11.
 */

public class VideoPickerPresenter extends BasePickerPresenter<VideoPickerModel> {

    public VideoPickerPresenter(PickerContract.IPickerView iPickerView) {
        super(iPickerView);
        mModel = new VideoPickerModel();
    }

}
