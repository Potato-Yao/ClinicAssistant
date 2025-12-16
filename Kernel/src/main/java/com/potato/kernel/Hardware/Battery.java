package com.potato.kernel.Hardware;

public class Battery {
    private int designedCapacity;
    private int capacity;
    private int remainCapacity;
    private int voltage;
    private int current;
    private int rate;  // charge or discharge power
    private boolean isCharging;

    public Battery(int designedCapacity, int capacity, int remainCapacity, int voltage, int current, int rate, int isCharging) {
        this.designedCapacity = designedCapacity;
        this.capacity = capacity;
        this.remainCapacity = remainCapacity;
        this.voltage = voltage;
        this.current = current;
        this.rate = rate;
        this.isCharging = isCharging == 1;
    }

    public double getHealthPercentage() {
        if (designedCapacity == 0) {
            return 0.0;
        }
        return ((double) capacity / designedCapacity) * 100.0;
    }

    public double getRemainPercentage() {
        if (capacity == 0) {
            return 0.0;
        }
        return ((double) remainCapacity / capacity) * 100.0;
    }

    public int getDesignedCapacity() {
        return designedCapacity;
    }

    public void setDesignedCapacity(int designedCapacity) {
        this.designedCapacity = designedCapacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRemainCapacity() {
        return remainCapacity;
    }

    public void setRemainCapacity(int remainCapacity) {
        this.remainCapacity = remainCapacity;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(int isCharging) {
        this.isCharging = isCharging == 1;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
    }
}
