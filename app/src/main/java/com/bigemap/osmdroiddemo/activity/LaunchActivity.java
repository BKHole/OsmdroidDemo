package com.bigemap.osmdroiddemo.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.service.MyLocationService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LaunchActivity extends BaseActivity {

    final private int REQUEST_CODE_ASK_WRITING_PERMISSIONS = 126;
    private ImageView launchImage;
    private AnimationSet animationSet;
    private MyCountDownTimer timer;
    private TextView timerTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        launchImage = $(R.id.iv_start_title);
        timerTv=$(R.id.tv_time);
        initAnimation();
        initEvent();
    }

    /**
     * 初始化动画
     */
    private void initAnimation() {
        animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1f);
        alphaAnimation.setDuration(3000);
        alphaAnimation.setFillAfter(true);
        animationSet.addAnimation(alphaAnimation);
        launchImage.setAnimation(animationSet);
        timer=new MyCountDownTimer(3000, 1000);
        timer.start();
    }

    private void initEvent() {
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (Build.VERSION.SDK_INT >= 23){
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                        requestPermissions(
                                new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                REQUEST_CODE_ASK_WRITING_PERMISSIONS);
                    }else{
                        initFile();
                        loadHomePage();
                    }
                }else{
                    initFile();
                    loadHomePage();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    /**
     * 初始化文件目录
     */
    private void initFile(){
        new Thread(){
            @Override
            public void run() {
                File file = new File(Constant.IMPORT_KML_PATH);
                if (!file.exists()){
                    file.mkdirs();
                }
                file = new File(Constant.EXPORT_KML_PATH);
                if (!file.exists()){
                    file.mkdirs();
                }
            }
        }.start();

    }
    /**
     * 跳转主页面
     */
    private void loadHomePage() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_ASK_WRITING_PERMISSIONS:
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initFile();
                    loadHomePage();
                }else {
                    // Permission Denied
                    showMissingPermissionDialog();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }

    }

    private class MyCountDownTimer extends CountDownTimer{

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            timerTv.setText((l / 1000) + "s 跳过");
        }

        @Override
        public void onFinish() {
            timerTv.setText("0s 跳过");
        }
    }

    @Override
    protected void onDestroy() {
        if ( timer!= null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    // 显示缺失权限提示
    private void showMissingPermissionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();

        builder.setMessage("当前应用缺少必要权限。\n\n请点击\"设置\"-\"权限\"-打开所需权限。\n\n最后点击两次后退按钮，即可返回。");
        // 拒绝, 退出应用
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });

        builder.show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse( "package:" + getPackageName()));
        startActivity(intent);
    }
}
