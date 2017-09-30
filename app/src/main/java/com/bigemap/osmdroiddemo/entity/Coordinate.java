package com.bigemap.osmdroiddemo.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * kml解析实体类，存储kml解析后的关键数据
 * Created blatitude Think on 2017/9/25.
 */

public class Coordinate implements Parcelable {
    private double longitude;//经度
    private double latitude;//纬度
    private String name;
    private String description;//描述

    public Coordinate(double longitude, double latitude, String name)
    {
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
    }
    
    public Coordinate(double longitude, double latitude, String name, String description){
        this(longitude,latitude,name);
        this.description=description;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.latitude);
        dest.writeString(this.name);
        dest.writeString(this.description);
    }

    protected Coordinate(Parcel in) {
        this.longitude = in.readDouble();
        this.latitude = in.readDouble();
        this.name = in.readString();
        this.description = in.readString();
    }

    public static final Creator<Coordinate> CREATOR = new Creator<Coordinate>() {
        @Override
        public Coordinate createFromParcel(Parcel source) {
            return new Coordinate(source);
        }

        @Override
        public Coordinate[] newArray(int size) {
            return new Coordinate[size];
        }
    };
}
