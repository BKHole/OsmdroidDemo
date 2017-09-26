package com.bigemap.osmdroiddemo.kml;

import android.util.Log;
import android.util.Xml;

import com.bigemap.osmdroiddemo.entity.Coordinate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析kml文件，导入
 * Created by Think on 2017/9/25.
 */

public class ReadKml {
    private static final String TAG = "ReadKml";
    //解析XML
    public static boolean addSampleSuccess = false; //判断读取KML是否成功
    private Coordinate coordinate = null; //存储从KML文件中读取出来的坐标值和name
    private ArrayList<GeoPoint> coordinateList = new ArrayList();//存储每次实例化的Coordinate对象，每个Coordinate都保存着不同的x,y,name
    public String t_type;
    //    public void parseKml(String pathName) throws Exception
//    {
//        File file = new File(pathName);//pathName为KML文件的路径
//        try {
//            ZipFile zipFile = new ZipFile(file);
//            ZipInputStream zipInputStream = null;
//            InputStream inputStream = null;
//            ZipEntry entry = null;
//            zipInputStream = new ZipInputStream(new FileInputStream(file));
//            while ((entry = zipInputStream.getNextEntry()) != null) {
//                String zipEntryName = entry.getName();
//                if (zipEntryName.endsWith("kml") || zipEntryName.endsWith("kmz")) {
//                    inputStream = zipFile.getInputStream(entry);
//                    parseXmlWithDom4j(inputStream);
//                }else if (zipEntryName.endsWith("png")) {
//
//                }
//            }
//            zipInputStream.close();
//            inputStream.close();
//        } catch (ZipException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }

    //    private Boolean parseXmlWithDom4j(InputStream input) throws Exception
//    {
//        SAXReader reader = new SAXReader();
//        Document document = null;
//        try {
//            document = reader.read(input);
//            Element root = document.getRootElement();//获取doc.kml文件的根结点
//            listNodes(root);
//            addSampleSuccess = true;
//            //选择sd卡中的kml文件，解析成功后即调用MainActivity中的添加marker的方法向地图上添加样点marker
//        } catch (DocumentException e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        }
//        return addSampleSuccess;
//    }
    //遍历当前节点下的所有节点
//    public void listNodes(Element node){
//
//        String name = "";//Placemark节点中的name属性
//        String x = "";//坐标x
//        String y = "";//坐标y
//        double d_x = 0.0;//对x作string to double
//        double d_y = 0.0;
//        try {
//            if ("Placemark".equals(node.getName())) {//如果当前节点是Placemark就解析其子节点
//                List<Element> placemarkSons = node.elements();//得到Placemark节点所有的子节点
//                for (Element element : placemarkSons) { //遍历所有的子节点
//                    if ("name".equals(element.getName())) {
//                        name = element.getText();
//                    }
//                }
//                Element pointSon;//Point节点的子节点
//                Iterator i = node.elementIterator("Point");//遍历Point节点的所有子节点
//                while (i.hasNext()) {
//                    pointSon = (Element)i.next();
//                    String nodeContent = "";
//                    nodeContent = pointSon.elementText("coordinates");//得到coordinates节点的节点内容
//                    String nodeContentSplit[] = null;
//                    nodeContentSplit = nodeContent.split(",");
//                    x = nodeContentSplit[1];
//                    y = nodeContentSplit[0];
//                    d_x = Double.valueOf(x.trim());
//                    d_y = Double.valueOf(y.trim());
//                }
//                coordinate = new Coordinate(d_x, d_y , name);
//                coordinateList.add(coordinate);//将每一个实例化的对象存储在list中
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //同时迭代当前节点下面的所有子节点
//        //使用递归
//        Iterator<Element> iterator = node.elementIterator();
//        while(iterator.hasNext()){
//            Element e = iterator.next();
//            listNodes(e);
//        }
//    }
    public ArrayList<GeoPoint> getCoordinateList() {
        return this.coordinateList;
    }

    //解析XML
    public String pullXml(String file) throws IOException, XmlPullParserException, JSONException {
        JSONArray array = new JSONArray();
        InputStream inputStream = new FileInputStream(new File(file));
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(inputStream, "utf-8");
        int type = parser.getEventType();
        while (type != XmlPullParser.END_DOCUMENT) {
            if (type == XmlPullParser.START_TAG) {
                JSONObject j = new JSONObject();
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
                        j.put("type", t_type);
                        String s = parser.nextText();
                        switch (t_type) {
                            case "point":
                                stringToPoint(s);
                                Log.d(TAG, "pullXml: ");
                                break;
                            case "polygon":
                            case "line":
                                String[] s_array = s.split(" ");
                                for (int i = 0; i < s_array.length; i++) {
                                    GeoPoint geoPoint=stringToPoint(s_array[i]);
//                                    coordinate = new Coordinate(geoPoint.getLatitude(), geoPoint.getLongitude(), t_type);
                                    coordinateList.add(geoPoint);
                                }
                                break;
                        }
                        array.put(j);
                        break;
                }
            }
            type = parser.next();
        }
        System.out.println(array.toString());
        return array.toString();
    }

    private GeoPoint stringToPoint(String data) throws JSONException {
        GeoPoint geoPoint = null;
        data = data.trim();
        String[] split = data.split(",");
        if (split.length > 1) {
            geoPoint = new GeoPoint(Double.valueOf(split[1].trim()), Double.valueOf(split[0].trim()));
        }
        return geoPoint;
    }
}
