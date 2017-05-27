package com.lynn.filepicker.entity;

import java.util.ArrayList;
import java.util.List;



public class Folder<T extends BaseFile> {
    private String id;
    private String name;
    private String path;
    private String coverPath;
    private boolean isSelected;
    private List<T> files = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T> getFiles() {
        return files;
    }

    public void setFiles(List<T> files) {
        this.files = files;
    }

    public void addFile(T file) {
        files.add(file);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Folder)) return false;
        Folder folder = (Folder) o;
        return this.path.equals(folder.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
