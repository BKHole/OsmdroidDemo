package com.bigemap.osmdroiddemo.treelist;

import java.util.List;

public interface OnTreeNodeClickListener {
    void onClick(Node node, int position);

    void onCheckChange(Node node, int position,List<Node> checkedNodes);
}
