package com.ek.model;

public class PhotoBean {
    int sort;
    int comic_id;
    String ext_path_folder;
    String new_filename;
    String extension;
    int version;

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getComic_id() {
        return comic_id;
    }

    public void setComic_id(int comic_id) {
        this.comic_id = comic_id;
    }

    public String getExt_path_folder() {
        return ext_path_folder;
    }

    public void setExt_path_folder(String ext_path_folder) {
        this.ext_path_folder = ext_path_folder;
    }

    public String getNew_filename() {
        return new_filename;
    }

    public void setNew_filename(String new_filename) {
        this.new_filename = new_filename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
