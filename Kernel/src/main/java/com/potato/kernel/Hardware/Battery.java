package com.potato.kernel.Hardware;

public class Battery {
    private int designedCapacity;
    private int capacity;
    private int remainCapacity;
    private int voltage;
    private int current;
    private int rate;  // charge or discharge power

    public Battery(int designedCapacity, int capacity, int remainCapacity, int voltage, int current, int rate) {
        this.designedCapacity = designedCapacity;
        this.capacity = capacity;
        this.remainCapacity = remainCapacity;
        this.voltage = voltage;
        this.current = current;
        this.rate = rate;
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
}
