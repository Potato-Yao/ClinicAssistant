package com.potato.Hardware;

import com.google.gson.Gson;
import com.potato.Native.LHMHelper;

import java.io.IOException;

public class HardwareInfoManager {
    private static HardwareInfoManager manager;
    private LHMHelper lhmHelper;

    private String updatedTime;
    private Motherboard motherboard;
    private CPU cpu;
    private GPU gpu;
    private RAM ram;
    // 0 for cpu fan, 1 for gpu fan, 2 for mid-fan
    private Fan[] fans;

    private HardwareInfoManager() {
//        lhmHelper = new LHMHelper();  // i know LHMHelper has only static methods, this is for the case i change my mind
//        lhmHelper.connect();
    }

    public static HardwareInfoManager getHardwareInfoManager() {
        if (manager == null) {
            manager = new HardwareInfoManager();
        }
        return manager;
    }

    public void update() {
        String lhmSensorsJson;
        try {
            lhmSensorsJson = lhmHelper.getHardwareList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (lhmSensorsJson.isEmpty()) {
            throw new RuntimeException("No hardware info returned!");
        }

        HardwareInfo hardwareInfo = new Gson().fromJson(lhmSensorsJson, HardwareInfo.class);
        this.updatedTime = hardwareInfo.getTimestampUtc();

        for (HardwareItem item : hardwareInfo.getHardware()) {
            // switch in java doesn't be designed well compared to rust, thus ill not use it
            String type = item.getType();
            if (type.equals("Motherboard")) {
                this.motherboard = new Motherboard(item.getName());
            } else if (type.equals("Cpu")) {
                CPUBuilder cpuBuilder = new CPUBuilder();
                cpuBuilder.name(item.getName());
                for (Sensor sensor : item.getSensors()) {
                    String sensorName = sensor.getName();
                    if (sensorName.equals("CPU Total")) {
                        cpuBuilder.usage(sensor.getValue());
                    }
                }
            }
        }
    }

    public Motherboard getMotherboard() {
        return motherboard;
    }

    public CPU getCpu() {
        return cpu;
    }

    public GPU getGpu() {
        return gpu;
    }

    public RAM getRam() {
        return ram;
    }

    public Fan[] getFans() {
        return fans;
    }
}
