package com.sscctv.seeeyesonvif.Map.utils;

import android.net.Uri;

/**
 * Author CodeBoy722
 *
 * Custom class for holding data of images on the device external storage
 */
public class ItemPictureList {

    private String pictureName;
    private String picturePath;
    private String pictureSize;
    private Uri imageUri;
    private Boolean selected = false;
    private boolean isRemoveSel;

    public ItemPictureList(){

    }

    public ItemPictureList(String picturName, String picturePath, String pictureSize, Uri imageUri) {
        this.pictureName = picturName;
        this.picturePath = picturePath;
        this.pictureSize = pictureSize;
        this.imageUri = imageUri;
    }


    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }

    public String getPictureSize() {
        return pictureSize;
    }

    public void setPictureSize(String pictureSize) {
        this.pictureSize = pictureSize;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public boolean isRemoveSel() {
        return isRemoveSel;
    }

    public void setRemoveSel(boolean removeSel) {
        isRemoveSel = removeSel;
    }
}
