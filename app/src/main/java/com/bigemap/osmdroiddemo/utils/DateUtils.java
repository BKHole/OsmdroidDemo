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
