package com.bigemap.osmdroiddemo.constants;

import android.os.Environment;

/**
 * 全局常量类
 * Created by Think on 2017/9/1.
 */

public class Constant {
    public static final String PREFS_NAME = "com.bigemap.prefs";
    public static final String PREFS_TILE_SOURCE = "tilesource";
    /**
     * as String because we cannot use double in Preferences, only float
     * and float is not accurate enough
     */
    public static final String PREFS_LATITUDE_STRING = "latitudeString";
    public static final String PREFS_LONGITUDE_STRING = "longitudeString";
    public static final String PREFS_ORIENTATION = "orientation";
    public static final String PREFS_ZOOM_LEVEL = "zoomLevel";
    public static final String PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble";
    public static final String PREFS_SHOW_LOCATION = "showLocation";
    public static final String PREFS_SHOW_COMPASS = "showCompass";
    /**
     * map source url
     * m:路线图（r）
     * t:地形图
     * p:带标签的地形图
     * s:卫星图
     * y:带标签的卫星图
     */
    public static final String URL_MAP_GOOGLE = "http://mt0.google.cn/vt/lyrs=m&hl=zh-CN&gl=cn&scale=2";
    public static final String URL_MAP_GOOGLE_SATELLITE = "http://mt0.google.cn/vt/lyrs=y&hl=zh-CN&gl=cn&scale=2";

    /**
     * tile source
     */
    public static final int GOOGLE_MAP = 0;//GOOGLE_MAP
    public static final int GOOGLE_SATELLITE = 1;//GOOGLE_SATELLITE
    public static final int OSM = 2;//OSM

    /**
     * file storage path
     */
    private static String APP_BASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/AOsmDemo";
    public static String DATABASE_PATH = APP_BASE_PATH + "/map-tracks.db";
    public static String IMPORT_KML_PATH = APP_BASE_PATH + "/imports";
    public static String EXPORT_KML_PATH = APP_BASE_PATH + "/exports";

    public static final String POST_URL="http://kh.bigemap.com/loginm.php";
}
