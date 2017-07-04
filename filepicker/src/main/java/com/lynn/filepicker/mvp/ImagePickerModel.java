package com.lynn.filepicker.mvp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.R;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.config.ImagePickerConfig;
import com.lynn.filepicker.db.IDao;
import com.lynn.filepicker.db.ImageDao;
import com.lynn.filepicker.entity.Folder;
import com.lynn.filepicker.entity.ImageFile;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.Images.ImageColumns.ORIENTATION;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * Created by liuke on 2017/5/23.
 */


public class ImagePickerModel implements PickerContract.IPickerModel {
    private static final String[] IMAGE_PROJECTION = {
            //Base File
            _ID,
            TITLE,
            DATA,
            SIZE,
            BUCKET_ID,
            BUCKET_DISPLAY_NAME,
            DATE_ADDED,
            //Image File
            ORIENTATION
    };
    private final Context mContext;
    private final IDao mImageDao;

    public ImagePickerModel() {
        mContext = Util.getContext();
        IDao imageDao = ((ImagePickerConfig) FilePicker.getPickerConfig()).getImageDao();
        if(imageDao!=null){
            mImageDao = imageDao;
        }else{
            mImageDao = new ImageDao(mContext);
        }
    }


    @Override
    public Observable<List<Folder>> getAllFiles() {
        Cursor cursor = MediaStore.Images.Media.query(mContext.getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, DATE_ADDED + " DESC");
        List<Folder> folders = onLoadImages(cursor);
        cursor.close();
        return Observable.just(folders);
    }

    private List<Folder> onLoadImages(Cursor cursor) {
        if (cursor.getPosition() != -1) {
            cursor.moveToPosition(-1);
        }
        List<Folder> folders = new ArrayList<>();
        List<ImageFile> imageFiles = new ArrayList<>();
        while (cursor.moveToNext()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndexOrThrow(DATA)), options);
            if (options.outMimeType == null) {
                continue;
            }
            ImageFile img = new ImageFile();
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(_ID));
            img.setId(id);
            img.setName(cursor.getString(cursor.getColumnIndexOrThrow(TITLE)));
            img.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DATA)));
            img.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(SIZE)));
            img.setBucketId(cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_ID)));
            img.setBucketName(cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)));
            img.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DATE_ADDED)));
            img.setOrientation(cursor.getInt(cursor.getColumnIndexOrThrow(ORIENTATION)));
            imageFiles.add(img);
            //Create a Folder
            Folder folder = new Folder();
            folder.setId(img.getBucketId());
            folder.setName(img.getBucketName());
            folder.setPath(Util.extractDirectory(img.getPath()));

            if (!folders.contains(folder)) {
                folder.setCoverPath(img.getPath());
                folder.addFile(img);
                folders.add(folder);
            } else {
                folders.get(folders.indexOf(folder)).addFile(img);
            }
        }
        ImagePickerConfig config = (ImagePickerConfig) FilePicker.getPickerConfig();
        if(config.isNeedEdit()){
            for(ImageFile imageFile:imageFiles){
                mImageDao.find(imageFile);
            }
        }
        Folder all = new Folder();
        all.setName(mContext.getString(R.string.all));
        all.setFiles(imageFiles);
        all.setSelected(true);
        folders.add(0, all);
        return folders;
    }
    public void syncImageFile(ImageFile file){
        mImageDao.find(file);
    }
}
