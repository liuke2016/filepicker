package com.lynn.filepicker.mvp;

import com.lynn.filepicker.entity.Folder;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by liuke on 2017/5/11.
 */

public interface PickerContract {
    interface IPickerView {
        void showLoading();
        void hideLoading();
        void showEmpty();
        void hideEmpty();
        void showAllFiles(List<Folder> folders,boolean isRefresh);
        void showMessage(String message);
    }
    interface IPickerPresenter{
        void loadAllFiles(boolean isRefresh);
    }

    interface IPickerModel{
        Observable<List<Folder>> getAllFiles();
    }
}
