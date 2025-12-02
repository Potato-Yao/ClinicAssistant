package com.potato.Hardware;

public record GPU(String name, double temperature, double maxTemperature, double power, double speed, double memTotal,
                  double memFree, double memUsed, double memUsage) {
}
