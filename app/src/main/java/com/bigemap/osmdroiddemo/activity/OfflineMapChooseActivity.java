package com.bigemap.osmdroiddemo.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.adapter.OfflineMapFileAdapter;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.OfflineMap;
import com.bigemap.osmdroiddemo.utils.binding.Bind;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import java.io.File;
import java.util.ArrayList;

public class OfflineMapChooseActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "OfflineMapChooseActivit";

    @Bind(R.id.et_offline_map_name)
    private EditText offlineMapName;
    @Bind(R.id.et_offline_map_description)
    private EditText offlineMapDesc;
    @Bind(R.id.tv_offline_map_path)
    private TextView offlineMapPath;
    @Bind(R.id.tv_offline_satellite_path)
    private TextView offlineSatellitePath;
    @Bind(R.id.tv_offline_map_save)
    private TextView offlineMapSave;

    private ArrayList<String> paths = null;   //paths：文件路径
    //    private File selectFile=null;
    private AlertDialog dialog = null;
    private int selectType; //0:电子，1:卫星

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTitle("编辑轨迹");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.activity_offline_map_choose);
        init();

    }

    /*
    初始化
     */
    private void init() {
        offlineMapPath.setOnClickListener(this);
        offlineSatellitePath.setOnClickListener(this);
        offlineMapSave.setOnClickListener(this);
    }

    /*
    item 单击事件
     */
    private OnViewClickListener onViewClickListener = new OnViewClickListener() {
        @Override
        public void onClick(View v, Object data) {
            File dir = (File) data;
//            getFile(dir.getPath());
            if (selectType == 0) {
                offlineMapName.setText(dir.getName());
                offlineMapPath.setText(dir.getPath());
            } else if (selectType == 1) {
                offlineSatellitePath.setText(dir.getPath());
            }
            dialog.dismiss();
        }
    };

    private void showFileDialog(String filePath) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_view_file_select, null);
        RecyclerView fileRecycler = (RecyclerView) view.findViewById(R.id.list_file_select);
        fileRecycler.setLayoutManager(new LinearLayoutManager(this));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        fileRecycler.setHasFixedSize(true);
        fileRecycler.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));
        TextView offlineTip = (TextView) view.findViewById(R.id.tv_offline_no_data);
        OfflineMapFileAdapter adapter = new OfflineMapFileAdapter(this);
        fileRecycler.setAdapter(adapter);
        adapter.setOnViewClickListener(onViewClickListener);
        getFileDir(filePath);
        adapter.setFilePaths(paths);

        if (paths.size() == 0) {
            offlineTip.setVisibility(View.VISIBLE);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择离线地图数据");
        builder.setView(view);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    /*
    获取离线地图存储文件目录路径
     */
    private void getFileDir(String fileDirPath) {
        paths = new ArrayList<>();
        File file = new File(fileDirPath);
        File[] files = file.listFiles();

        for (File fileTemp : files) {
            if (fileTemp.isDirectory()) {
                paths.add(fileTemp.getPath());
            }
        }
    }

    /*
    获取离线地图文件路径
     */
    private void getFile(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        for (File temp : files) {
            if (temp.isFile() && temp.getName().endsWith("mbtiles")) {
//                selectFile=temp;
                break;
            }
        }
    }

    private void saveData() {
        String name = offlineMapName.getText().toString().trim();
        String description = offlineMapDesc.getText().toString().trim();
        String elePath = offlineMapPath.getText().toString().trim();
        String satelPath = offlineSatellitePath.getText().toString().trim();
        OfflineMap offlineMap = new OfflineMap();
        offlineMap.setName(name);
        offlineMap.setDescription(description);
        offlineMap.setElePath(elePath);
        offlineMap.setSatelPath(satelPath);

        boolean success = offlineMap.save();
        if (success) {
            toastUtils.showSingletonToast("success");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_offline_map_path:
                selectType = 0;
                showFileDialog(Constant.ELECTRONIC_MAP_PATH);
                break;
            case R.id.tv_offline_satellite_path:
                selectType = 1;
                showFileDialog(Constant.SATELLITE_MAP_PATH);
                break;
            case R.id.tv_offline_map_save:
                offlineMapName.setCursorVisible(false);
                offlineMapDesc.setCursorVisible(false);
                saveData();
                finish();
                break;
        }
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
