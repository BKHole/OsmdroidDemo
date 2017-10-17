package com.bigemap.osmdroiddemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigemap.osmdroiddemo.R;
import com.bigemap.osmdroiddemo.viewholder.BaseRecyclerViewHolder;
import com.bigemap.osmdroiddemo.viewholder.OnViewClickListener;

import java.io.File;
import java.util.List;

/**
 * 文件列表adapter
 * Created by Think on 2017/9/25.
 */

public class FileRecyclerAdapter extends BaseRecyclerViewAdapter<FileRecyclerAdapter.FileHolder>{
    private List<String> fileItems;      //items：存放显示的名称
    private List<String> filePaths;   //paths：存放文件路径
    private List<String> fileSizes;   //sizes：文件大小
    private LayoutInflater inflater;
    private Bitmap mIcon_video;
    private Bitmap mIcon_folder;
    private Context mContext;

    public FileRecyclerAdapter(Context context){
        this.mContext=context;
        inflater=LayoutInflater.from(context);
    }

    public void setFileItems(List<String> items){
        this.fileItems=items;
        notifyDataSetChanged();
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
        mIcon_folder = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.img_folder);      //文件夹的图文件
        mIcon_video = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.img_video);
        View view=inflater.inflate(R.layout.item_view_file, parent, false);
        return new FileHolder(view);
    }

    @Override
    public void onBindViewHolderS(FileHolder holder, int position) {
        File f = new File(filePaths.get(position));
        holder.setTag(f);
        holder.f_title.setText(f.getName());
        if(f.isDirectory()){
            holder.f_icon.setImageBitmap(mIcon_folder);
            holder.f_size.setText("");
        }else{
            holder.f_size.setText(fileSizes.get(position));
            holder.f_icon.setImageBitmap(mIcon_video);
        }
    }

    @Override
    public int getItemCount() {
        return fileItems.size();
    }

    class FileHolder extends BaseRecyclerViewHolder implements View.OnClickListener {

        TextView f_title;
        TextView f_size;
        ImageView f_icon;
        private FileHolder(View itemView) {
            super(itemView);
            f_title= (TextView) itemView.findViewById(R.id.tv_file_manager_file_title);
            f_size= (TextView) itemView.findViewById(R.id.tv_file_manager_file_size);
            f_icon= (ImageView) itemView.findViewById(R.id.iv_file_manager_file_icon);
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
