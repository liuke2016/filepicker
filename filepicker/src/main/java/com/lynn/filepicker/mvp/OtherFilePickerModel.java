package com.lynn.filepicker.mvp;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lynn.filepicker.R;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.entity.BaseFile;
import com.lynn.filepicker.entity.Folder;
import com.lynn.filepicker.entity.OtherFile;
import com.lynn.filepicker.mvp.PickerContract.IPickerModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Files.FileColumns.DATA;
import static android.provider.MediaStore.Files.FileColumns.DATE_ADDED;
import static android.provider.MediaStore.Files.FileColumns.MIME_TYPE;
import static android.provider.MediaStore.Files.FileColumns.SIZE;
import static android.provider.MediaStore.Files.FileColumns.TITLE;
/**
 * Created by liuke on 2017/5/23.
 */


class OtherFilePickerModel implements IPickerModel {
    private static final String[] FILE_PROJECTION = {
            //Base File
            _ID,
            TITLE,
            DATA,
            SIZE,
            DATE_ADDED,

            //Other File
            MIME_TYPE
    };
    private String[] mSuffixArgs;
    private final Context mContext;

    OtherFilePickerModel(String[] suffixArgs) {
        mContext = Util.getContext();
        mSuffixArgs = suffixArgs;
    }


    @Override
    public Observable<List<Folder>> getAllFiles() {
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Files.getContentUri("external"), FILE_PROJECTION,
                null, null, DATE_ADDED + " DESC");
        List<Folder> folders = onLoadOtherFile(cursor);
        cursor.close();
        return Observable.just(folders);
    }

    private List<Folder> onLoadOtherFile(Cursor cursor) {
        if (cursor.getPosition() != -1) {
            cursor.moveToPosition(-1);
        }
        List<Folder> folders = new ArrayList<>();
        List<BaseFile> files = new ArrayList<>();
        while (!cursor.isClosed() && cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow(DATA));
            if (path != null && contains(mSuffixArgs, path)) {
                //Create a File instance
                OtherFile file = new OtherFile();
                file.setId(cursor.getLong(cursor.getColumnIndexOrThrow(_ID)));
                file.setName(cursor.getString(cursor.getColumnIndexOrThrow(TITLE)));
                file.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DATA)));
                file.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(SIZE)));
                file.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DATE_ADDED)));

                file.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MIME_TYPE)));
                files.add(file);
                //Create a Folder
                Folder folder = new Folder();
                folder.setName(Util.extractName(Util.extractDirectory(file.getPath())));
                folder.setPath(Util.extractDirectory(file.getPath()));

                if (!folders.contains(folder)) {
                    folder.addFile(file);
                    folders.add(folder);
                } else {
                    folders.get(folders.indexOf(folder)).addFile(file);
                }
            }
        }
        Folder all = new Folder();
        all.setName(mContext.getString(R.string.all));
        all.setFiles(files);
        all.setSelected(true);
        folders.add(0, all);
        return folders;
    }

    private boolean contains(String[] types, String path) {
        for (String string : types) {
            if (path.endsWith(string)) return true;
        }
        return false;
    }
}
