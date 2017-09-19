package com.bigemap.osmdroiddemo.utils;

import android.content.Context;
import android.content.Intent;

import com.bigemap.osmdroiddemo.activity.TrackActivity;

/**
 * 界面工具类
 * Created by Think on 2017/9/14.
 */

public class UIUtils {
    public static void showTrackActivity(Context context){
        Intent intent=new Intent(context.getApplicationContext(), TrackActivity.class);
        context.startActivity(intent);
    }
}
