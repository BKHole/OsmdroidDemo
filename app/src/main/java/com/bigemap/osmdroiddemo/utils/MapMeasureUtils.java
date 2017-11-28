package com.bigemap.osmdroiddemo.utils;

import android.util.Log;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.util.constants.MathConstants;

import java.util.List;

/**
 * 地图面积计算
 * Created by Think on 2017/9/28.
 */

public class MapMeasureUtils implements MathConstants,GeoConstants{
    private static final String TAG = "MapMeasureUtils";
    private static double metersPerDegree = 2.0 * Math.PI * RADIUS_EARTH_METERS / 360.0;

    /**
     * 计算周长
     *
     * @param points 经纬度坐标
     * @return perimeter 单位：米
     */
    public static String calculatePerimeter(List<GeoPoint> points) {
        int distance = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            distance += getDistance(points.get(i), points.get(i + 1));
        }
        return String.valueOf(distance);
    }

    /**
     * 计算两点间距离
     *
     * @param point1 坐标点1
     * @param point2 坐标点2
     * @return distance in meters
     */
    private static int getDistance(GeoPoint point1, GeoPoint point2) {
        return point1.distanceTo(point2);
    }
    
    /**
     * 计算面积
     *
     * @param points 经纬度坐标
     * @return area 单位：平方公里
     */
    public static double calculateArea(List<GeoPoint> points) {
        double areaMeters2 = PlanarPolygonAreaMeters2(points);
        double squareKilometer = areaMeters2 / 1000000.0;
        if (areaMeters2 > 1000000.0) {
            areaMeters2 = SphericalPolygonAreaMeters2(points);
            squareKilometer = areaMeters2 / 1000000.0;
        }
        Log.d(TAG, "面积为：" + squareKilometer + "（平方公里）");
        return squareKilometer;
    }

    /**
     * 球面多边形面积计算
     *
     * @param points
     * @return
     */
    private static double SphericalPolygonAreaMeters2(List<GeoPoint> points) {
        double totalAngle = 0.0;
        for (int i = 0; i < points.size(); ++i) {
            int j = (i + 1) % points.size();
            int k = (i + 2) % points.size();
            totalAngle += Angle(points.get(i), points.get(j), points.get(k));
        }
        double planarTotalAngle = (points.size() - 2) * 180.0;
        double sphericalExcess = totalAngle - planarTotalAngle;
        if (sphericalExcess > 420.0) {
            totalAngle = points.size() * 360.0 - totalAngle;
            sphericalExcess = totalAngle - planarTotalAngle;
        } else if (sphericalExcess > 300.0 && sphericalExcess < 420.0) {
            sphericalExcess = Math.abs(360.0 - sphericalExcess);
        }
        return sphericalExcess * DEG2RAD * RADIUS_EARTH_METERS * RADIUS_EARTH_METERS;
    }

    /**
     * 角度
     * * @param p1
     * * @param p2
     * * @param p3
     * * @return Angle double
     */
    private static double Angle(GeoPoint p1, GeoPoint p2, GeoPoint p3) {
        double bearing21 = Bearing(p2, p1);
        double bearing23 = Bearing(p2, p3);
        double angle = bearing21 - bearing23;
        if (angle < 0.0) angle += 360.0;
        return angle;
    }

    /**
     * 方向
     * * @param from
     * * @param to
     * * @return bearing double
     */
    private static double Bearing(GeoPoint from, GeoPoint to) {
        double lat1 = from.getLatitude() * DEG2RAD;
        double lon1 = from.getLongitude() * DEG2RAD;
        double lat2 = to.getLatitude() * DEG2RAD;
        double lon2 = to.getLongitude() * DEG2RAD;
        double angle = -Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2), Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
        if (angle < 0.0) angle += Math.PI * 2.0;
        angle = angle * RAD2DEG;
        return angle;
    }

    /**
     * 平面多边形面积
     * @param points 多边形经纬度坐标
     * @return area 平面多边形面积
     */
    private static double PlanarPolygonAreaMeters2(List<GeoPoint> points) {
        double a = 0.0;
        for (int i = 0; i < points.size(); ++i) {
            int j = (i + 1) % points.size();
            double xi = points.get(i).getLongitude() * metersPerDegree * Math.cos(points.get(i).getLatitude() * DEG2RAD);
            double yi = points.get(i).getLatitude() * metersPerDegree;
            double xj = points.get(j).getLongitude() * metersPerDegree * Math.cos(points.get(j).getLatitude() * DEG2RAD);
            double yj = points.get(j).getLatitude() * metersPerDegree;
            a += xi * yj - xj * yi;
        }
        return Math.abs(a / 2.0);
    }
}

