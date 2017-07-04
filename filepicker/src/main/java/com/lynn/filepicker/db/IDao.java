package com.lynn.filepicker.db;

import com.lynn.filepicker.entity.ImageFile;

/**
 * Created by liuke on 2017/6/15.
 */

public interface IDao {
    void insert(ImageFile imageFile);
    void update(ImageFile imageFile);
    void find(ImageFile imageFile);
}
