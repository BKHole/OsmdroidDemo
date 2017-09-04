package com.bigemap.osmdroiddemo.constants;

/**
 * 全局常量类
 * Created by Think on 2017/9/1.
 */

public class Constant {
    public static final String PREFS_NAME = "com.bigemap.prefs";
    public static final String PREFS_TILE_SOURCE = "tilesource";
    public static final String PREFS_SCROLL_X = "scrollX";
    public static final String PREFS_SCROLL_Y = "scrollY";
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
     */
    public static final String URL_MAP_GOOGLE="http://mt3.google.cn/vt/lyrs=m@365000000&hl=zh-CN&gl=cn";
    public static final String URL_MAP_GOOGLE_SATELLITE="http://mt3.google.cn/vt/lyrs=s@76&gl=cn";

}
