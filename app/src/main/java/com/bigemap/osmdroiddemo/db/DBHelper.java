package com.bigemap.osmdroiddemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.bigemap.osmdroiddemo.constants.Constant;

/**
 * 数据库表工具类
 * Created by Think on 2017/9/13.
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = Constant.DATABASE_PATH;
    public static final int DATABASE_VERSION = 1;

    public final static String GMTTime = "GMTTime";
    /**
     * TRACKS_TABLE
     */
    public final static String TRACKS_TABLE = "tracks";
    public final static String FIELD_trackid = "trackid"; // column 0 PK
    public final static String FIELD_name = "name"; // column 1
    public final static String FIELD_description = "description";// column 2
    public final static String FIELD_startTime = "start_time"; // column 3
    public final static String FIELD_totalTime = "total_time"; // column 4
    public final static String FIELD_totalDistance = "total_distance"; // column 5
    public final static String FIELD_averageSpeed = "average_speed"; // column 6
    public final static String FIELD_maximumSpeed = "maximum_speed"; // column 7
    public final static String FIELD_minAltitude = "min_altitude"; // column 8
    public final static String FIELD_maxAltitude = "max_altitude"; // column 9
    public final static String FIELD_trackPoints = "points"; // column 10
    public final static String FIELD_trackSource = "source"; // column 11
    public final static String FIELD_trackType = "type"; // column 12
    public final static String FIELD_measureVersion = "measure_version"; // column 13
    /**
     * WAYPOINTS_TABLE
     */
    public final static String WAYPOINTS_TABLE = "waypoints";
    public final static String FIELD_pointid = "pointid"; // column 0 PK
//    public final static String FIELD_trackid = "trackid"; // column 1 FK
    public final static String FIELD_time = "time";// column 2
    public final static String FIELD_latitude = "latitude";// column 3
    public final static String FIELD_longitude = "longitude";// column 4
    public final static String FIELD_altitude = "altitude";// column 5
    public final static String FIELD_speed = "speed";// column 6
    public final static String FIELD_bearing = "bearing";// column 7
    public final static String FIELD_accuracy = "accuracy";// column 8

    public DBHelper(Context context) {
        // CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String CREATE_TRACK_POINTS_TABLE = "CREATE TABLE IF NOT EXISTS " + WAYPOINTS_TABLE + " ("
            + FIELD_pointid + " INTEGER primary key autoincrement, "
            + " " + FIELD_trackid + " INTEGER NOT NULL,"
            + " " + FIELD_time + " CHAR(20), "
            + " " + FIELD_latitude + " DOUBLE DEFAULT '0', "
            + " " + FIELD_longitude + " DOUBLE DEFAULT '0', "
            + " " + FIELD_altitude + " DOUBLE DEFAULT '0', "
            + " " + FIELD_speed + " FLOAT DEFAULT '0', "
            + " " + FIELD_bearing + " FLOAT DEFAULT '0', "
            + " " + FIELD_accuracy + " FLOAT DEFAULT '0');";

    public static final String DELETE_TRACK_POINTS_TABLE =
            "DROP TABLE IF EXISTS " + WAYPOINTS_TABLE;

    private static final String CREATE_TRACKS_TABLE ="CREATE TABLE IF NOT EXISTS " + TRACKS_TABLE + " ("
            + FIELD_trackid + " INTEGER primary key autoincrement, "
            + " "+ FIELD_name + " VARCHAR,"
            + " "+ FIELD_description + " VARCHAR, "
            + " "+ FIELD_startTime + " CHAR(20), "
            + " "+ FIELD_totalTime + " CHAR(20), "
            + " "+ FIELD_totalDistance + " FLOAT, "
            + " "+ FIELD_averageSpeed + " FLOAT, "
            + " "+ FIELD_maximumSpeed + " FLOAT, "
            + " "+ FIELD_minAltitude + " DOUBLE, "
            + " "+ FIELD_maxAltitude + " DOUBLE, "
            + " "+ FIELD_trackPoints + " INTEGER, "
            + " "+ FIELD_trackSource + " INTEGER, "
            + " "+ FIELD_trackType + " INTEGER, "
            + " "+ FIELD_measureVersion + " INTEGER);";

    private static final String DELETE_TRACK_TABLE =
            "DROP TABLE IF EXISTS " + TRACKS_TABLE;
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TRACK_POINTS_TABLE);
        db.execSQL(CREATE_TRACKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL(DELETE_TRACK_POINTS_TABLE);
//        db.execSQL(DELETE_TRACK_TABLE);
    }
}
