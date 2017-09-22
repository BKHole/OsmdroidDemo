package com.bigemap.osmdroiddemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.bigemap.osmdroiddemo.viewholder.BaseRecyclerViewHolder;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;
import com.bigemap.osmdroiddemo.viewholder.OnViewLongClickListener;

/**
 * recyclerViewAdapter基类
 * Created by Think on 2017/9/21.
 */

public abstract class BaseRecyclerViewAdapter<VH extends BaseRecyclerViewHolder> extends RecyclerView.Adapter<VH> {
    private OnViewClickListener mOnViewClickListener;
    private OnViewLongClickListener mOnViewLongClickListener;

    @Override
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        VH holder = onCreateViewHolderS(parent, viewType);
        // TODO: set listeners when onCreate?
        return holder;
    }

    public abstract VH onCreateViewHolderS(ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        holder.setOnViewClickListener(mOnViewClickListener);
        holder.setOnViewLongClickListener(mOnViewLongClickListener);
        onBindViewHolderS(holder, position);
    }

    public abstract void onBindViewHolderS(VH holder, int position);

    public void setOnViewClickListener(OnViewClickListener l) {
        mOnViewClickListener = l;
    }

    public void setOnViewLongClickListener(OnViewLongClickListener l) {
        mOnViewLongClickListener = l;
    }

    public OnViewClickListener getOnViewClickListener() {
        return mOnViewClickListener;
    }

    public OnViewLongClickListener getOnViewLongClickListener() {
        return mOnViewLongClickListener;
    }
}
