package com.bigemap.osmdroiddemo.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.adapter.TrackRecyclerAdapter;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.Coordinate;
import com.bigemap.osmdroiddemo.entity.Location;
import com.bigemap.osmdroiddemo.entity.Track;
import com.bigemap.osmdroiddemo.kml.WriteKml;
import com.bigemap.osmdroiddemo.utils.UIUtils;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;
import com.bigemap.osmdroiddemo.viewholder.OnViewLongClickListener;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrackRecordActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "TrackActivity";
    private RecyclerView trackRecycler;
    private TrackRecyclerAdapter trackAdapter;
    private List<Track> tracks;
    private long trackID;
    private TextView noDataTv;
    private WriteKml writeKml=new WriteKml();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("轨迹记录");
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_track);
//        if (Build.VERSION.SDK_INT >= 21) {
//            View decorView = getWindow().getDecorView();
//            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            decorView.setSystemUiVisibility(option);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }

        initView();
        initData();
        initEvent();
    }

    private void initView(){
        TextView clearAll=$(R.id.clear_all);
        noDataTv = $(R.id.tv_no_data);
        clearAll.setOnClickListener(this);
    }

    private void initData() {
        File file=new File(Constant.EXPORT_KML_PATH);
        if (!file.exists()){
            file.mkdirs();
        }
        trackAdapter = new TrackRecyclerAdapter(this);
        trackRecycler = $(R.id.list_track);
        trackRecycler.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        trackRecycler.setHasFixedSize(true);
        trackRecycler.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
        trackRecycler.setAdapter(trackAdapter);
    }

    private void initEvent(){
        trackAdapter.setOnViewClickListener(new OnViewClickListener() {
            @Override
            public void onClick(View v, Object data) {
                Track track= (Track) data;
                trackID=track.getId();
                UIUtils.showTrackEditActivity(TrackRecordActivity.this, trackID);
                Log.d(TAG, "onItemClick: trackID="+trackID);
            }
        });
        trackAdapter.setOnViewLongClickListener(new OnViewLongClickListener() {
            @Override
            public void onLongClick(View v, Object data) {
                Track track= (Track) data;
                trackID=track.getId();
                popUpDialog(trackID);
                Log.d(TAG, "onLongClick: test");
            }
        });
    }

    /**
     * 长按操作
     */
    private void popUpDialog(final long trackID) {
        // 创建数据
        final String[] items = new String[] { "导出kml", "删除"};
        // 创建对话框构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置参数
        builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String type="";
                        switch (which){
                            case 0:
                                try {
                                    List<Coordinate> coordinates=new ArrayList<>();
                                    Track track=DataSupport.find(Track.class,trackID);
                                    Log.d(TAG, "onClick: trackType="+track.getTrackType());
                                    switch (track.getTrackType()){
                                        case 0://line
                                            type="line";
                                            break;
                                        case 1://polygon
                                            type="polygon";
                                    }
                                    List<Location> points=DataSupport.findAll(Location.class,trackID);
                                    for (Location location: points){
                                        Coordinate coordinate=new Coordinate(Double.valueOf(location.getLongitude())
                                                ,Double.valueOf(location.getLatitude()), track.getName());
                                        coordinates.add(coordinate);
                                    }
                                    writeKml.createKml(type+"_"+track.getId(),coordinates,type);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                DataSupport.delete(Track.class,trackID);
                                tracks = DataSupport.findAll(Track.class);
                                trackAdapter.setDataList(tracks);
                                isDataNull();
                                break;
                        }
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracks = DataSupport.findAll(Track.class);
        trackAdapter.setDataList(tracks);
        isDataNull();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_track, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_add_more:
                UIUtils.showFileManagerActivity(this);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clear_all:
                DataSupport.deleteAll(Track.class);
                trackAdapter.clearAllData();
                noDataTv.setVisibility(View.VISIBLE);
                toastUtils.showSingletonToast("清除成功");
                break;
        }
    }

    /**
     * 判断数据列表是否为空
     */
    private void isDataNull() {
        if (tracks.size()==0){
            noDataTv.setVisibility(View.VISIBLE);
        }else{
            noDataTv.setVisibility(View.GONE);
        }
    }
}
