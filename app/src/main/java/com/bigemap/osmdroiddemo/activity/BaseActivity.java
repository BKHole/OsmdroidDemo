package com.bigemap.osmdroiddemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bigemap.osmdroiddemo.application.MainApplication;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.utils.DataKeeper;
import com.bigemap.osmdroiddemo.utils.ToastUtils;

public class BaseActivity extends AppCompatActivity {

    public ToastUtils toastUtils;
    public DataKeeper dataKeeper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toastUtils = new ToastUtils(this);
        dataKeeper=new DataKeeper(this, Constant.PREFS_NAME);
    }

    protected final <T extends View>T $(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApplication.getRefWatcher(this).watch(this);
    }
}
