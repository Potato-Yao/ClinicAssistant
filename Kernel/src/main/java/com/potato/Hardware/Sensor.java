package com.potato.Hardware;

import com.google.gson.annotations.SerializedName;

public class Sensor {
    @SerializedName("Id")
    private String id;

    @SerializedName("Name")
    private String name;

    @SerializedName("Type")
    private String type;

    @SerializedName("Value")
    private double value;

    @SerializedName("Min")
    private double min;

    @SerializedName("Max")
    private double max;

    @SerializedName("Index")
    private int index;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getIndex() {
        return index;
    }
}
