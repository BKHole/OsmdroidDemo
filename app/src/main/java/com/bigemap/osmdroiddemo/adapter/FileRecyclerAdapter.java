package com.bigemap.osmdroiddemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.utils.DateUtils;
import com.bigemap.osmdroiddemo.view.IconView;
import com.bigemap.osmdroiddemo.viewholder.BaseRecyclerViewHolder;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;
import com.bigemap.osmdroiddemo.viewholder.OnViewLongClickListener;

import java.io.File;
import java.util.List;

/**
 * 文件列表adapter
 * Created by Think on 2017/9/25.
 */

public class FileRecyclerAdapter extends BaseRecyclerViewAdapter<FileRecyclerAdapter.FileHolder>{
    private List<String> filePaths;   //paths：文件路径
    private List<String> fileSizes;   //sizes：文件大小
    private LayoutInflater inflater;
    private Context mContext;

    public FileRecyclerAdapter(Context context){
        this.mContext=context;
        inflater=LayoutInflater.from(context);
    }

    public void setFilePaths(List<String> paths){
        this.filePaths=paths;
        notifyDataSetChanged();
    }

    public void setFileSizes(List<String> sizes){
        this.fileSizes=sizes;
        notifyDataSetChanged();
    }


    @Override
    public FileHolder onCreateViewHolderS(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.item_view_file, parent, false);
        return new FileHolder(view);
    }

    @Override
    public void onBindViewHolderS(FileHolder holder, int position) {
        File f = new File(filePaths.get(position));
        String date=DateUtils.formatUTC(f.lastModified(),"yyyy-MM-dd");
        holder.setTag(f);
        holder.f_title.setText(f.getName());
        holder.f_date.setText(date);
        if(f.isDirectory()){
            holder.f_icon.setText(mContext.getResources().getText(R.string.folder));
            holder.f_size.setText("");
        }else{
            holder.f_size.setText(fileSizes.get(position));
            holder.f_icon.setText(mContext.getResources().getText(R.string.file));
        }
    }

    @Override
    public int getItemCount() {
        return filePaths.size();
    }

    class FileHolder extends BaseRecyclerViewHolder implements View.OnClickListener {

        TextView f_title;
        TextView f_size;
        IconView f_icon;
        TextView f_date;
        private FileHolder(View itemView) {
            super(itemView);
            f_title= (TextView) itemView.findViewById(R.id.tv_file_manager_file_title);
            f_size= (TextView) itemView.findViewById(R.id.tv_file_manager_file_size);
            f_icon= (IconView) itemView.findViewById(R.id.iv_file_manager_file_icon);
            f_date= (TextView) itemView.findViewById(R.id.tv_file_manager_file_date);
            itemView.setOnClickListener(this);
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
