package com.bigemap.osmdroiddemo.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.bigemap.osmdroiddemo.application.MainApplication;

/**
 * 字体图标
 * Created by Think on 2017/10/27.
 */

public class IconView extends AppCompatTextView {
    public IconView(Context context) {
        super(context);
        init(context);
    }

    public IconView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        //设置字体图标
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/icon.ttf");
        this.setTypeface(font);
    }

}
