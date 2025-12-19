package com.potato.kernel.Hardware;

/**
 * stores fan info
 */
public class Fan extends HardwareItem {
    private int fanSpeed;

    public Fan(int fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public int getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(int fanSpeed) {
        this.fanSpeed = fanSpeed;
    }
}
