package com.potato.kernel.Hardware;

public class GPU extends HardwareItem {
    public GPU(String name, double temperature, double maxTemperature, double power, double speed, double memTotal, double memFree, double memUsed, double memUsage) {
        this.name = name;
        this.temperature = temperature;
        this.maxTemperature = maxTemperature;
        this.power = power;
        this.speed = speed;
        this.memTotal = memTotal;
        this.memFree = memFree;
        this.memUsed = memUsed;
        this.memUsage = memUsage;
    }


    private String name;

    private double temperature;

    private double maxTemperature;

    private double power;

    private double speed;

    private double memTotal;

    private double memFree;

    private double memUsed;

    private double memUsage;

    public void setName(String name) {
        this.name = name;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getMemTotal() {
        return memTotal;
    }

    public void setMemTotal(double memTotal) {
        this.memTotal = memTotal;
    }

    public double getMemFree() {
        return memFree;
    }

    public void setMemFree(double memFree) {
        this.memFree = memFree;
    }

    public double getMemUsed() {
        return memUsed;
    }

    public void setMemUsed(double memUsed) {
        this.memUsed = memUsed;
    }

    public double getMemUsage() {
        return memUsage;
    }

    public void setMemUsage(double memUsage) {
        this.memUsage = memUsage;
    }
}
