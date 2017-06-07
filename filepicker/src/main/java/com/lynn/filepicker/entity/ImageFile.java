package com.lynn.filepicker.entity;

import android.os.Parcel;
import android.os.Parcelable;



public class ImageFile extends BaseFile implements Parcelable {
    private int orientation;   //0, 90, 180, 270
    private int editCount;
    private String editedPath;
    public int getOrientation() {
        return orientation;
    }

    public int getEditCount() {
        return editCount;
    }

    public void setEditCount(int editCount) {
        this.editCount = editCount;
    }

    public String getEditedPath() {
        return editedPath;
    }

    public void setEditedPath(String editedPath) {
        this.editedPath = editedPath;
    }

    public void setOrientation(int orientation) {

        this.orientation = orientation;
    }

    @Override
    public String getPath() {
        if(editedPath!=null){
            return editedPath;
        }
        return path;
    }

    public String getSoucePath(){
        return path;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        dest.writeString(getName());
        dest.writeString(getPath());
        dest.writeLong(getSize());
        dest.writeString(getBucketId());
        dest.writeString(getBucketName());
        dest.writeLong(getDate());
        dest.writeByte((byte) (isSelected() ? 1 : 0));
        dest.writeInt(orientation);
        dest.writeInt(editCount);
        dest.writeString(editedPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageFile> CREATOR = new Creator<ImageFile>() {
        @Override
        public ImageFile[] newArray(int size) {
            return new ImageFile[size];
        }

        @Override
        public ImageFile createFromParcel(Parcel in) {
            ImageFile file = new ImageFile();
            file.setId(in.readLong());
            file.setName(in.readString());
            file.setPath(in.readString());
            file.setSize(in.readLong());
            file.setBucketId(in.readString());
            file.setBucketName(in.readString());
            file.setDate(in.readLong());
            file.setSelected(in.readByte() != 0);
            file.setOrientation(in.readInt());
            file.setEditCount(in.readInt());
            file.setEditedPath(in.readString());
            return file;
        }
    };
}
