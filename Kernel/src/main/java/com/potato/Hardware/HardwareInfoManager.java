package com.potato.Hardware;

import com.google.gson.Gson;
import com.potato.External.LHMHelper;

import static com.potato.Utils.Admin.*;

import java.io.IOException;
import java.util.Arrays;

public class HardwareInfoManager {
    private static HardwareInfoManager manager;
    private LHMHelper lhmHelper;

    /*
    [0, 32] is for CPU
    [33, 64] is for GPU
    [65, 80] is for RAM
    [81, 96] is for Disk
    [97, 112] is for Motherboard
    [113, 128] is for Battery
    [129, 144] is for Network
     */
    private final static int INDEX_ARRAY_SIZE = 256;

    /*
    // [0, 32] for cpu
    0 -> cpu load total
    1 -> cpu package temperature
    2 -> cpu core average temperature
    3 -> cpu package power
    4 -> cpu core voltage
    5 -> cpu clock begin
    6 -> cpu clock end

    // [33, 64] for gpu
    33 -> gpu name
    34 -> gpu temperature
    35 -> gpu max temperature
    36 -> gpu power
    37 -> gpu speed
    38 -> gpu memory total
    39 -> gpu memory free
    40 -> gpu memory used
    41 -> gpu memory usage
     */
    private int[] index = new int[INDEX_ARRAY_SIZE];
    private String[] names = new String[INDEX_ARRAY_SIZE];

    private String updatedTime;
    private Motherboard motherboard = new Motherboard(null);

    private CPU cpu = new CPU(null, -1, -1, -1, -1, -1, -1, -1, -1);

    private GPU gpu = new GPU(null, -1, -1, -1, -1, -1, -1, -1, -1);

    private RAM ram = new RAM(-1, -1, -1);

    private Disk disk = new Disk();

    private Battery battery = new Battery();

    private Network network = new Network();

    // 0 for cpu fan, 1 for gpu fan, 2 for mid-fan
    private Fan[] fans = new Fan[3];

    private HardwareInfoManager() throws IOException {
        if (!isAdmin()) {
            throw new RuntimeException("The program is not run as administrator!");
        }

        names[0] = "";

        lhmHelper = LHMHelper.connect();

        Arrays.fill(fans, new Fan(-1));

        String lhmSensorsJson;
        try {
            lhmSensorsJson = lhmHelper.getHardwareList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (lhmSensorsJson.isEmpty()) {
            throw new RuntimeException("No hardware list returned!");
        }

        Arrays.fill(index, -1);
        Sensor[] sensors = new Gson().fromJson(lhmSensorsJson, Sensor[].class);
        for (Sensor sensor : sensors) {
            String name = sensor.getName();
            String info = sensor.getInfo();
            int ind = sensor.getIndex();
            if (name.equals("CPU Total") && info.equals("Load")) {
                index[0] = ind;
            } else if (name.equals("CPU Package") && info.equals("Temperature")) {
                index[1] = ind;
            } else if (name.equals("Core Average") && info.equals("Temperature")) {
                index[2] = ind;
            } else if (name.equals("CPU Package") && info.equals("Power")) {
                index[3] = ind;
            } else if (name.equals("CPU Core") && info.equals("Voltage")) {
                index[4] = ind;
            } else if (name.equals("CPU Clock #1") && info.equals("Clock")) {
                index[5] = ind;
            } else if (name.contains("CPU Clock #") && info.equals("Clock")) {
                index[6] = Math.max(ind, index[6]);
            } else if (name.equals("GPU Core") && info.equals("Temperature")) {
                index[34] = ind;
            } else if (name.equals("GPU Hot Spot") && info.equals("Temperature")) {
                index[35] = ind;
            } else if (name.equals("GPU Package") && info.equals("Power")) {
                index[36] = ind;
            } else if (name.equals("GPU Core") && info.equals("Clock")) {
                index[37] = ind;
            } else if (name.equals("GPU Memory Total") && info.equals("SmallData")) {
                index[38] = ind;
            } else if (name.equals("GPU Memory Free") && info.equals("SmallData")) {
                index[39] = ind;
            } else if (name.equals("GPU Memory Used") && info.equals("SmallData")) {
                index[40] = ind;
            }
        }
    }

    public static HardwareInfoManager getHardwareInfoManager() throws IOException {
        if (manager == null) {
            manager = new HardwareInfoManager();
        }
        return manager;
    }

    public void update() throws IOException {
        lhmHelper.update();

        if (index[0] != -1) {
            cpu.setLoad(lhmHelper.getValue(index[0]));
        }
        if (index[1] != -1) {
            cpu.setPackageTemperature(lhmHelper.getValue(index[1]));
        }
        if (index[2] != -1) {
            cpu.setAverageTemperature(lhmHelper.getValue(index[2]));
        }
        if (index[3] != -1) {
            cpu.setPower(lhmHelper.getValue(index[3]));
        }
        if (index[4] != -1) {
            cpu.setVoltage(lhmHelper.getValue(index[4]));
        }
        if (index[5] != -1) {
            cpu.setClockBegin(lhmHelper.getValue(index[5]));
        }
        if (index[6] != -1) {
            cpu.setClockEnd(lhmHelper.getValue(index[6]));
        }
        if (index[34] != -1) {
            gpu.setTemperature(lhmHelper.getValue(index[34]));
        }
        if (index[35] != -1) {
            gpu.setMaxTemperature(lhmHelper.getValue(index[35]));
        }
        if (index[36] != -1) {
            gpu.setPower(lhmHelper.getValue(index[36]));
        }
        if (index[37] != -1) {
            gpu.setSpeed(lhmHelper.getValue(index[37]));
        }
        if (index[38] != -1) {
            gpu.setMemTotal(lhmHelper.getValue(index[38]));
        }
        if (index[39] != -1) {
            gpu.setMemFree(lhmHelper.getValue(index[39]));
        }
        if (index[40] != -1) {
            gpu.setMemUsed(lhmHelper.getValue(index[40]));
        }
    }

    public void close() {
        try {
            this.lhmHelper.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static HardwareInfoManager getManager() {
        return manager;
    }

    public static void setManager(HardwareInfoManager manager) {
        HardwareInfoManager.manager = manager;
    }

    public LHMHelper getLhmHelper() {
        return lhmHelper;
    }

    public void setLhmHelper(LHMHelper lhmHelper) {
        this.lhmHelper = lhmHelper;
    }

    public int[] getIndex() {
        return index;
    }

    public void setIndex(int[] index) {
        this.index = index;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Motherboard getMotherboard() {
        return motherboard;
    }

    public void setMotherboard(Motherboard motherboard) {
        this.motherboard = motherboard;
    }

    public CPU getCpu() {
        return cpu;
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public GPU getGpu() {
        return gpu;
    }

    public void setGpu(GPU gpu) {
        this.gpu = gpu;
    }

    public RAM getRam() {
        return ram;
    }

    public void setRam(RAM ram) {
        this.ram = ram;
    }

    public Disk getDisk() {
        return disk;
    }

    public void setDisk(Disk disk) {
        this.disk = disk;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Fan[] getFans() {
        return fans;
    }

    public void setFans(Fan[] fans) {
        this.fans = fans;
    }
}
