package com.bigemap.osmdroiddemo.constants;

import android.os.Environment;

/**
 * 全局常量类
 * Created by Think on 2017/9/1.
 */

public class Constant {
    public static final String PREFS_NAME = "com.bigemap.prefs";
    public static final String PREFS_TILE_SOURCE = "tilesource";//瓦片源
    public static final String PREFS_MAP_SOURCE = "mapsource";//在线地图选择
    public static final String PREFS_OFFLINE_MAP_SOURCE = "offlinemapsource";//离线线地图选择
    /**
     * as String because we cannot use double in Preferences, only float
     * and float is not accurate enough
     */
    public static final String PREFS_LATITUDE_STRING = "latitudeString";
    public static final String PREFS_LONGITUDE_STRING = "longitudeString";
    public static final String PREFS_ORIENTATION = "orientation";
    public static final String PREFS_ZOOM_LEVEL = "zoomLevel";
    public static final String PREFS_ZOOM_LEVEL_DOUBLE = "zoomLevelDouble";
    public static final String PREFS_NORMAL_MAP_STATE = "normalMapState";
    public static final String PREFS_SATELLITE_STATE = "satelliteState";
    public static final String PREFS_OFFLINE_ELECTRIC_STATE = "offlineElectric";
    public static final String PREFS_OFFLINE_SATELLITE_STATE = "offlineSatellite";
    public static final String PREFS_OFFLINE_ELE_PATH = "offlineElePath";
    public static final String PREFS_OFFLINE_SATEL_PATH = "offlineSatelPath";
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
    public static final int OSM = 1;//OSM

    /**
     * track type
     */
    public static final String POLYGON = "polygon";
    public static final String POLYLINE = "line";
    public static final String POI = "poi";
    /**
     * file storage path
     */
    public static String APP_BASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/AOsmDemo";
    public static String IMPORT_KML_PATH = APP_BASE_PATH + "/kml";
    public static String OFFLINE_MAP_PATH = APP_BASE_PATH + "/offline";
    public static String ELECTRONIC_MAP_PATH = OFFLINE_MAP_PATH + "/electronic";
    public static String SATELLITE_MAP_PATH = OFFLINE_MAP_PATH + "/satellite";
    public static String TILE_CACHE = OFFLINE_MAP_PATH + "/cache";

    public static final String POST_URL="http://kh.bigemap.com/loginm.php";
}
