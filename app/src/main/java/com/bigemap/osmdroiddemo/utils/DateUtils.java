package com.bigemap.osmdroiddemo.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 日期格式工具类
 * Created by Think on 2017/9/11.
 */

public class DateUtils {
    /**
     *  开始定位
     */
    public final static int MSG_LOCATION_START = 0;
    /**
     * 定位完成
     */
    public final static int MSG_LOCATION_FINISH = 1;
    /**
     * 停止定位
     */
    public final static int MSG_LOCATION_STOP= 2;

    public final static String KEY_URL = "URL";
    public final static String URL_H5LOCATION = "file:///android_asset/location.html";

    private static SimpleDateFormat GMTTimeFormatter = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss'Z'");
    {
        GMTTimeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static SimpleDateFormat sdf = null;
    public  static String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }

    public static long getGMTTime(String strGMTTime) {
        long millisecond = 0;
        try {
            millisecond = GMTTimeFormatter.parse(strGMTTime).getTime();
        } catch (ParseException e) {
            if (strGMTTime.length() > 0)
                e.printStackTrace();
            else
                System.err.println(e.getMessage());
        }
        return millisecond;
    }
}
