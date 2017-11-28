package com.bigemap.osmdroiddemo.kml;

import android.content.Context;
import android.location.Location;
import android.widget.EditText;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.BaseGraph;

import org.osmdroid.util.GeoPoint;

import java.util.List;

/**
 * 导出kml
 * Created by Think on 2017/9/26.
 */

public class WriteKml {
    //kml中coordinates为GPS经纬度坐标
    private static final String TAG = "WriteKml";
    private Context mContext;
    public WriteKml(Context context){
        this.mContext=context;
    }

    public void createKml(String kmlName, List<BaseGraph> alterSamples)throws Exception{
        Kml kml=new Kml();
        kml.Document=new Document();
        kml.Document.id="root_doc";
        kml.Document.Folder.name=kmlName;
        for (BaseGraph graph: alterSamples){
            Placemark p=new Placemark();
            p.name=graph.getName();
            switch (graph.getType()){
                case Constant.POLYGON:
                    p.Polygon = new Polygon();
                    p.Polygon.outerBoundaryIs.LinearRing.coordinates=getCoordinates(graph.getGeoPoints());
                    break;
                case Constant.POLYLINE:
                    p.LineString=new LineString();
                    p.LineString.coordinates=getCoordinates(graph.getGeoPoints());
                    break;
                case Constant.POI:
                    p.Point=new Point();
                    p.Point.coordinates=getCoordinates(graph.getGeoPoints());
                    break;
                default:
                    break;
            }
            kml.Document.Folder.placemarks.add(p);
        }
        KMLXStream x = new KMLXStream();
        String docKmlPath = Constant.IMPORT_KML_PATH + "/" + kmlName + ".kml";
        x.toKMLFile(kml, docKmlPath, true, false);
        //开始对文件进行压缩，一个kmz文件其实是一个压缩文件，里面包含一个kml文件和一个png图标
        Toast.makeText(mContext, "保存kml成功", Toast.LENGTH_SHORT).show();
    }
    /*
    * 传入三个参数，一是kml的名称，第二个是坐标点的list，第三个是导出轨迹类型
    * */
    public void createKml(String kmlName, List<GeoPoint> alterSamples, String type) throws Exception {
        Kml kml = new Kml();
        kml.Document = new Document();
        kml.Document.open = "1";
        kml.Document.id="root_doc";
        kml.Document.Folder.name="export_area";

        Placemark p = new Placemark();
        p.name=kmlName;
        Style s = new Style();
        s.polyStyle.color = "ff0000ff";
        s.polyStyle.outline = "0";
        s.lineStyle.width = "0";
        p.styles.add(s);
        if (type.equals("line")){
            p.LineString=new LineString();
            p.LineString.coordinates=getCoordinates(alterSamples);
        }else if (type.equals("polygon")){
            p.Polygon = new Polygon();
            p.Polygon.extrude = "1";
            p.Polygon.altitudeMode = "absolute";
            p.Polygon.outerBoundaryIs.LinearRing.coordinates=getCoordinates(alterSamples);
        }
        kml.Document.Folder.placemarks.add(p);
        KMLXStream x = new KMLXStream();
        String docKmlPath = Constant.IMPORT_KML_PATH + "/" + kmlName + ".kml";
        x.toKMLFile(kml, docKmlPath, true, false);
        //开始对文件进行压缩，一个kmz文件其实是一个压缩文件，里面包含一个kml文件和一个png图标
        Toast.makeText(mContext, "保存kml成功", Toast.LENGTH_SHORT).show();
    }

    private String getCoordinates(List<GeoPoint> coordinates) {
        StringBuilder buffer = new StringBuilder();
        for (GeoPoint coordinate : coordinates) {
            buffer.append(coordinate.getLongitude());
            buffer.append(",");
            buffer.append(coordinate.getLatitude());
            buffer.append(" ");
        }
        return buffer.toString().trim();
    }
}
