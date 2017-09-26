package com.bigemap.osmdroiddemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.adapter.TrackRecyclerAdapter;
import com.bigemap.osmdroiddemo.db.TrackDao;
import com.bigemap.osmdroiddemo.entity.Track;
import com.bigemap.osmdroiddemo.utils.UIUtils;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import java.util.List;

public class TrackActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "TrackActivity";
    private RecyclerView trackRecycler;
    private TrackRecyclerAdapter trackAdapter;
    private TrackDao trackDao;
    private List<Track> tracks;
    private long trackID;
    private TextView noDataTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("轨迹记录");
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_track);
        trackDao = new TrackDao(this);
        tracks = trackDao.getAllTracks();

        initView();
        initData();
    }

    private void initView(){
        TextView clearAll=$(R.id.clear_all);
        noDataTv = $(R.id.tv_no_data);
        if (tracks.size()==0){
            noDataTv.setVisibility(View.VISIBLE);
        }
        clearAll.setOnClickListener(this);
    }
    private void initData() {
        trackAdapter = new TrackRecyclerAdapter(this);
        trackAdapter.setDataList(tracks);
        trackRecycler = $(R.id.track_recycler_view);
        trackRecycler.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        trackRecycler.setHasFixedSize(true);
        trackRecycler.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
        trackRecycler.setAdapter(trackAdapter);
        trackAdapter.setOnViewClickListener(new OnViewClickListener() {
            @Override
            public void onClick(View v, Object data) {
                Track track= (Track) data;
                trackID=track.getTrackid();
                UIUtils.showTrackEditActivity(TrackActivity.this, trackID);
//                trackDao.getTrackPoints(trackID);//如果选中该条轨迹，回到主界面展示该轨迹，将此处数据传回地图界面
                Log.d(TAG, "onItemClick: trackID="+trackID);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    protected final <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clear_all:
                trackDao.clearAll();
                trackAdapter.clearAllData();
                noDataTv.setVisibility(View.VISIBLE);
                Toast.makeText(TrackActivity.this, "clear all success", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
