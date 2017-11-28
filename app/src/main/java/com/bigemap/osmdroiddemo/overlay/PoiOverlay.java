package com.bigemap.osmdroiddemo.overlay;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.osmdroid.views.overlay.ItemizedIconOverlay;

import java.util.List;

/**
 * Created by Think on 2017/11/21.
 */

public class PoiOverlay extends ItemizedIconOverlay{
    public PoiOverlay(List pList, Drawable pDefaultMarker, OnItemGestureListener pOnItemGestureListener, Context pContext) {
        super(pList, pDefaultMarker, pOnItemGestureListener, pContext);
    }

    public PoiOverlay(List pList, OnItemGestureListener pOnItemGestureListener, Context pContext) {
        super(pList, pOnItemGestureListener, pContext);
    }

    public PoiOverlay(Context pContext, List pList, OnItemGestureListener pOnItemGestureListener) {
        super(pContext, pList, pOnItemGestureListener);
    }
}
