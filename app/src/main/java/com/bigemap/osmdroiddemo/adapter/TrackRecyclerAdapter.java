package com.bigemap.osmdroiddemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.entity.Track;

import java.util.List;

/**
 * 轨迹adapter
 * Created by Think on 2017/9/14.
 */

public class TrackRecyclerAdapter extends RecyclerView.Adapter<TrackRecyclerAdapter.TrackViewHolder> {
    private List<Track> mData;
    private Context mContext;
    private LayoutInflater inflater;


    public TrackRecyclerAdapter(List<Track> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
        inflater = LayoutInflater.from(mContext);
    }

    public void setDataList(List<Track> mData) {
        for (Track track : mData) {
            this.mData.add(track);
        }
    }

    public void clearAllData(){
        this.mData.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_view_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TrackViewHolder holder, int position) {
        Track track = mData.get(position);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.itemView, holder.getLayoutPosition());
                }
            });
        }
        if (TextUtils.isEmpty(track.getName())) {
            track.setName("绘制" + position);
        }
        holder.tvTrackName.setText(track.getName());
        holder.tvTrackTime.setText(track.getStartTime());

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView tvTrackName;
        TextView tvTrackTime;

        public TrackViewHolder(View itemView) {
            super(itemView);
            tvTrackName = (TextView) itemView.findViewById(R.id.txt_track_name);
            tvTrackTime = (TextView) itemView.findViewById(R.id.txt_track_time);
        }

    }

}
