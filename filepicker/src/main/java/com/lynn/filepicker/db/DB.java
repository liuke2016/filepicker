package com.lynn.filepicker.db;

/**
 * Created by liuke on 2017/6/7.
 */

public interface DB {
    String DB_NAME = "ImagePicker";
    String TABLE_NAME = "EDITED_IMAGE";
    String COLUMNS_IMAGE_ID = "IMAGE_ID";
    String COLUMNS_SOURCE_PATH = "SOURCE_PATH";
    String COLUMNS_EDITED_PATH = "EDITED_PATH";
    String COLUMNS_EDITED_COUNT = "EDITED_COUNT";
}
