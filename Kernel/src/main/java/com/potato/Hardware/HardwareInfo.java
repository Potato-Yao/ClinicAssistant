package com.potato.Hardware;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HardwareInfo {
    @SerializedName("TimestampUtc")
    private String timestampUtc;

    @SerializedName("Hardware")
    private List<HardwareItem> hardware;

    public String getTimestampUtc() {
        return timestampUtc;
    }

    public List<HardwareItem> getHardware() {
        return hardware;
    }
}
