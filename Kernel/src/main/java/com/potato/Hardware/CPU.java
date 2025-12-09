package com.potato.Hardware;

public class CPU extends HardwareItem {
    private String name;
    private double usage;
    private double packageTemperature;
    private double averageTemperature;
    private double power;
    private double clockBegin;
    private double clockEnd;
    private double load;
    private double voltage;

    public CPU(String name, double usage, double packageTemperature, double averageTemperature, double power, double clockBegin, double clockEnd, double load, double voltage) {
        this.name = name;
        this.usage = usage;
        this.packageTemperature = packageTemperature;
        this.averageTemperature = averageTemperature;
        this.power = power;
        this.clockBegin = clockBegin;
        this.clockEnd = clockEnd;
        this.load = load;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getUsage() {
        return usage;
    }

    public void setUsage(double usage) {
        this.usage = usage;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public double getClockBegin() {
        return clockBegin;
    }

    public void setClockBegin(double clockBegin) {
        this.clockBegin = clockBegin;
    }

    public double getClockEnd() {
        return clockEnd;
    }

    public void setClockEnd(double clockEnd) {
        this.clockEnd = clockEnd;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }

    public double getPackageTemperature() {
        return packageTemperature;
    }

    public void setPackageTemperature(double packageTemperature) {
        this.packageTemperature = packageTemperature;
    }

    public double getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(double averageTemperature) {
        this.averageTemperature = averageTemperature;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }
}
