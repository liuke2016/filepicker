package com.lynn.filepicker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.lynn.filepicker.activity.AudioPickerActivity;
import com.lynn.filepicker.activity.ImagePickerActivity;
import com.lynn.filepicker.activity.OtherFilesPickerActivity;
import com.lynn.filepicker.activity.VideoPickerActivity;
import com.lynn.filepicker.config.AudioPickerConfig;
import com.lynn.filepicker.config.BasePickerConfig;
import com.lynn.filepicker.config.ImagePickerConfig;
import com.lynn.filepicker.config.OtherFilePickerConfig;
import com.lynn.filepicker.config.VideoPickerConfig;
import com.lynn.filepicker.entity.AudioFile;
import com.lynn.filepicker.entity.BaseFile;
import com.lynn.filepicker.entity.ImageFile;
import com.lynn.filepicker.entity.OtherFile;
import com.lynn.filepicker.entity.VideoFile;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Created by liuke on 2017/4/7.
 */

public class FilePicker {
    public static final String RESULT_PICK = "ResultPick";

    private static BasePickerConfig mPickerConfig;

    public static BasePickerConfig getPickerConfig() {
        return mPickerConfig;
    }


    public static Observable<ArrayList<ImageFile>> pickImage(@NonNull Activity activity) {
        return pickImage(activity.getFragmentManager(), new ImagePickerConfig.Builder()
                .isNeedCamera(true)
                .maxNumber(9)
                .build());
    }

    public static Observable<ArrayList<ImageFile>> pickImage(@NonNull Fragment fragment) {
        return pickImage(fragment.getFragmentManager(), new ImagePickerConfig.Builder()
                .isNeedCamera(false)
                .maxNumber(9)
                .build());
    }

    public static Observable<ArrayList<ImageFile>> pickImage(@NonNull Activity activity, @NonNull ImagePickerConfig imagePickerConfig){
        return pickImage(activity.getFragmentManager(), imagePickerConfig);
    }

    public static Observable<ArrayList<ImageFile>> pickImage(@NonNull Fragment fragment, @NonNull ImagePickerConfig imagePickerConfig){
        return pickImage(fragment.getFragmentManager(), imagePickerConfig);
    }

    public static Observable<ArrayList<ImageFile>> pickImage(@NonNull FragmentManager fragmentManager, @NonNull ImagePickerConfig imagePickerConfig) {
        return FilePicker.pick(fragmentManager, imagePickerConfig);
    }

    public static Observable<ArrayList<AudioFile>> pickAudio(@NonNull Activity activity) {
        return pickAudio(activity.getFragmentManager(), new AudioPickerConfig.Builder()
                .isNeedRecord(false)
                .maxNumber(9)
                .build());
    }

    public static Observable<ArrayList<AudioFile>> pickAudio(@NonNull Fragment fragment) {
        return pickAudio(fragment.getFragmentManager(), new AudioPickerConfig.Builder()
                .isNeedRecord(false)
                .maxNumber(9)
                .build());
    }

    public static Observable<ArrayList<AudioFile>> pickAudio(@NonNull Activity activity, @NonNull AudioPickerConfig audioPickerConfig){
        return pickAudio(activity.getFragmentManager(), audioPickerConfig);
    }

    public static Observable<ArrayList<AudioFile>> pickAudio(@NonNull Fragment fragment, @NonNull AudioPickerConfig audioPickerConfig){
        return pickAudio(fragment.getFragmentManager(), audioPickerConfig);
    }

    public static Observable<ArrayList<AudioFile>> pickAudio(@NonNull FragmentManager fragmentManager, @NonNull AudioPickerConfig audioPickerConfig) {
        return FilePicker.pick(fragmentManager, audioPickerConfig);
    }



    public static Observable<ArrayList<VideoFile>> pickVideo(@NonNull Activity activity) {
        return pickVideo(activity.getFragmentManager(), new VideoPickerConfig.Builder()
                .isNeedCamera(false)
                .maxNumber(9)
                .build());
    }

    public static Observable<ArrayList<VideoFile>> pickVideo(@NonNull Fragment fragment) {
        return pickVideo(fragment.getFragmentManager(), new VideoPickerConfig.Builder()
                .isNeedCamera(false)
                .maxNumber(9)
                .build());
    }

    public static Observable<ArrayList<VideoFile>> pickVideo(@NonNull Activity activity, @NonNull VideoPickerConfig videoPickerConfig){
        return pickVideo(activity.getFragmentManager(), videoPickerConfig);
    }

