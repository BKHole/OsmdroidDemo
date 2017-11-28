package com.bigemap.osmdroiddemo.entity;

import com.google.gson.annotations.SerializedName;

/**
 * array object
 * Created by Think on 2017/11/1.
 */

public class Title {

    @SerializedName("tip")
    private Tip tip;

    public Tip getTip() {
        return tip;
    }

    public void setTip(Tip tip) {
        this.tip = tip;
    }

}
