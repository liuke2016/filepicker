package com.lynn.filepicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.lynn.filepicker.entity.BaseFile;

import java.util.ArrayList;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

import static com.lynn.filepicker.FilePicker.RESULT_PICK;


public class ResultHandlerFragment<T extends BaseFile> extends Fragment {

  public static final int REQUEST_CODE = 0x00100;

  PublishSubject<ArrayList<T>> resultSubject = PublishSubject.create();
  BehaviorSubject<Boolean> attachSubject = BehaviorSubject.create();



  public PublishSubject<ArrayList<T>> getResultSubject() {
    return resultSubject;
  }

  public BehaviorSubject<Boolean> getAttachSubject() {
    return attachSubject;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE && data != null) {
      resultSubject.onNext(data.<T>getParcelableArrayListExtra(RESULT_PICK));
    }
  }

  @TargetApi(23) @Override public void onAttach(Context context) {
    super.onAttach(context);
    attachSubject.onNext(true);
  }

  @SuppressWarnings("deprecation") @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (Build.VERSION.SDK_INT < 23) {
      attachSubject.onNext(true);
    }
  }
}
