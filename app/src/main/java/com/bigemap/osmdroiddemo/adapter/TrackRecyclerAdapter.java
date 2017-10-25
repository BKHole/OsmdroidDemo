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
import com.bigemap.osmdroiddemo.viewholder.BaseRecyclerViewHolder;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;
import com.bigemap.osmdroiddemo.viewholder.OnViewLongClickListener;

import java.util.List;

/**
 * 轨迹adapter
 * Created by Think on 2017/9/14.
 */

public class TrackRecyclerAdapter extends BaseRecyclerViewAdapter<TrackRecyclerAdapter.TrackViewHolder> {
    private List<Track> mData;
    private LayoutInflater inflater;

    public TrackRecyclerAdapter(Context mContext) {
        inflater = LayoutInflater.from(mContext);
    }

    public void setDataList(List<Track> mData) {
        this.mData=mData;
        notifyDataSetChanged();
    }

    public void clearAllData(){
        this.mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public TrackViewHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_view_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolderS(TrackViewHolder holder, int position) {
        Track track = mData.get(position);
        holder.setTag(track);
        holder.bind(track);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class TrackViewHolder extends BaseRecyclerViewHolder implements View.OnClickListener,View.OnLongClickListener {
        TextView tvTrackName;
        TextView tvTrackTime;

        public TrackViewHolder(View itemView) {
            super(itemView);
            tvTrackName = (TextView) itemView.findViewById(R.id.txt_track_name);
            tvTrackTime = (TextView) itemView.findViewById(R.id.txt_track_time);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private void bind(Track track){
            if (TextUtils.isEmpty(track.getName())) {
                track.setName("绘制" + track.getId());
            }
            tvTrackName.setText(track.getName());
            tvTrackTime.setText(track.getStartTime());
        }

        @Override
        public void onClick(View v) {
            OnViewClickListener listener = getOnViewClickListener();
            if (listener != null) {
                listener.onClick(v, getTag());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            OnViewLongClickListener longClickListener=getOnViewLongClickListener();
            if (longClickListener!=null){
                longClickListener.onLongClick(v, getTag());
            }
            return true;
        }
    }

}
