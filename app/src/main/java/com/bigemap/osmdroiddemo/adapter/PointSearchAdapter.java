package com.bigemap.osmdroiddemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.entity.Coordinate;
import com.bigemap.osmdroiddemo.viewholder.BaseRecyclerViewHolder;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import java.util.List;

/**
 * 位置搜索adapter
 * Created by Think on 2017/9/29.
 */

public class PointSearchAdapter extends BaseRecyclerViewAdapter<PointSearchAdapter.ViewHolder>{
    private LayoutInflater inflater;
    private List<Coordinate> mData;

    public PointSearchAdapter(Context context){
        inflater=LayoutInflater.from(context);
    }

    public void setDataList(List<Coordinate> coordinates){
        this.mData=coordinates;
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.item_view_point_search, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolderS(ViewHolder holder, int position) {
        Coordinate coordinate=mData.get(position);
        holder.setTag(coordinate);
        holder.bind(coordinate);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends BaseRecyclerViewHolder implements View.OnClickListener{
        TextView positionName;
        TextView positionAddr;

        public ViewHolder(View itemView) {
            super(itemView);
            positionAddr= (TextView) itemView.findViewById(R.id.tv_point_search_address);
            positionName= (TextView) itemView.findViewById(R.id.tv_point_search_name);
            itemView.setOnClickListener(this);
        }

        private void bind(Coordinate coordinate){
            positionName.setText(coordinate.getName());
            positionAddr.setText(coordinate.getDescription());
        }

        @Override
        public void onClick(View v) {
            OnViewClickListener listener = getOnViewClickListener();
            if (listener != null) {
                listener.onClick(v, getTag());
            }
        }
    }
}
