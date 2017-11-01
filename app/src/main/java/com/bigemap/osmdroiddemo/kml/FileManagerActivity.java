package com.bigemap.osmdroiddemo.kml;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.activity.BaseActivity;
import com.bigemap.osmdroiddemo.adapter.FileRecyclerAdapter;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.Coordinate;
import com.bigemap.osmdroiddemo.entity.Location;
import com.bigemap.osmdroiddemo.entity.Track;
import com.bigemap.osmdroiddemo.utils.DateUtils;
import com.bigemap.osmdroiddemo.utils.FileUtils;
import com.bigemap.osmdroiddemo.utils.UIUtils;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerActivity extends BaseActivity {

    private static final String TAG = "FileManagerActivity";
    private List<String> items = null;   //items：存放显示的名称
    private List<String> paths = null;   //paths：存放文件路径
    private List<String> sizes = null;   //sizes：文件大小
    private String rootPath = Environment.getExternalStorageDirectory().getPath();//rootPath：起始文件夹
    private TextView path_edit;
    private RecyclerView showFileRecycler;
    private FileRecyclerAdapter adapter;
    private ReadKml readKml;
    private ArrayList<Coordinate> coordinates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("导入KML文件");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_file_manager);
        items = new ArrayList<>();
        paths = new ArrayList<>();
        sizes = new ArrayList<>();
        readKml = new ReadKml();
        initView();
        initEvent();
    }

    private void initView() {
        path_edit = (TextView) findViewById(R.id.tv_file_manager_path);
        showFileRecycler = (RecyclerView) findViewById(R.id.list_file_manager);
        adapter = new FileRecyclerAdapter(this);
        showFileRecycler.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        showFileRecycler.setHasFixedSize(true);
        showFileRecycler.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
        File file=new File(Constant.IMPORT_KML_PATH);
        if (!file.exists()){
            file.mkdirs();
        }
        String ini_path = Constant.IMPORT_KML_PATH + "/";
        getFileDir(ini_path);
        showFileRecycler.setAdapter(adapter);
    }

    private void initEvent() {
        adapter.setOnViewClickListener(new OnViewClickListener() {
            @Override
            public void onClick(View v, Object data) {
                File file = (File) data;
                if (file.isDirectory()) {
                    getFileDir(file.getPath());
                } else {
                    try {
                        readKml.parseKml(file.getPath());
                        coordinates=readKml.getCoordinateList();
                        ArrayList<GeoPoint> geoPoints=new ArrayList<GeoPoint>();
                        for (Coordinate coordinate: coordinates){
                            geoPoints.add(new GeoPoint(coordinate.getLatitude(), coordinate.getLongitude()));
                        }
                        Track track=new Track();
                        String time= DateUtils.formatUTC(System.currentTimeMillis(), null);
                        track.setName(file.getName());
                        track.setStartTime(time);
                        track.setTrackSource(2);
                        track.setTrackType(3);
                        if (coordinates.size()>0){
                            for (GeoPoint geoPoint: geoPoints){
                                Location location=new Location();
                                location.setLatitude(String.valueOf(geoPoint.getLatitude()));
                                location.setLongitude(String.valueOf(geoPoint.getLongitude()));
                                location.save();
                                track.getLocations().add(location);
                            }
                            track.save();
                            UIUtils.showTrackRecordActivity(FileManagerActivity.this);
                            finish();
                        }else{
                            toastUtils.showToast("导入失败");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    /**
     * 取得文件结构的方法
     *
     * @param filePath
     */
    private void getFileDir(String filePath) {
    /* 设置目前所在路径 */
        path_edit.setText(filePath);
        items.clear();
        paths.clear();
        sizes.clear();
        File f = new File(filePath);
        File[] files = f.listFiles();
        if (files != null) {
            /* 将所有文件添加ArrayList中 */
            for (File file: files) {
                if (file.isDirectory()) {
                    items.add(file.getName());
                    paths.add(file.getPath());
                    sizes.add("");
                }
                if (file.isFile() && file.getName().endsWith("kml")) {
                    //如果是文件,也只添加后缀名为KML的文件
                    items.add(file.getName());
                    paths.add(file.getPath());
                    sizes.add(FileUtils.fileSizeMsg(file));
                }
            }
            adapter.setFileItems(items);
            adapter.setFilePaths(paths);
            adapter.setFileSizes(sizes);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                File file = new File(path_edit.getText().toString());
                if (rootPath.equals(path_edit.getText().toString())) {
                    UIUtils.showTrackRecordActivity(this);
                    finish();
                } else {
                    getFileDir(file.getParent());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 重写返回键功能:返回上一级文件夹
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 是否触发按键为back键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            File file = new File(path_edit.getText().toString());
            if (rootPath.equals(path_edit.getText().toString())) {
                return super.onKeyDown(keyCode, event);
            } else {
                getFileDir(file.getParent());
                return true;
            }
            // 如果不是back键正常响应
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
