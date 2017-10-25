package com.bigemap.osmdroiddemo.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * 轨迹表
 * Created by Think on 2017/9/13.
 */

public class Track extends DataSupport implements Parcelable {
    private int id;
    private String name;
    private String description;
    private String startTime;
    private String totalTime;
    private float totalDistance;
    private float aveSpeed;
    private float maxSpeed;
    private double minAltitude;
    private double maxAltitude;
    private int trackSource;
    private int trackType;
    private List<Location> locations =new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getTrackSource() {
        return trackSource;
    }

    public void setTrackSource(int trackSource) {
        this.trackSource = trackSource;
    }

    public int getTrackType() {
        return trackType;
    }

    public void setTrackType(int trackType) {
        this.trackType = trackType;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.startTime);
        dest.writeString(this.totalTime);
        dest.writeFloat(this.totalDistance);
        dest.writeFloat(this.aveSpeed);
        dest.writeFloat(this.maxSpeed);
        dest.writeDouble(this.minAltitude);
        dest.writeDouble(this.maxAltitude);
        dest.writeInt(this.trackSource);
        dest.writeInt(this.trackType);
        dest.writeList(this.locations);
    }

    public Track() {
    }

    protected Track(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.description = in.readString();
        this.startTime = in.readString();
        this.totalTime = in.readString();
        this.totalDistance = in.readFloat();
        this.aveSpeed = in.readFloat();
        this.maxSpeed = in.readFloat();
        this.minAltitude = in.readDouble();
        this.maxAltitude = in.readDouble();
        this.trackSource = in.readInt();
        this.trackType = in.readInt();
        this.locations = new ArrayList<Location>();
        in.readList(this.locations, Location.class.getClassLoader());
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
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
