package com.bigemap.osmdroiddemo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;

import com.bigemap.osmdroiddemo.entity.Track;
import com.bigemap.osmdroiddemo.utils.DateUtils;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_accuracy;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_altitude;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_averageSpeed;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_bearing;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_description;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_latitude;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_longitude;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_maxAltitude;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_maximumSpeed;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_measureVersion;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_minAltitude;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_name;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_speed;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_startTime;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_time;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_totalDistance;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_totalTime;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_trackPoints;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_trackSource;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_trackType;
import static com.bigemap.osmdroiddemo.db.DBHelper.FIELD_trackid;
import static com.bigemap.osmdroiddemo.db.DBHelper.GMTTime;
import static com.bigemap.osmdroiddemo.db.DBHelper.TRACKS_TABLE;
import static com.bigemap.osmdroiddemo.db.DBHelper.WAYPOINTS_TABLE;

/**
 * 数据库表控制类
 * Created by Think on 2017/9/13.
 */

public class TrackDao {
    private DBHelper helper;
    private SQLiteDatabase db;

    public TrackDao(Context context) {
        helper = BeanFactory.getDBHelper(context);
        // 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
        // mFactory);
        // 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    public long insertTrack(String trackName, String trackDescription, String startGMTTime,
                            ArrayList<GeoPoint> locationList, String trackSource, int trackType) {
        long trackID = 0;
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(FIELD_name, trackName);
            cv.put(FIELD_description, trackDescription);
            cv.put(FIELD_startTime, startGMTTime);
            cv.put(FIELD_trackSource, trackSource);
            cv.put(FIELD_trackType, trackType);
            trackID = db.insert(TRACKS_TABLE, null, cv);

            for (GeoPoint loc: locationList) {
                String strGMTTime = "";
//                String strGMTTime = DateUtils.formatUTC(loc.getTime(),null);
//                if (loc.getExtras() != null)
//                    strGMTTime = loc.getExtras().getString(GMTTime);
                cv = new ContentValues();
                cv.put(FIELD_trackid, trackID);
                cv.put(FIELD_time, strGMTTime);
                cv.put(FIELD_latitude, loc.getLatitude());
                cv.put(FIELD_longitude, loc.getLongitude());
                cv.put(FIELD_altitude, loc.getAltitude());
//                cv.put(FIELD_speed, loc.getSpeed());
//                cv.put(FIELD_bearing, loc.getBearing());
//                cv.put(FIELD_accuracy, loc.getAccuracy());
                db.insert(WAYPOINTS_TABLE, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return trackID;
    }

    public long addTrack(String trackName, String trackDescription, String startGMTTime,
                            ArrayList<Location> locationList, String trackSource, int trackType) {
        long trackID = 0;
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(FIELD_name, trackName);
            cv.put(FIELD_description, trackDescription);
            cv.put(FIELD_startTime, startGMTTime);
            cv.put(FIELD_trackSource, trackSource);
            cv.put(FIELD_trackType, trackType);
            trackID = db.insert(TRACKS_TABLE, null, cv);

            for (Location loc: locationList) {
                String strGMTTime = DateUtils.formatUTC(loc.getTime(),null);
//                if (loc.getExtras() != null)
//                    strGMTTime = loc.getExtras().getString(GMTTime);
                cv = new ContentValues();
                cv.put(FIELD_trackid, trackID);
                cv.put(FIELD_time, strGMTTime);
                cv.put(FIELD_latitude, loc.getLatitude());
                cv.put(FIELD_longitude, loc.getLongitude());
                cv.put(FIELD_altitude, loc.getAltitude());
                cv.put(FIELD_speed, loc.getSpeed());
                cv.put(FIELD_bearing, loc.getBearing());
                cv.put(FIELD_accuracy, loc.getAccuracy());
                db.insert(WAYPOINTS_TABLE, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return trackID;
    }

    //---deletes a particular track---
    public boolean deleteTrack(long rowId) {
        int rowsAffected = 0;
        db.beginTransaction();
        try {
            rowsAffected = db.delete(WAYPOINTS_TABLE, FIELD_trackid + "=" + rowId, null);
            rowsAffected = db.delete(TRACKS_TABLE, FIELD_trackid + "=" + rowId, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return rowsAffected > 0;
    }

    /**
     * 获取所有轨迹路线
     * @return List<Track>
     */
    public List<Track> getAllTracks() {
        List<Track> trackList=new ArrayList<>();
        Cursor cursor=db.query(TRACKS_TABLE, new String[] {
                        FIELD_trackid,
                        FIELD_name,
                        FIELD_description,
                        FIELD_startTime,
                        FIELD_totalTime,
                        FIELD_totalDistance,
                        FIELD_averageSpeed,
                        FIELD_maximumSpeed,
                        FIELD_minAltitude,
                        FIELD_maxAltitude,
                        FIELD_trackPoints,
                        FIELD_trackSource,
                        FIELD_trackType,
                        FIELD_measureVersion },
                null,
                null,
                null,
                null,
                FIELD_trackid + " DESC");
        while(cursor.moveToNext()){
            Track track=new Track();
            track.setTrackid(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_trackid)));
            track.setName(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_name)));
            track.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_description)));
            track.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_startTime)));
            track.setTotalTime(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_totalTime)));
            track.setTotalDistance(cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_totalDistance)));
            track.setAveSpeed(cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_averageSpeed)));
            track.setMaxSpeed(cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_maximumSpeed)));
            track.setMinAltitude(cursor.getDouble(cursor.getColumnIndexOrThrow(FIELD_minAltitude)));
            track.setMaxAltitude(cursor.getDouble(cursor.getColumnIndexOrThrow(FIELD_maxAltitude)));
            track.setTrackPoints(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_trackPoints)));
            track.setTrackSource(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_trackSource)));
            track.setTrackType(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_trackType)));
            track.setMeasureVersion(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_measureVersion)));
            trackList.add(track);
        }
        cursor.close();
        return trackList;
    }

    //---retrieves a particular track---
    public Track getTrack(long rowId) throws SQLException {
        Track track=new Track();
        Cursor cursor = db.query(true, TRACKS_TABLE, new String[] {
                        FIELD_trackid,
                        FIELD_name,
                        FIELD_description,
                        FIELD_startTime,
                        FIELD_totalTime,
                        FIELD_totalDistance,
                        FIELD_averageSpeed,
                        FIELD_maximumSpeed,
                        FIELD_minAltitude,
                        FIELD_maxAltitude,
                        FIELD_trackPoints,
                        FIELD_trackSource,
                        FIELD_trackType,
                        FIELD_measureVersion
                },
                FIELD_trackid + "=" + rowId,
                null,
                null,
                null,
                null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            track.setTrackid(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_trackid)));
            track.setName(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_name)));
            track.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_description)));
            track.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_startTime)));
            track.setTotalTime(cursor.getString(cursor.getColumnIndexOrThrow(FIELD_totalTime)));
            track.setTotalDistance(cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_totalDistance)));
            track.setAveSpeed(cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_averageSpeed)));
            track.setMaxSpeed(cursor.getFloat(cursor.getColumnIndexOrThrow(FIELD_maximumSpeed)));
            track.setMinAltitude(cursor.getDouble(cursor.getColumnIndexOrThrow(FIELD_minAltitude)));
            track.setMaxAltitude(cursor.getDouble(cursor.getColumnIndexOrThrow(FIELD_maxAltitude)));
            track.setTrackPoints(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_trackPoints)));
            track.setTrackSource(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_trackSource)));
            track.setTrackType(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_trackType)));
            track.setMeasureVersion(cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_measureVersion)));
            cursor.close();
        }

        return track;
    }

    //---updates a track---
    public boolean updateTrack(long trackID, String trackName, String trackDescription) {
        ContentValues cv = new ContentValues();
        cv.put(FIELD_name, trackName);
        cv.put(FIELD_description, trackDescription);
        return db.update(TRACKS_TABLE, cv, FIELD_trackid + "=" + trackID, null) > 0;
    }

    public boolean updateTrack(long trackID, long totalTime, float totalDistance,
                               float averageSpeed, float maximumSpeed, double minAltitude, double maxAltitude,
                               long trackPoints, int measureVersion) {
        ContentValues cv = new ContentValues();
        cv.put(FIELD_totalTime, totalTime);
        cv.put(FIELD_totalDistance, totalDistance);
        cv.put(FIELD_averageSpeed, averageSpeed);
        cv.put(FIELD_maximumSpeed, maximumSpeed);
        cv.put(FIELD_minAltitude, minAltitude);
        cv.put(FIELD_maxAltitude, maxAltitude);
        cv.put(FIELD_trackPoints, trackPoints);
        cv.put(FIELD_measureVersion, measureVersion);
        return db.update(TRACKS_TABLE, cv, FIELD_trackid + "=" + trackID, null) > 0;
    }

    //---retrieves all trackPoints---
    public ArrayList<Location> getTrackPoints(long rowId) throws SQLException {
        ArrayList<Location> locations=new ArrayList<>();
        Cursor mCursor = db.query(false, WAYPOINTS_TABLE, new String[] {
                        FIELD_trackid,
                        FIELD_time,
                        FIELD_latitude,
                        FIELD_longitude,
                        FIELD_altitude,
                        FIELD_speed,
                        FIELD_bearing,
                        FIELD_accuracy,
                },
                FIELD_trackid + "=" + rowId,
                null,
                null,
                null,
                null,
                null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            for (int i=0; i < mCursor.getCount(); i++) {
                double latitude = mCursor.getDouble(mCursor.getColumnIndexOrThrow(FIELD_latitude));
                double longitude = mCursor.getDouble(mCursor.getColumnIndexOrThrow(FIELD_longitude));
                double altitude = mCursor.getDouble(mCursor.getColumnIndexOrThrow(FIELD_altitude));
                String strGMTTime = mCursor.getString(mCursor.getColumnIndexOrThrow(FIELD_time));

                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                location.setAltitude(altitude);
                location.setTime(DateUtils.getGMTTime(strGMTTime));
                locations.add(location);
                mCursor.moveToNext();
            }
            mCursor.close();
        }
        return locations;
    }

    /**
     * clear all tables
     */
    public void clearAll(){
        String sql_track="delete from "+DBHelper.TRACKS_TABLE;
        String sql_track_points="delete from "+DBHelper.WAYPOINTS_TABLE;
        db.execSQL(sql_track);
        db.execSQL(sql_track_points);
    }

    public static void setGMTTimeString(Location location, String strGMTTime) {
        Bundle bundle = new Bundle();
        bundle.putString(GMTTime, strGMTTime);
        location.setExtras(bundle);
    }

    public static String getGMTTimeString(Location location) {
        Bundle bundle = location.getExtras();
        String strGMTTime = bundle.getString(GMTTime);
        return strGMTTime;
    }
}
