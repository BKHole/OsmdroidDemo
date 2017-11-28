package com.bigemap.osmdroiddemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.constants.Constant;
import com.bigemap.osmdroiddemo.entity.Map;
import com.bigemap.osmdroiddemo.utils.DataKeeper;
import com.bigemap.osmdroiddemo.viewholder.BaseRecyclerViewHolder;
import com.bigemap.osmdroiddemo.viewholder.OnItemListener;

import java.util.List;

/**
 * 地图源adapter
 * Created by Think on 2017/10/18.
 */

public class MapSourceAdapter extends BaseRecyclerViewAdapter<MapSourceAdapter.ViewHolder> {
    private List<Map> mapList;
    private LayoutInflater inflater;
    private int defItem;
    private OnItemListener onItemListener;
    private Context context;
    private DataKeeper dataKeeper;

    public MapSourceAdapter(Context context) {
        this.context = context;
        dataKeeper = new DataKeeper(context, Constant.PREFS_NAME);
        defItem = dataKeeper.getInt(Constant.PREFS_MAP_SOURCE, 0);
        this.inflater = LayoutInflater.from(context);
    }

    public void setDataList(List<Map> maps) {
        this.mapList = maps;
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
        View view = inflater.inflate(R.layout.item_view_map_source, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolderS(ViewHolder holder, int position) {
        Map map = mapList.get(position);
        holder.setTag(map);
        holder.bind(map);
        if (defItem == position) {
            holder.mapIcon.setSelected(true);
        } else {
            holder.mapIcon.setSelected(false);
        }
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    class ViewHolder extends BaseRecyclerViewHolder implements View.OnClickListener {
        ImageView mapIcon;
        TextView mapName;

        private ViewHolder(View itemView) {
            super(itemView);
            mapIcon = (ImageView) itemView.findViewById(R.id.iv_map_source_icon);
            mapName = (TextView) itemView.findViewById(R.id.tv_map_source_name);
            itemView.setOnClickListener(this);
        }

        private void bind(Map map) {
            mapIcon.setImageResource(map.getMapIcon());
            mapName.setText(map.getMapName());
        }

        @Override
        public void onClick(View v) {
            if (onItemListener != null) {
                onItemListener.onClick(v, getLayoutPosition(), getTag());
            }
        }
    }
}