    public static Observable<ArrayList<VideoFile>> pickVideo(@NonNull Fragment fragment, @NonNull VideoPickerConfig videoPickerConfig){
        return pickVideo(fragment.getFragmentManager(), videoPickerConfig);
    }

    public static Observable<ArrayList<VideoFile>> pickVideo(@NonNull FragmentManager fragmentManager, @NonNull VideoPickerConfig videoPickerConfig) {
        return FilePicker.pick(fragmentManager, videoPickerConfig);
    }


    public static Observable<ArrayList<OtherFile>> pickOtherFile(@NonNull Activity activity) {
        return pickOtherFile(activity.getFragmentManager(), new OtherFilePickerConfig.Builder()
                .maxNumber(9)
                .build());
    }

    public static Observable<ArrayList<OtherFile>> pickOtherFile(@NonNull Fragment fragment) {
        return pickOtherFile(fragment.getFragmentManager(), new OtherFilePickerConfig.Builder()
                .maxNumber(9)
                .build());
    }

    public static Observable<ArrayList<OtherFile>> pickOtherFile(@NonNull Activity activity, @NonNull OtherFilePickerConfig otherFilePickerConfig){
        return pickOtherFile(activity.getFragmentManager(), otherFilePickerConfig);
    }

    public static Observable<ArrayList<OtherFile>> pickOtherFile(@NonNull Fragment fragment, @NonNull OtherFilePickerConfig otherFilePickerConfig){
        return pickOtherFile(fragment.getFragmentManager(), otherFilePickerConfig);
    }

    public static Observable<ArrayList<OtherFile>> pickOtherFile(@NonNull FragmentManager fragmentManager, @NonNull OtherFilePickerConfig otherFilePickerConfig) {
        return FilePicker.pick(fragmentManager, otherFilePickerConfig);
    }
    private static boolean setPickerConfig(BasePickerConfig pickerConfig) {
        if (pickerConfig == null) {
            return false;
        }
        mPickerConfig = pickerConfig;
        return true;
    }

     static <T extends BaseFile> Observable<ArrayList<T>> pick(@NonNull final FragmentManager fragmentManager, @NonNull final BasePickerConfig pickerConfig) {
        if (setPickerConfig(pickerConfig)) {
            ResultHandlerFragment fragment = (ResultHandlerFragment) fragmentManager.findFragmentByTag(
                    pickerConfig.getClass().getName());
            if (fragment == null) {
                fragment = new ResultHandlerFragment<T>();
                fragmentManager.beginTransaction()
                        .add(fragment, pickerConfig.getClass().getName())
                        .commit();
            } else if (fragment.isDetached()) {
                final FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.attach(fragment);
                transaction.commit();
            }
            final ResultHandlerFragment finalFragment = fragment;
            return finalFragment.getAttachSubject().filter(new Predicate<Boolean>() {
                @Override public boolean test(@NonNull Boolean aBoolean) throws Exception {
                    return aBoolean;
                }
            }).flatMap(new Function<Boolean, ObservableSource<ArrayList<T>>>() {
                @Override public ObservableSource<ArrayList<T>> apply(@NonNull Boolean aBoolean)
                        throws Exception {
//                    Type[] types = ((ParameterizedType) FilePicker.class.getDeclaredMethod("pick", FragmentManager.class, BasePickerConfig.class).getGenericReturnType()).getActualTypeArguments();
//                    Type[] types1 = ((ParameterizedType) types[0]).getActualTypeArguments();

                    Intent intent = new Intent();
                    if(pickerConfig instanceof ImagePickerConfig){
                        intent.setClass(finalFragment.getActivity(), ImagePickerActivity.class);
                    }else if(pickerConfig instanceof AudioPickerConfig){
                        intent.setClass(finalFragment.getActivity(), AudioPickerActivity.class);
                    }else if(pickerConfig instanceof VideoPickerConfig){
                        intent.setClass(finalFragment.getActivity(), VideoPickerActivity.class);
                    }else if(pickerConfig instanceof OtherFilePickerConfig){
                        intent.setClass(finalFragment.getActivity(), OtherFilesPickerActivity.class);
                    }
                    finalFragment.startActivityForResult(intent, ResultHandlerFragment.REQUEST_CODE);
                    finalFragment.getActivity().overridePendingTransition(R.anim.zoom_in,0);
                    return finalFragment.getResultSubject();
                }
            }).take(1);
        }
        return null;
    }
}
