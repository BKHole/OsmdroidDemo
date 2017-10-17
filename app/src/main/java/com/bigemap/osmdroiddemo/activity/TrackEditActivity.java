package com.bigemap.osmdroiddemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.db.TrackDao;
import com.bigemap.osmdroiddemo.entity.Track;

public class TrackEditActivity extends BaseActivity {

    private TrackDao trackDao;
    private Track track;
    private EditText trackNameEt, trackDescriptionEt;
    private TextView saveTrackTv, viewTrackTv;
    private long trackId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("编辑轨迹");
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_track_edit);
        initData();
        initView();
        initEvent();
    }

    private void initData() {
        trackDao=new TrackDao(this);
        trackId = getIntent().getLongExtra("trackId", 0);
        track = trackDao.getTrack(trackId);
    }

    private void initView() {
        trackNameEt=$(R.id.et_track_edit_name);
        trackDescriptionEt=$(R.id.et_track_edit_description);
        saveTrackTv=$(R.id.tv_track_edit_save);
        viewTrackTv=$(R.id.tv_track_edit_view);
        trackNameEt.setText(track.getName());
        if (!TextUtils.isEmpty(track.getDescription())){
            trackDescriptionEt.setText(track.getDescription());
        }
    }
    private void initEvent(){
        saveTrackTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=trackNameEt.getText().toString();
                String description=trackDescriptionEt.getText().toString();
                if (!TextUtils.isEmpty(name)){
                    trackDao.updateTrack(trackId, name, description);
                    Intent intent=new Intent(TrackEditActivity.this, TrackRecordActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        viewTrackTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TrackEditActivity.this, MainActivity.class);
                intent.putExtra("trackId", trackId);
                startActivity(intent);
                finish();
            }
        });
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
}
