package com.bigemap.osmdroiddemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.bigemap.osmdroiddemo.MainApplication;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected final <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainApplication.getRefWatcher(this).watch(this);
    }
}
