package com.bigemap.osmdroiddemo.kml;

import android.util.Log;
import android.util.Xml;

import com.bigemap.osmdroiddemo.entity.Coordinate;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 解析kml文件，导入
 * Created by Think on 2017/9/25.
 */

public class ReadKml {
    private static final String TAG = "ReadKml";
    //解析XML
    private Coordinate coordinate = null; //存储从KML文件中读取出来的坐标值和name
    private ArrayList<Coordinate> coordinateList = new ArrayList();//存储每次实例化的Coordinate对象，每个Coordinate都保存着不同的x,y,name
    public String t_type;

    public void parseKml(String pathName) throws Exception {
        File file = new File(pathName);//pathName为KML文件的路径
        InputStream inputStream = new FileInputStream(file);
//        parseXmlWithDom4j(inputStream);
        pullXml(inputStream);
        inputStream.close();
    }

    private Boolean parseXmlWithDom4j(InputStream input) throws Exception {
        boolean addSampleSuccess = false;
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(input);
            Element root = document.getRootElement();//获取kml文件的根结点
            Log.d(TAG, "parseXmlWithDom4j: root=" + root.getName());
            listNodes(root);
            addSampleSuccess = true;
            //选择sd卡中的kml文件，解析成功后即调用MainActivity中的添加marker的方法向地图上添加样点marker
        } catch (DocumentException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return addSampleSuccess;
    }

    //    遍历当前节点下的所有节点
    private void listNodes(Element node) {

        Log.d(TAG, "listNodes: nodeName" + node.getName());
        String name = "";//Placemark节点中的name属性
        String x = "";//坐标x
        String y = "";//坐标y
        double d_x = 0.0;//对x作string to double
        double d_y = 0.0;
        try {
            if ("Placemark".equals(node.getName())) {//如果当前节点是Placemark就解析其子节点
                List<Element> placemarkSons = node.elements();//得到Placemark节点所有的子节点
                for (Element element : placemarkSons) { //遍历所有的子节点
                    if ("name".equals(element.getName())) {
                        name = element.getText();
                    }
                }
                Element pointSon;//LineString 节点的子节点
                Iterator i = node.elementIterator("LineString");//遍历Point节点的所有子节点
                while (i.hasNext()) {
                    pointSon = (Element) i.next();
                    String nodeContent = "";
                    nodeContent = pointSon.elementText("coordinates");//得到coordinates节点的节点内容
                    String[] s_array = nodeContent.split(" ");
                    for (int temp = 0; temp < s_array.length; temp++) {
                        GeoPoint geoPoint = stringToPoint(s_array[temp]);
                        coordinate = new Coordinate(geoPoint.getLatitude(), geoPoint.getLongitude(), t_type);
                        coordinateList.add(coordinate);
                    }
                }
//                coordinate = new Coordinate(d_x, d_y, name);
//                coordinateList.add(coordinate);//将每一个实例化的对象存储在list中
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //同时迭代当前节点下面的所有子节点
        //使用递归
        Iterator<Element> iterator = node.elementIterator();
        while (iterator.hasNext()) {
            Element e = iterator.next();
            listNodes(e);
        }
    }
    public ArrayList<Coordinate> getCoordinateList() {
        return this.coordinateList;
    }

        //解析XML
    public void pullXml(InputStream inputStream) throws IOException, XmlPullParserException, JSONException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "utf-8");
        int type = parser.getEventType();
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG) {
                switch (parser.getName()) {
                    case "Placemark":
                        break;
                    case "Point":
                        t_type = "point";
                        break;
                    case "LinearRing":
                        t_type = "polygon";
                        break;
                    case "LineString":
                        t_type = "line";
                        break;
                    case "coordinates":
                        String s = parser.nextText();
                        switch (t_type) {
                            case "point":
                                stringToPoint(s);
                                Log.d(TAG, "pullXml: ");
                                break;
                            case "polygon":
                            case "line":
                                String[] s_array = s.split(" ");
                                for (String temp: s_array) {
                                    GeoPoint geoPoint = stringToPoint(temp);
                                    coordinate = new Coordinate(geoPoint.getLatitude(), geoPoint.getLongitude(), t_type);
                                    coordinateList.add(coordinate);
                                }
                                break;
                        }
                        break;
                }
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
}
