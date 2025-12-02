package com.potato.Hardware;

public class CPUBuilder {
    private String name;
    private double usage;
    private double temperature;
    private double power;
    private double speed;

    public CPUBuilder() {
        this.name = null;
        this.usage = -1;
        this.temperature = -1;
        this.power = -1;
        this.speed = -1;
    }

    public CPUBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CPUBuilder usage(double usage) {
        this.usage = usage;
        return this;
    }

    public CPUBuilder temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public CPUBuilder power(double power) {
        this.power = power;
        return this;
    }

    public CPUBuilder speed(double speed) {
        this.speed = speed;
        return this;
    }

    public CPU build() {
        return new CPU(name, usage, temperature, power, speed);
    }
}
