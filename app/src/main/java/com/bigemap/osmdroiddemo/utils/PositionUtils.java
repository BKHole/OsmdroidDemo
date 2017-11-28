package com.bigemap.osmdroiddemo.utils;

import android.location.Location;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * 坐标系转换类
 * Created by Think on 2017/9/7.
 */

public class PositionUtils {
    private static double pi = 3.1415926535897932384626;
    private static double a = 6378245.0;
    private static double ee = 0.00669342162296594323;

    /**
     * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
     *
     * @param geoPoint
     * @return GeoPoint
     */
    public static GeoPoint gps84_To_Gcj02(GeoPoint geoPoint) {
        double lat = geoPoint.getLatitude();
        double lon = geoPoint.getLongitude();
        if (outOfChina(lat, lon)) {
            return null;
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new GeoPoint(mgLat, mgLon);
    }

    /**
     * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
     *
     * @param location
     * @return GeoPoint
     */
    public static GeoPoint gps84_To_Gcj02(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        if (outOfChina(lat, lon)) {
            return null;
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new GeoPoint(mgLat, mgLon);
    }

    /**
     * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
     *
     * @param location
     * @return GeoPoint
     */
    public static Location gps_To_Gcj02(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        if (outOfChina(lat, lon)) {
            return null;
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        location.setLatitude(mgLat);
        location.setLongitude(mgLon);
        return location;
    }

    /**
     * * 火星坐标系 (GCJ-02) to 84 * * @param lon * @param lat * @return
     */
    public static GeoPoint gcj_To_Gps84(GeoPoint geoPoint) {
        double lat = geoPoint.getLatitude();
        double lon = geoPoint.getLongitude();
        GeoPoint gps = transform(lat, lon);
        double longitude = lon * 2 - gps.getLongitude();
        double latitude = lat * 2 - gps.getLatitude();
        return new GeoPoint(latitude, longitude);
    }

    public static List<GeoPoint> gcjToGps(List<GeoPoint> geoPoints){
        List<GeoPoint> points=new ArrayList<>();
        for (GeoPoint geoPoint: geoPoints){
            points.add(gcj_To_Gps84(geoPoint));
        }
        return points;
    }
    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param geoPoint
     * @return GeoPoint
     */
    public static GeoPoint gcj02_To_Bd09(GeoPoint geoPoint) {
        double x = geoPoint.getLatitude(), y = geoPoint.getLongitude();
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new GeoPoint(bd_lat, bd_lon);
    }

    /**
     * * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 * * 将 BD-09 坐标转换成GCJ-02 坐标 * * @param
     * bd_lat * @param bd_lon * @return
     *
     * @param geoPoint
     * @return GeoPoint
     */
    public static GeoPoint bd09_To_Gcj02(GeoPoint geoPoint) {
        double bd_lat = geoPoint.getLatitude();
        double bd_lon = geoPoint.getLongitude();
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new GeoPoint(gg_lat, gg_lon);
    }

    /**
     * (BD-09)-->84
     *
     * @param geoPoint
     * @return GeoPoint
     */
    public static GeoPoint bd09_To_Gps84(GeoPoint geoPoint) {
        GeoPoint gcj02 = bd09_To_Gcj02(geoPoint);
        return gcj_To_Gps84(gcj02);

    }

    private static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    private static GeoPoint transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new GeoPoint(lat, lon);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new GeoPoint(mgLat, mgLon);
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;
        return ret;
    }

    public static ArrayList<GeoPoint> wgsToGcj(List<Location> locations){
        ArrayList<GeoPoint> geoPoints=new ArrayList<>();
        for (Location location: locations){
            geoPoints.add(gps84_To_Gcj02(location));
        }
        return geoPoints;
    }

    public static List<GeoPoint> wgsToGcj02(List<GeoPoint> locations){
        List<GeoPoint> geoPoints=new ArrayList<>();
        for (GeoPoint location: locations){
            geoPoints.add(gps84_To_Gcj02(location));
        }
        return geoPoints;
    }

    /**
     * from String to GeoPoint
     * @param data string
     * @return GeoPoint
     */
    public static GeoPoint stringToPoint(String data) {
        GeoPoint geoPoint = null;
        data = data.trim();
        String[] split = data.split(",");
        if (split.length > 1) {
            geoPoint = new GeoPoint(Double.valueOf(split[1].trim()), Double.valueOf(split[0].trim()));
        }
        return geoPoint;
    }
}
