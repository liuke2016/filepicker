package com.lynn.filepicker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lynn.filepicker.entity.ImageFile;

/**
 * Created by liuke on 2017/6/7.
 */

public class ImageDao {
    private ImageSQLiteHelper mSqLiteHelper;

    public ImageDao(Context context) {
        mSqLiteHelper = new ImageSQLiteHelper(context, DB.DB_NAME, null, 1);
    }

    public void insert(ImageFile imageFile) {
        SQLiteDatabase sqLiteDatabase = mSqLiteHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB.COLUMNS_IMAGE_ID, imageFile.getId());
        contentValues.put(DB.COLUMNS_SOURCE_PATH, imageFile.getPath());
        contentValues.put(DB.COLUMNS_EDITED_PATH, imageFile.getEditedPath());
        contentValues.put(DB.COLUMNS_EDITED_COUNT, imageFile.getEditCount());
        sqLiteDatabase.insert(DB.TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
    }
    public void update(ImageFile imageFile){
        SQLiteDatabase sqLiteDatabase = mSqLiteHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DB.COLUMNS_EDITED_COUNT,imageFile.getEditCount());
        sqLiteDatabase.update(DB.TABLE_NAME,contentValues,DB.COLUMNS_IMAGE_ID+"=?",new String[]{imageFile.getId()+""});
        sqLiteDatabase.close();
    }
    public void find(ImageFile imageFile) {
        SQLiteDatabase sqLiteDatabase = mSqLiteHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(DB.TABLE_NAME, new String[]{DB.COLUMNS_EDITED_PATH, DB.COLUMNS_EDITED_COUNT}, DB.COLUMNS_IMAGE_ID + "=?", new String[]{imageFile.getId() + ""}, null, null, null);
        if(cursor.moveToFirst()){
            imageFile.setEditedPath(cursor.getString(0));
            imageFile.setEditCount(cursor.getInt(1));
        }
        cursor.close();
        sqLiteDatabase.close();
    }
}
