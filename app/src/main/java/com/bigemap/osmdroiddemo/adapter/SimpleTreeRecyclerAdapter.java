package com.bigemap.osmdroiddemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.treelist.Node;
import com.bigemap.osmdroiddemo.treelist.OnTreeNodeClickListener;
import com.bigemap.osmdroiddemo.treelist.TreeRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SimpleTreeRecyclerAdapter extends TreeRecyclerAdapter {
    private int defItem=-1;
    public SimpleTreeRecyclerAdapter(RecyclerView mTree, Context context, List<Node> datas, int defaultExpandLevel, int iconExpand, int iconNoExpand) {
        super(mTree, context, datas, defaultExpandLevel, iconExpand, iconNoExpand);
    }


    public SimpleTreeRecyclerAdapter(RecyclerView mTree, Context context, List<Node> datas, int defaultExpandLevel) {
        super(mTree, context, datas, defaultExpandLevel);
    }

    public void setItemSelected(int position) {
        this.defItem = position;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHoder(View.inflate(mContext, R.layout.item_view_my_layers, null));
    }

    @Override
    public void onBindViewHolder(final Node node, RecyclerView.ViewHolder holder, final int position) {

        final MyHoder viewHolder = (MyHoder) holder;
        if (defItem == position) {
            viewHolder.layout.setSelected(true);
        }else{
            viewHolder.layout.setSelected(false);
        }
        //todo do something
        viewHolder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChecked(node, viewHolder.cb.isChecked());
                List<Node> checkedNodes = new ArrayList<Node>();
                for(Node n:mAllNodes){
                    if(n.isChecked()){
                        checkedNodes.add(n);
                    }
                }
                onTreeNodeClickListener.onCheckChange(node,position,checkedNodes);
//                SimpleTreeRecyclerAdapter.this.notifyDataSetChanged();
            }
        });

        if (node.isChecked()) {
            viewHolder.cb.setChecked(true);
        } else {
            viewHolder.cb.setChecked(false);
        }

        if (node.getIcon() == -1) {
            viewHolder.icon.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.icon.setImageResource(node.getIcon());
        }
        viewHolder.label.setText(node.getName());
    }

    class MyHoder extends RecyclerView.ViewHolder {

        LinearLayout layout;
        public CheckBox cb;

        public TextView label;

        public ImageView icon;

        public MyHoder(View itemView) {
            super(itemView);
            layout= (LinearLayout) itemView.findViewById(R.id.ly_item_my_layers);
            cb = (CheckBox) itemView.findViewById(R.id.item_my_layer);
            label = (TextView) itemView.findViewById(R.id.id_treenode_label);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }

    }
}
