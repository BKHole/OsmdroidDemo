package com.bigemap.osmdroiddemo.utils;

import android.content.Context;
import android.content.Intent;

import com.bigemap.osmdroiddemo.activity.MainActivity;
import com.bigemap.osmdroiddemo.activity.TrackRecordActivity;
import com.bigemap.osmdroiddemo.activity.TrackEditActivity;
import com.bigemap.osmdroiddemo.kml.FileManagerActivity;

/**
 * 界面工具类
 * Created by Think on 2017/9/14.
 */

public class UIUtils {

    public static void showTrackRecordActivity(Context context){
        Intent intent=new Intent(context.getApplicationContext(), TrackRecordActivity.class);
        context.startActivity(intent);
    }

    public static void showTrackEditActivity(Context context, long trackId){
        Intent intent=new Intent(context.getApplicationContext(), TrackEditActivity.class);
        intent.putExtra("trackId", trackId);
        context.startActivity(intent);
    }

    public static void showFileManagerActivity(Context context){
        Intent intent=new Intent(context.getApplicationContext(), FileManagerActivity.class);
        context.startActivity(intent);
    }
}
