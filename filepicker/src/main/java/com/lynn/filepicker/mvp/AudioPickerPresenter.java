package com.lynn.filepicker.mvp;

/**
 * Created by liuke on 2017/5/11.
 */

public class AudioPickerPresenter extends BasePickerPresenter<AudioPickerModel> {

    public AudioPickerPresenter(PickerContract.IPickerView iPickerView) {
        super(iPickerView);
        mModel = new AudioPickerModel();
    }

}
