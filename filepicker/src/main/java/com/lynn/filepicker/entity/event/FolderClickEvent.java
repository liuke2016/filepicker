package com.lynn.filepicker.entity.event;


import com.lynn.filepicker.entity.Folder;

import java.util.List;


public class FolderClickEvent {
  private int mPosition;

  private List<Folder> mFolders;
  private Folder mFolder;

  public FolderClickEvent(List<Folder> folders, Folder folder, int position) {
    mFolders = folders;
    mFolder = folder;
    mPosition = position;
  }
  public int getPosition() {
    return mPosition;
  }


  public Folder getFolder() {
    return mFolder;
  }

  public List<Folder> getFolders() {
    return mFolders;
  }
}
