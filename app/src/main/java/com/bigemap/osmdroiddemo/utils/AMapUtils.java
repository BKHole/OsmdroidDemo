package com.bigemap.osmdroiddemo.utils;

import android.widget.EditText;

/**
 * 高德搜索功能工具类
 * Created by Think on 2017/9/29.
 */

public class AMapUtils {

    /**
     * 判断edittext是否null
     */
    public static String checkEditText(EditText editText) {
        if (editText != null && editText.getText() != null
                && !(editText.getText().toString().trim().equals(""))) {
            return editText.getText().toString().trim();
        } else {
            return "";
        }
    }

    public static boolean IsEmptyOrNullString(String s) {
        return (s == null) || (s.trim().length() == 0);
    }
}
