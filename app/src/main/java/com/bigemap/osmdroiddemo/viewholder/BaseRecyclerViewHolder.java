/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bigemap.osmdroiddemo.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BaseRecyclerViewHolder extends RecyclerView.ViewHolder {
    private Object mTag;

    private OnViewClickListener mOnViewClickListener;
    private OnViewLongClickListener mOnViewLongClickListener;

    public BaseRecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public Object getTag() {
        return mTag;
    }

    public final void setOnViewClickListener(OnViewClickListener l) {
        mOnViewClickListener = l;
    }

    public final void setOnViewLongClickListener(OnViewLongClickListener l) {
        mOnViewLongClickListener = l;
    }

    public final OnViewClickListener getOnViewClickListener() {
        return mOnViewClickListener;
    }

    public final OnViewLongClickListener getOnViewLongClickListener() {
        return mOnViewLongClickListener;
    }
}
