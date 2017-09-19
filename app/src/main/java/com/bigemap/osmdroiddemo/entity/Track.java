package com.bigemap.osmdroiddemo.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用于存储轨迹
 * Created by Think on 2017/9/13.
 */

public class Track implements Parcelable {
    private int trackid;
    private String name;
    private String description;
    private String startTime;
    private String totalTime;
    private float totalDistance;
    private float aveSpeed;
    private float maxSpeed;
    private double minAltitude;
    private double maxAltitude;
    private int trackPoints;
    private int trackSource;

    public int getTrackid() {
        return trackid;
    }

    public void setTrackid(int trackid) {
        this.trackid = trackid;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(float totalDistance) {
        this.totalDistance = totalDistance;
    }

    public float getAveSpeed() {
        return aveSpeed;
    }

    public void setAveSpeed(float aveSpeed) {
        this.aveSpeed = aveSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getMinAltitude() {
        return minAltitude;
    }

    public void setMinAltitude(double minAltitude) {
        this.minAltitude = minAltitude;
    }

    public double getMaxAltitude() {
        return maxAltitude;
    }

    public void setMaxAltitude(double maxAltitude) {
        this.maxAltitude = maxAltitude;
    }

    public int getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(int trackPoints) {
        this.trackPoints = trackPoints;
    }

    public int getTrackSource() {
        return trackSource;
    }

    public void setTrackSource(int trackSource) {
        this.trackSource = trackSource;
    }

    public int getMeasureVersion() {
        return measureVersion;
    }

    public void setMeasureVersion(int measureVersion) {
        this.measureVersion = measureVersion;
    }

    private int measureVersion;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.trackid);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.startTime);
        dest.writeString(this.totalTime);
        dest.writeFloat(this.totalDistance);
        dest.writeFloat(this.aveSpeed);
        dest.writeFloat(this.maxSpeed);
        dest.writeDouble(this.minAltitude);
        dest.writeDouble(this.maxAltitude);
        dest.writeInt(this.trackPoints);
        dest.writeInt(this.trackSource);
        dest.writeInt(this.measureVersion);
    }

    public Track() {
    }

    protected Track(Parcel in) {
        this.trackid = in.readInt();
        this.name = in.readString();
        this.description = in.readString();
        this.startTime = in.readString();
        this.totalTime = in.readString();
        this.totalDistance = in.readFloat();
        this.aveSpeed = in.readFloat();
        this.maxSpeed = in.readFloat();
        this.minAltitude = in.readDouble();
        this.maxAltitude = in.readDouble();
        this.trackPoints = in.readInt();
        this.trackSource = in.readInt();
        this.measureVersion = in.readInt();
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel source) {
            return new Track(source);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };
}
