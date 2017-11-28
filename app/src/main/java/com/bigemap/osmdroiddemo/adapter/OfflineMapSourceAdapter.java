package com.bigemap.osmdroiddemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.entity.OfflineMap;
import com.bigemap.osmdroiddemo.view.IconView;
import com.bigemap.osmdroiddemo.viewholder.BaseRecyclerViewHolder;
import com.bigemap.osmdroiddemo.viewholder.OnItemListener;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import java.util.List;

/**
 * 离线地图源Adapter
 * Created by Think on 2017/11/16.
 */

public class OfflineMapSourceAdapter extends BaseRecyclerViewAdapter<OfflineMapSourceAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<OfflineMap> entities;
    private int defItem=-1;
    private OnItemListener onItemListener;

    public OfflineMapSourceAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setData(List<OfflineMap> offlineMaps) {
        this.entities = offlineMaps;
        notifyDataSetChanged();
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public void setItemSelected(int position) {
        this.defItem = position;
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_offline_map_source, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolderS(ViewHolder holder, int position) {
        OfflineMap entity=entities.get(position);
        holder.setTag(entity);
        holder.name.setText(entity.getName());
        if (defItem == position) {
            holder.icon.setTextColor(Color.BLUE);
        }else{
            holder.icon.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return entities.size();
    }

    class ViewHolder extends BaseRecyclerViewHolder implements View.OnClickListener{
        IconView icon;
        TextView name;
        private ViewHolder(View itemView) {
            super(itemView);
            icon= (IconView) itemView.findViewById(R.id.item_offline_icon);
            name= (TextView) itemView.findViewById(R.id.item_offline_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemListener != null) {
                onItemListener.onClick(v, getLayoutPosition(), getTag());
            }
        }
    }
}
