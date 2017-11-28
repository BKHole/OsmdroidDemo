package com.bigemap.osmdroiddemo.kml;

import android.util.Log;
import android.util.Xml;

import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.BaseGraph;

import org.json.JSONException;
import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 解析kml文件，导入
 * Created by Think on 2017/9/25.
 */

public class ReadKml {

    //kml中coordinates为GPS经纬度坐标
    private static final String TAG = "ReadKml";
    //解析XML
    private ArrayList<BaseGraph> baseGraphs;
    private ArrayList<GeoPoint> geoPoints;

    private String t_type;
    private BaseGraph baseGraph = null;
    private int id;

    public ReadKml() {
        baseGraphs = new ArrayList<>();
        geoPoints = new ArrayList<>();
    }

    public void parseKml(String pathName) throws Exception {
        File file = new File(pathName);//pathName为KML文件的路径
        InputStream inputStream = new FileInputStream(file);
        pullXml(inputStream);
        inputStream.close();
    }

    public ArrayList<BaseGraph> getBaseGraphs() {
        return this.baseGraphs;
    }

    public ArrayList<GeoPoint> getGeoPoints() {
        return this.geoPoints;
    }

    //解析XML
    private void pullXml(InputStream inputStream) throws IOException, XmlPullParserException, JSONException {

        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "utf-8");
        int type = parser.getEventType();
        while (type != XmlPullParser.END_DOCUMENT) {
            String nodeName = parser.getName();
            switch (type) {
                case XmlPullParser.START_DOCUMENT:
                    Log.d(TAG, "pullXml: startRead");
                    break;
                case XmlPullParser.START_TAG:
                    if ("Placemark".equals(nodeName)) {
                        baseGraph = new BaseGraph();
                        baseGraph.setId(id);
                    } else if (baseGraph != null) {
                        if (nodeName.equals("name")) {
                            baseGraph.setName(parser.nextText());
                        } else if ("Point".equals(nodeName)) {
                            t_type = Constant.POI;
                            baseGraph.setType(t_type);
                        } else if ("LinearRing".equals(nodeName)) {
                            t_type = Constant.POLYGON;
                            baseGraph.setType(t_type);
                        } else if ("LineString".equals(nodeName)) {
                            t_type = Constant.POLYLINE;
                            baseGraph.setType(t_type);
                        } else if ("coordinates".equals(nodeName)) {
                            String s = parser.nextText();
                            switch (t_type) {
                                case Constant.POI:
                                    baseGraph.getGeoPoints().add(stringToPoint(s));
                                    geoPoints.add(stringToPoint(s));
                                    break;
                                case Constant.POLYGON:
                                    baseGraph.getGeoPoints().addAll(stringToPoints(s));
                                    geoPoints.addAll(stringToPoints(s));
                                    break;
                                case Constant.POLYLINE:
                                    baseGraph.getGeoPoints().addAll(stringToPoints(s));
                                    geoPoints.addAll(stringToPoints(s));
                                    break;
                            }
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("Placemark".equals(nodeName)) {
                        baseGraphs.add(baseGraph);
                        id++;
                        baseGraph = null;
                    }
                    break;
                default:
                    break;
            }
            type = parser.next();
        }
    }

    private GeoPoint stringToPoint(String data) {
        GeoPoint geoPoint = null;
        data = data.trim();
        String[] split = data.split(",");
        if (split.length > 1) {
            geoPoint = new GeoPoint(Double.valueOf(split[1].trim()), Double.valueOf(split[0].trim()));
        }
        return geoPoint;
    }

    private ArrayList<GeoPoint> stringToPoints(String s) {
        ArrayList<GeoPoint> points = new ArrayList<>();
        String[] s_array = s.split(" ");
        for (String temp : s_array) {
            GeoPoint geoPoint = stringToPoint(temp);
            points.add(geoPoint);
        }
        return points;
    }

}
