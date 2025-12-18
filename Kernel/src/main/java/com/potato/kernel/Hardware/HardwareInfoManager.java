package com.potato.kernel.Hardware;

import com.google.gson.Gson;
import com.potato.kernel.Config;
import com.potato.kernel.External.LHMHelper;

import static com.potato.kernel.Utils.Admin.*;

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

    // [65, 80] for RAM
    65 -> mem used
    66 -> mem available

    // [113, 128] for battery
    113 -> battery capacity
    114 -> battery remain capacity
    115 -> battery voltage
    116 -> battery current
    117 -> battery charge/discharge rate
    118 -> battery designed capacity
     */
    private int[] index = new int[INDEX_ARRAY_SIZE];
    private String[] names = new String[INDEX_ARRAY_SIZE];

    private String updatedTime;
    private Motherboard motherboard = new Motherboard(null);

    private CPU cpu = new CPU(null, -1, -1, -1, -1, -1, -1);
    private GPU gpu = new GPU(null, -1, -1, -1, -1, -1, -1, -1, -1);
    private RAM ram = new RAM(-1, -1);
    private Disk disk = new Disk();
    private Battery battery = new Battery(-1, -1, -1, -1, -1, -1, 0);
    private Network network = new Network();
    // 0 for cpu fan, 1 for gpu fan, 2 for mid-fan
    private Fan[] fans = new Fan[3];

    private int prevBatteryCapacity = -1;

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

            // THE CODE BELOW IS SCRIPT GENERATED, DON'T CHANGE THEM DIRECTLY! CHANGE THE SCRIPT sensor_map.py INSTEAD
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
            } else if (name.equals("CPU Core #1") && info.equals("Clock")) {
                index[5] = ind;
            } else if (name.contains("CPU Core #") && info.equals("Clock")) {
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
            } else if (name.equals("Memory Used") && info.equals("Data")) {
                index[65] = ind;
            } else if (name.equals("Memory Available") && info.equals("Data")) {
                index[66] = ind;
            } else if (name.equals("Fully-Charged Capacity") && info.equals("Energy")) {
                index[113] = ind;
            } else if (name.equals("Remaining Capacity") && info.equals("Energy")) {
                index[114] = ind;
            } else if (name.equals("Voltage") && info.equals("Voltage")) {
                index[115] = ind;
            } else if (name.equals("Charge Current") && info.equals("Current")) {
                index[116] = ind;
            } else if (name.equals("Discharge Current") && info.equals("Current")) {
                index[116] = ind;
            } else if (name.equals("Charge/Discharge Current") && info.equals("Current")) {
                index[116] = ind;
            } else if (name.equals("Charge Rate") && info.equals("Power")) {
                index[117] = ind;
            } else if (name.equals("Discharge Rate") && info.equals("Power")) {
                index[117] = ind;
            } else if (name.equals("Charge/Discharge Rate") && info.equals("Power")) {
                index[117] = ind;
            } else if (name.equals("Designed Capacity") && info.equals("Energy")) {
                index[118] = ind;
            }
        }
        // THE CODE ABOVE IS SCRIPT GENERATED, DON'T CHANGE THEM DIRECTLY! CHANGE THE SCRIPT sensor_map.py INSTEAD

        // update hardware info automatically
        Thread updater = new Thread(() -> {
            while (true) {
                try {
                    update();
                    Thread.sleep(Config.HARDWARE_INFO_SEEK_RATE);
                } catch (IOException | InterruptedException e) {
                    break;
                }
            }
        });
        updater.setDaemon(true);
        updater.start();
    }

    public static HardwareInfoManager getHardwareInfoManager() throws IOException {
        if (manager == null) {
            manager = new HardwareInfoManager();
        }
        return manager;
    }

    public void update() throws IOException {
        lhmHelper.update();

        // THE CODE BELOW IS SCRIPT GENERATED, DON'T CHANGE THEM DIRECTLY! CHANGE THE SCRIPT sensor_map.py INSTEAD
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
            cpu.setClockBeginIndex(index[5]); //(lhmHelper.getValue(index[5]));
        }
        if (index[6] != -1) {
            cpu.setClockEndIndex(index[6]); //(lhmHelper.getValue(index[6]));
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
        if (index[65] != -1) {
            ram.setUsedSize(lhmHelper.getValue(index[65]));
        }
        if (index[66] != -1) {
            ram.setFreeSize(lhmHelper.getValue(index[66]));
            System.out.println(ram.getFreeSize());
        }
        if (index[113] != -1) {
            battery.setCapacity(lhmHelper.getValue(index[113]));
        }
        if (index[114] != -1) {
            battery.setRemainCapacity(lhmHelper.getValue(index[114]));
        }
        if (index[115] != -1) {
            battery.setVoltage(lhmHelper.getValue(index[115]));
        }
        if (index[116] != -1) {
            battery.setCurrent(lhmHelper.getValue(index[116]));
        }
        if (index[116] != -1) {
            battery.setCurrent(lhmHelper.getValue(index[116]));
        }
        if (index[116] != -1) {
            battery.setCurrent(lhmHelper.getValue(index[116]));
        }
        if (index[117] != -1) {
            battery.setRate(lhmHelper.getValue(index[117]));
        }
        if (index[117] != -1) {
            battery.setRate(lhmHelper.getValue(index[117]));
        }
        if (index[117] != -1) {
            battery.setRate(lhmHelper.getValue(index[117]));
        }
        if (index[118] != -1) {
            battery.setDesignedCapacity(lhmHelper.getValue(index[118]));
        }
        // THE CODE ABOVE IS SCRIPT GENERATED, DON'T CHANGE THEM DIRECTLY! CHANGE THE SCRIPT sensor_map.py INSTEAD

        double clock = 0;
        double firstClock = 0;
        double secondClock = 0;
        double thirdClock = 0;
        for (int i = cpu.getClockBeginIndex(); i <= cpu.getClockEndIndex(); i++) {
            if (lhmHelper.getValue(i) > firstClock) {
                thirdClock = secondClock;
                secondClock = firstClock;
                firstClock = lhmHelper.getValue(i);
            }

            clock = firstClock * 0.5 + secondClock * 0.25 + thirdClock * 0.25;
            clock /= 1000;
//            clock = 3.952 * Math.pow(clock, 3) - 46.6351 * Math.pow(clock, 2) + 182.335 * clock - 232.454;  // by using Newton interpolation formula, compare to task manager
        }
        cpu.setClock(clock);

        if (prevBatteryCapacity < battery.getRemainCapacity() || battery.getRate() == 0) {
            battery.setCharging(true);
        } else if (prevBatteryCapacity > battery.getRemainCapacity()) {
            battery.setCharging(false);
        }
        prevBatteryCapacity = battery.getRemainCapacity();
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
