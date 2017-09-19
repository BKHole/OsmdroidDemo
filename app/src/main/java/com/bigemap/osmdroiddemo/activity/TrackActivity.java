package com.bigemap.osmdroiddemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.adapter.TrackRecyclerAdapter;
import com.bigemap.osmdroiddemo.db.TrackDao;
import com.bigemap.osmdroiddemo.entity.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "TrackActivity";
    private RecyclerView trackRecycler;
    private TrackRecyclerAdapter trackAdapter;
    private TrackDao trackDao;
    private List<Track> tracks;
    private long trackID;

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
        clearAll.setOnClickListener(this);
    }
    private void initData() {
        trackAdapter = new TrackRecyclerAdapter(tracks, this);
        trackRecycler = $(R.id.track_recycler_view);
        trackRecycler.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        trackRecycler.setHasFixedSize(true);
        trackRecycler.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
        trackRecycler.setAdapter(trackAdapter);
        trackAdapter.setOnItemClickListener(new TrackRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                trackID=tracks.get(position).getTrackid();
                trackDao.getTrackPoints(trackID);//如果选中该条轨迹，回到主界面展示该轨迹，将此处数据传回地图界面
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
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
                Toast.makeText(TrackActivity.this, "clear all success", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
