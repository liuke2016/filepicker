package com.lynn.filepicker.mvp;

import com.lynn.filepicker.entity.Folder;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liuke on 2017/5/11.
 */

public class BasePickerPresenter<T extends PickerContract.IPickerModel> implements PickerContract.IPickerPresenter {
    private PickerContract.IPickerView mIPickerView;
    T mModel;

    BasePickerPresenter(PickerContract.IPickerView iPickerView) {
        mIPickerView = iPickerView;
    }
    @Override
    public void loadAllFiles(final boolean isRefresh) {
        Observable.just(isRefresh)
                .flatMap(new Function<Boolean, Observable<List<Folder>>>() {
                    @Override
                    public Observable<List<Folder>> apply(@NonNull Boolean isRefresh) throws Exception {
                        if (isRefresh) {
                            Thread.sleep(300);
                            return mModel.getAllFiles();
                        } else {
                            return mModel.getAllFiles();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        mIPickerView.hideEmpty();
                        mIPickerView.showLoading();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Folder>>() {
                    @Override
                    public void accept(@NonNull List<Folder> folders) throws Exception {
                        mIPickerView.hideLoading();
                        if(folders.size()==1){
                            mIPickerView.showAllFiles(folders, isRefresh);
                            mIPickerView.showEmpty();
                        }else{
                            mIPickerView.showAllFiles(folders, isRefresh);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        mIPickerView.showMessage(throwable.getMessage());
                    }
                });
    }

}
