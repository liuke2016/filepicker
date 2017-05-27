package com.lynn.filepicker.mvp;

/**
 * Created by liuke on 2017/5/11.
 */

public class OtherFilePickerPresenter extends BasePickerPresenter<OtherFilePickerModel> {
    public OtherFilePickerPresenter(PickerContract.IPickerView iPickerView,String[] suffixArgs) {
        super(iPickerView);
        mModel = new OtherFilePickerModel(suffixArgs);
    }

}
