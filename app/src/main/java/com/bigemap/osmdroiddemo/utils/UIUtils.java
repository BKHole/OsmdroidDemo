package com.bigemap.osmdroiddemo.utils;

import android.content.Context;
import android.content.Intent;

import com.bigemap.osmdroiddemo.activity.TrackActivity;
import com.bigemap.osmdroiddemo.activity.TrackEditActivity;

/**
 * 界面工具类
 * Created by Think on 2017/9/14.
 */

public class UIUtils {
    public static void showTrackActivity(Context context){
        Intent intent=new Intent(context.getApplicationContext(), TrackActivity.class);
        context.startActivity(intent);
    }

    public static void showTrackEditActivity(Context context, long trackId){
        Intent intent=new Intent(context.getApplicationContext(), TrackEditActivity.class);
        intent.putExtra("trackId", trackId);
        context.startActivity(intent);
    }
}
