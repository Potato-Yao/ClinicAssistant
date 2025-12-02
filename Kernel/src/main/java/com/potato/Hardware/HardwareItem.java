package com.potato.Hardware;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HardwareItem {
    @SerializedName("Id")
    private String id;

    @SerializedName("Name")
    private String name;

    @SerializedName("Type")
    private String type;

    @SerializedName("Sensors")
    private List<Sensor> sensors;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }
}
