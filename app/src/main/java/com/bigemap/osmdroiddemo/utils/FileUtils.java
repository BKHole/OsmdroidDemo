package com.bigemap.osmdroiddemo.utils;

import java.io.File;

/**
 * 文件管理工具类
 * Created by Think on 2017/9/25.
 */

public class FileUtils {

    //获得一个文件的类型信息
    public static String getMIMEType(File f){
        String type="";
        String fName=f.getName();
    /* 取得扩展名 */
        String end=fName.substring(fName.lastIndexOf(".")+1,fName.length()).toLowerCase();
        if(end.equals("m4a")||end.equals("mp3")||end.equals("mid")||end.equals("xmf")||end.equals("ogg")||end.equals("wav")){
            type = "audio";
        }else if(end.equals("3gp")||end.equals("mp4")){
            type = "video";
        }else if(end.equals("jpg")||end.equals("gif")||end.equals("png")||end.equals("jpeg")||end.equals("bmp")){
            type = "image";
        }else if(end.equals("apk")){
            type = "apk";
        }else if (end.equals("kml")){
            type="kml";
        }else{
            type="no";
        }
        return type;
    }

    //获得一个文件的大小信息
    public static String  fileSizeMsg(File f){
        int sub_index = 0;
        String  show = "";
        if(f.isFile()){
            long length = f.length();
            if(length>=1073741824){
                sub_index = (String.valueOf((float)length/1073741824)).indexOf(".");
                show = ((float)length/1073741824+"000").substring(0,sub_index+3)+"GB";
            }else if(length>=1048576){
                sub_index = (String.valueOf((float)length/1048576)).indexOf(".");
                show =((float)length/1048576+"000").substring(0,sub_index+3)+"MB";
            }else if(length>=1024){
                sub_index = (String.valueOf((float)length/1024)).indexOf(".");
                show = ((float)length/1024+"000").substring(0,sub_index+3)+"KB";
            }else if(length<1024){
                show = String.valueOf(length)+"B";
            }
        }
        return show;
    }
}
