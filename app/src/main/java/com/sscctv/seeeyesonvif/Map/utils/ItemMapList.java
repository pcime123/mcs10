package com.sscctv.seeeyesonvif.Map.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ItemMapList implements Parcelable {

    private String imgPath;
    private String name;
    private String location;
    private ArrayList<ItemMapConfig> devList;

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<ItemMapConfig> getDevList() {
        return devList;
    }

    public void setDevList(ArrayList<ItemMapConfig> devList) {
        this.devList = devList;
    }

    public ItemMapList() {

    }

    public ItemMapList(Parcel in) {
        this.imgPath = in.readString();
        this.name = in.readString();
        this.location = in.readString();
        this.devList = in.readArrayList(null);
    }

    public ItemMapList(String imgPath, String name, String location, ArrayList<ItemMapConfig> devList) {
        this.imgPath = imgPath;
        this.name = name;
        this.location = location;
        this.devList = devList;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(location);
        parcel.writeString(imgPath);
        parcel.writeList(devList);
    }

    public static Creator<ItemMapList> CREATOR = new Creator<ItemMapList>() {

        @Override
        public ItemMapList createFromParcel(Parcel source) {
            return new ItemMapList(source);
        }

        @Override
        public ItemMapList[] newArray(int size) {
            return new ItemMapList[size];
        }

    };
}
