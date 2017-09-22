package com.bigemap.osmdroiddemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.db.TrackDao;
import com.bigemap.osmdroiddemo.entity.Track;

public class TrackEditActivity extends AppCompatActivity {

    private TrackDao trackDao;
    private Track track;
    private EditText trackNameEt, trackDescriptionEt;
    private TextView saveTrackTv;
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
    protected final <T extends View> T $(int id) {
        return (T) findViewById(id);
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
                    Toast.makeText(TrackEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                }
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