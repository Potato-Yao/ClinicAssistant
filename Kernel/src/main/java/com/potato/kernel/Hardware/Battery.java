package com.potato.kernel.Hardware;

/**
 * stores battery info
 */
public class Battery extends HardwareItem {
    private int designedCapacity;  // the maximum capacity the battery should have
    private int actuallyCapacity;  // the maximum capacity the battery actually has
    private int remainCapacity;  // the current remaining capacity
    private int voltage;  // the voltage battery supplies. be zero when charging
    private int current;  // the current battery supplies. be zero when charging
    private int rate;  // charge or discharge power. be zero when fully charged
    private boolean isCharging;

    public Battery(int designedCapacity, int actuallyCapacity, int remainCapacity, int voltage, int current, int rate, int isCharging) {
        this.designedCapacity = designedCapacity;
        this.actuallyCapacity = actuallyCapacity;
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
        return ((double) actuallyCapacity / designedCapacity) * 100.0;
    }

    public double getRemainPercentage() {
        if (actuallyCapacity == 0) {
            return 0.0;
        }
        return ((double) remainCapacity / actuallyCapacity) * 100.0;
    }

    public int getDesignedCapacity() {
        return designedCapacity;
    }

    public void setDesignedCapacity(int designedCapacity) {
        this.designedCapacity = designedCapacity;
    }

    public int getActuallyCapacity() {
        return actuallyCapacity;
    }

    public void setActuallyCapacity(int actuallyCapacity) {
        this.actuallyCapacity = actuallyCapacity;
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
