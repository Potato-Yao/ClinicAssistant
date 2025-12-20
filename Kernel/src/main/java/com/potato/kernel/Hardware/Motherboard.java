package com.potato.kernel.Hardware;

/**
 * stores motherboard info
 */
public class Motherboard extends HardwareItem {
    private String name;

    public Motherboard(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
