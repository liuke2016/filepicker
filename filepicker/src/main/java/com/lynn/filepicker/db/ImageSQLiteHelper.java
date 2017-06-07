package com.lynn.filepicker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by liuke on 2017/6/7.
 */

public class ImageSQLiteHelper extends SQLiteOpenHelper {


    public ImageSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DB.TABLE_NAME + "(" + DB.COLUMNS_IMAGE_ID + " INTEGER PRIMARY KEY," + DB.COLUMNS_SOURCE_PATH + " TEXT," + DB.COLUMNS_EDITED_PATH + " TEXT," + DB.COLUMNS_EDITED_COUNT + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
