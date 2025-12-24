package com.potato.kernel.Hardware;

import com.google.gson.annotations.SerializedName;

/**
 * fits the result from lhm, represent a sensor info it gives
 */
public class Sensor {
    @SerializedName("Id")
    private String id;

    @SerializedName("Name")
    private String name;

    @SerializedName("Index")
    private  int index;

    @SerializedName("Info")
    private String info;

    public String getInfo() {
        return info;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
