package com.bigemap.osmdroiddemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.bigemap.osmdroiddemo.application.MainApplication;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.utils.DataKeeper;
import com.bigemap.osmdroiddemo.utils.ToastUtils;
import com.bigemap.osmdroiddemo.utils.binding.ViewBinder;

public class BaseActivity extends AppCompatActivity {
    public ToastUtils toastUtils;
    public DataKeeper dataKeeper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toastUtils = new ToastUtils(this);
        dataKeeper=new DataKeeper(this, Constant.PREFS_NAME);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        ViewBinder.bind(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ViewBinder.bind(this);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        ViewBinder.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApplication.getRefWatcher(this).watch(this);
    }

}
