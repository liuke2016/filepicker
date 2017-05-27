package com.lynn.filepicker.mvp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.lynn.filepicker.R;
import com.lynn.filepicker.Util;
import com.lynn.filepicker.entity.BaseFile;
import com.lynn.filepicker.entity.Folder;
import com.lynn.filepicker.entity.VideoFile;
import com.lynn.filepicker.mvp.PickerContract.IPickerModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
import static android.provider.MediaStore.Images.ImageColumns.BUCKET_ID;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;
import static android.provider.MediaStore.Video.VideoColumns.DURATION;
/**
 * Created by liuke on 2017/5/23.
 */


class VideoPickerModel implements IPickerModel {
    private static final String[] VIDEO_PROJECTION = {
            //Base File
            _ID,
            TITLE,
            DATA,
            SIZE,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            DATE_ADDED,
            //Video File
            DURATION
    };
    private final Context mContext;

    VideoPickerModel() {
        mContext = Util.getContext();
    }


    @Override
    public Observable<List<Folder>> getAllFiles() {
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION,
                null, null, DATE_ADDED + " DESC");
        List<Folder> folders = onLoadVideo(cursor);
        return Observable.just(folders);
    }

    private List<Folder> onLoadVideo(Cursor cursor) {
        if (cursor.getPosition() != -1) {
            cursor.moveToPosition(-1);
        }
        List<Folder> folders = new ArrayList<>();
        List<BaseFile> videoFiles = new ArrayList<>();
        while (cursor.moveToNext()) {
            //Create a File instance
            VideoFile video = new VideoFile();
            video.setId(cursor.getLong(cursor.getColumnIndexOrThrow(_ID)));
            video.setName(cursor.getString(cursor.getColumnIndexOrThrow(TITLE)));
            video.setPath(cursor.getString(cursor.getColumnIndexOrThrow(DATA)));
            video.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(SIZE)));
            video.setBucketId(cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_ID)));
            video.setBucketName(cursor.getString(cursor.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME)));
            video.setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DATE_ADDED)));

            video.setDuration(cursor.getLong(cursor.getColumnIndexOrThrow(DURATION)));
            //Query Thumbnail
            String filePath = mContext.getExternalCacheDir().getAbsolutePath() + "/"
                    + video.getId() + ".png";
            File file = new File(filePath);
            if (file.exists()) {
                video.setThumbnail(filePath);
            } else {
                String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
                String[] selectionArgs = new String[]{video.getId() + ""};
                final Cursor thumbCursor = mContext.getContentResolver().query(
                        MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Video.Thumbnails.DATA,
                                MediaStore.Video.Thumbnails.VIDEO_ID},
                        selection,
                        selectionArgs,
                        null);
                if (thumbCursor != null && thumbCursor.moveToFirst()) {
                    String thumbnail = thumbCursor.getString(
                            thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                    video.setThumbnail(thumbnail);
                }
                if (thumbCursor != null) {
                    thumbCursor.close();
                }

                //If there is no thumbnail in DB, create one on external disk
                if (TextUtils.isEmpty(video.getThumbnail())) {
                    String path = saveBitmap(getVideoThumbnail(video.getPath(), 180, 180,
                            MediaStore.Images.Thumbnails.MINI_KIND), filePath);
                    video.setThumbnail(path);
                }
            }
            videoFiles.add(video);
            //Create a Folder
            Folder folder = new Folder();
            folder.setId(video.getBucketId());
            folder.setName(video.getBucketName());
            folder.setPath(Util.extractDirectory(video.getPath()));

            if (!folders.contains(folder)) {
                folder.addFile(video);
                VideoFile videoFile = (VideoFile) folder.getFiles().get(0);
                folder.setCoverPath(videoFile.getThumbnail());
                folders.add(folder);
            } else {
                folders.get(folders.indexOf(folder)).addFile(video);
            }
        }
        Folder all = new Folder();
        all.setName(mContext.getString(R.string.all));
        all.setFiles(videoFiles);
        all.setSelected(true);
        folders.add(0, all);
        return folders;
    }


    private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    private String saveBitmap(Bitmap bitmap, String pathName) {
        if (bitmap == null) {
            return "";
        }

        String path = "";
//        String pathName = context.get().getExternalCacheDir().getAbsolutePath() + "/" + String.valueOf(System.currentTimeMillis()) + ".png";
        File f = new File(pathName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            path = pathName;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return path;
    }
}
