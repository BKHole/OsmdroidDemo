package com.bigemap.osmdroiddemo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.adapter.FileRecyclerAdapter;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.utils.FileUtils;
import com.bigemap.osmdroiddemo.utils.binding.Bind;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MyTrackActivity extends BaseActivity {
    private static final String TAG = "MyTrackActivity";
    private List<String> paths = null;   //paths：文件路径
    private List<String> sizes = null;   //sizes：文件大小
    private FileRecyclerAdapter adapter;
    @Bind(R.id.tv_file_manager_path)
    private TextView path_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("我的轨迹");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_my_track);
        paths = new ArrayList<>();
        sizes = new ArrayList<>();
        initView();
        initEvent();
    }

    private void initView() {
        RecyclerView showFileRecycler = (RecyclerView) findViewById(R.id.list_file_manager);
        showFileRecycler.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        showFileRecycler.setHasFixedSize(true);
        showFileRecycler.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));

        adapter = new FileRecyclerAdapter(this);
        showFileRecycler.setAdapter(adapter);
        File file = new File(Constant.IMPORT_KML_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        String ini_path = Constant.IMPORT_KML_PATH + "/";
        getFileDir(ini_path);
    }

    private void initEvent() {
        adapter.setOnViewClickListener(new OnViewClickListener() {
            @Override
            public void onClick(View v, Object data) {
                File file = (File) data;
                showDialog(file);
            }
        });
    }

    private void showDialog(final File file) {
        String[] items = new String[]{"查看", "删除"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent i = new Intent();
                        i.putExtra("filePath", file.getPath());
                        setResult(33, i);
                        MyTrackActivity.this.finish();
                        break;
                    case 1:
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * 取得文件结构
     *
     * @param filePath
     */
    private void getFileDir(String filePath) {
    /* 设置目前所在路径 */
        path_edit.setText(filePath);
        paths.clear();
        sizes.clear();
        File f = new File(filePath);
        File[] files = f.listFiles();
        if (files != null) {
            /* 将所有文件添加ArrayList中 */
            for (File file : files) {
                if (file.isDirectory()) {
                    paths.add(file.getPath());
                    sizes.add("");
                }
                if (file.isFile() && file.getName().endsWith("kml")) {
                    //如果是文件,也只添加后缀名为KML的文件
                    paths.add(file.getPath());
                    sizes.add(FileUtils.fileSizeMsg(file));
                }
            }
            adapter.setFilePaths(paths);
            adapter.setFileSizes(sizes);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

}
