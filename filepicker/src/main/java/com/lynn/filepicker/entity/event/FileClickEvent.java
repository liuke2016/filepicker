package com.lynn.filepicker.entity.event;


public class FileClickEvent<T> {

  public boolean isSelected() {
    return mIsSelected;
  }

  public T getFile() {
    return mFile;
  }

  private boolean mIsSelected;
  private T mFile;

  public FileClickEvent(boolean isSelected, T file) {
    mIsSelected = isSelected;
    mFile = file;
  }


}
