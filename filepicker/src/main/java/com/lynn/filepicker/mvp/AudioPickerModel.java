package com.lynn.filepicker.mvp;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.lynn.filepicker.R;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.entity.AudioFile;
import com.lynn.filepicker.entity.BaseFile;
import com.lynn.filepicker.entity.Folder;
import com.lynn.filepicker.mvp.PickerContract.IPickerModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;
import static android.provider.MediaStore.Video.VideoColumns.DURATION;
/**
 * Created by liuke on 2017/5/23.
 */


class AudioPickerModel implements IPickerModel {
    private static final String[] AUDIO_PROJECTION = {
            //Base File
            _ID,
            TITLE,
            DATA,
            SIZE,
            DATE_ADDED,
            //Audio File
            MediaStore.Audio.Media.DURATION
    };
    private final Context mContext;

    AudioPickerModel() {
        mContext = Util.getContext();
    }


    @Override
    public Observable<List<Folder>> getAllFiles() {
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, AUDIO_PROJECTION,
                null, null, DATE_ADDED + " DESC");
        List<Folder> folders = onLoadAudio(cursor);
        cursor.close();
        return Observable.just(folders);
    }

    private List<Folder> onLoadAudio(Cursor cursor) {
        if (cursor.getPosition() != -1) {
            cursor.moveToPosition(-1);
        }
        List<Folder> folders = new ArrayList<>();
        List<BaseFile> audioFiles = new ArrayList<>();
        while (cursor.moveToNext()) {
            //Create a File instance
            AudioFile audio = new AudioFile();
            audio.setId(cursor.getLong(cursor.getColumnIndexOrThrow(_ID)));
            audio.setName(cursor.getString(cursor.getColumnIndexOrThrow(TITLE)));
            audio.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DATA)));
            audio.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(SIZE)));
            audio.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DATE_ADDED)));

            audio.setDuration(cursor.getLong(cursor.getColumnIndexOrThrow(DURATION)));
            audioFiles.add(audio);
            //Create a Folder
            Folder folder = new Folder();
            folder.setName(Util.extractName(Util.extractDirectory(audio.getPath())));
            folder.setPath(Util.extractDirectory(audio.getPath()));

            if (!folders.contains(folder)) {
                folder.addFile(audio);
                folders.add(folder);
            } else {
                folders.get(folders.indexOf(folder)).addFile(audio);
            }
        }
        Folder all = new Folder();
        all.setName(mContext.getString(R.string.all));
        all.setFiles(audioFiles);
        all.setSelected(true);
        folders.add(0, all);
        return folders;
    }
}
