package com.bigemap.osmdroiddemo.kml;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.MainApplication;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.Coordinate;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 导出kml
 * Created by Think on 2017/9/26.
 */

public class WriteKml {
    private static final String TAG = "WriteKml";

    /*
    * 传入三个参数，一是kml的名称，第二个是坐标点的list，第三个是导出轨迹类型
    * */
    public void createKml(String kmlName, List<Coordinate> alterSamples, String type) throws Exception {
        Element root = DocumentHelper.createElement("kml");  //根节点是kml
        //给根节点kml添加属性
        root.addAttribute("xmlns", "http://www.opengis.net/kml/2.2");
        Document document = DocumentHelper.createDocument(root);
        //给根节点kml添加子节点  Document
        Element documentElement = root.addElement("Document");
        documentElement.addAttribute("id", "root_doc");

        Element folderElement = documentElement.addElement("Folder");//Folder节点
        //给Folder节点添加子节点
        folderElement.addElement("name").addText(kmlName);
        Element placeMarkElement = folderElement.addElement("Placemark");
        //给Placemark节点添加子节点
        placeMarkElement.addElement("name").addText(type);
        Element styleElement = placeMarkElement.addElement("Style");
        //给Style节点添加子节点
        Element lineStyle = styleElement.addElement("LineStyle");
        lineStyle.addElement("color").addText("ff0000ff");
        Element polyStyle = styleElement.addElement("PolyStyle");
        polyStyle.addElement("fill").addText("0");

        if (type.equals("line")){
            Element lineStringElement = placeMarkElement.addElement("LineString");
            lineStringElement.addElement("coordinates").addText(getCoordinates(alterSamples));
        }else if (type.equals("polygon")){
            Element polygonElement=placeMarkElement.addElement("Polygon");
            Element outerBoundaryIsElement=polygonElement.addElement("outerBoundaryIs");
            Element linearRingElement=outerBoundaryIsElement.addElement("LinearRing");
            linearRingElement.addElement("coodinates").addText(getCoordinates(alterSamples));
        }


        //将生成的kml写出本地
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("utf-8");//设置编码格式
        //将doc.kml写入到
        String docKmlPath = Constant.EXPORT_KML_PATH + "/" + kmlName + ".kml";
        FileOutputStream outputStream = new FileOutputStream(new File(docKmlPath));
        XMLWriter xmlWriter = new XMLWriter(outputStream, format);
        xmlWriter.write(document);

        xmlWriter.close();
        //开始对文件进行压缩，一个kml文件其实是一个压缩文件，里面包含一个kml文件和一个png图标
//        zipWriteKml(docKmlPath, kmlName);
        Toast.makeText(MainApplication.getAppContext(), "导出kml成功", Toast.LENGTH_SHORT).show();
    }

    /*
    * 将生成的kml文件和drawable下的某个png图标进行压缩，生成最终的kml文件，并保存在/data/data/<package name>/files目录
    * */
    public void zipWriteKml(String docKmlPath, String kmlName) throws IOException {
        // 最终生成的kml文件
        FileOutputStream fileOutput = MainApplication.getAppContext().openFileOutput(kmlName + ".kml", Context.MODE_PRIVATE);

        OutputStream os = new BufferedOutputStream(fileOutput);
        ZipOutputStream zos = new ZipOutputStream(os);
        byte[] buf = new byte[8192];
        int len;

        File file = new File(docKmlPath);
        if (!file.isFile())
            Log.d(TAG, "doc.kml is nonexist");
        ZipEntry ze = new ZipEntry(file.getName());
        zos.putNextEntry(ze);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        while ((len = bis.read(buf)) > 0) {
            zos.write(buf, 0, len);

        }
        zos.closeEntry();

        // 压缩drawable目录下的图片
//        Resources r = MainApplication.getAppContext().getResources();
//        Bitmap bitmap = BitmapFactory.decodeResource(r, R.drawable.layer0_symbol);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        InputStream input = new ByteArrayInputStream(baos.toByteArray());
        int temp = 0;
        ZipEntry entry2 = new ZipEntry("layer0_symbol.png");
        zos.putNextEntry(entry2);
        while ((temp = input.read()) != -1) {
            zos.write(temp);
        }
        input.close();
        zos.closeEntry();
        zos.close();

    }

    private String getCoordinates(List<Coordinate> coordinates) {
        StringBuilder buffer = new StringBuilder();
        for (Coordinate coordinate : coordinates) {
            buffer.append(coordinate.getX());
            buffer.append(",");
            buffer.append(coordinate.getY());
            buffer.append(" ");
        }
        return buffer.toString().trim();
    }
}
