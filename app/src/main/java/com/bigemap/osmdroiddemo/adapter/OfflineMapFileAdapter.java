package com.bigemap.osmdroiddemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.viewholder.BaseRecyclerViewHolder;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import java.io.File;
import java.util.ArrayList;

/**
 * 离线地图选择Adapter
 * Created by Think on 2017/11/15.
 */

public class OfflineMapFileAdapter extends BaseRecyclerViewAdapter<OfflineMapFileAdapter.ViewHolder> {

    private ArrayList<String> filePaths;   //paths：文件路径
    private LayoutInflater inflater;
    public OfflineMapFileAdapter(Context context){
        inflater=LayoutInflater.from(context);
    }

    public void setFilePaths(ArrayList<String> paths){
        this.filePaths=paths;
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.dialog_item_file_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolderS(ViewHolder holder, int position) {
        File file=new File(filePaths.get(position));
        holder.name.setText(file.getName());
        holder.setTag(file);
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    class ViewHolder extends BaseRecyclerViewHolder implements View.OnClickListener{
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name= (TextView) itemView.findViewById(R.id.tv_offline_map_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            OnViewClickListener listener=getOnViewClickListener();
            if (listener!=null){
                listener.onClick(v, getTag());
            }
        }
    }
}
