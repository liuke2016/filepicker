package com.lynn.filepicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.lynn.filepicker.FilePicker;
import com.lynn.filepicker.entity.BaseFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuke on 2017/5/12.
 */

public abstract class BasePickerAdapter<T extends BaseFile> extends RecyclerView.Adapter {


    ArrayList<T> mSelectedList = new ArrayList<>();
    Context mContext;
    ArrayList<T> mFiles;

    BasePickerAdapter(Context context, ArrayList<T> files) {
        mContext = context;
        mFiles = files;
    }

    public void add(List<T> list) {
        mFiles.addAll(list);
        notifyDataSetChanged();
    }

    public void add(T file) {
        mFiles.add(file);
        notifyDataSetChanged();
    }

    public void add(int index, T file) {
        mFiles.add(index, file);
        notifyDataSetChanged();
    }

    public BaseFile get(int index) {
        return mFiles.get(index);
    }

    public void refresh(List<T> list) {
        mFiles.clear();
        mFiles.addAll(list);
        notifyDataSetChanged();
    }

    public void refresh(T file) {
        mFiles.clear();
        mFiles.add(file);
        notifyDataSetChanged();
    }

    public List<T> getDataSet() {
        return mFiles;
    }

    public ArrayList<T> getSelectedList() {
        return mSelectedList;
    }
    public void setSelectedList(ArrayList<T> selectedList) {
        mSelectedList.clear();
        mSelectedList.addAll(selectedList);
    }


    @Override
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public abstract int getItemCount();


    protected boolean isUpToMax() {
        return mSelectedList.size() >= FilePicker.getPickerConfig().getMaxNumber();
    }
}
