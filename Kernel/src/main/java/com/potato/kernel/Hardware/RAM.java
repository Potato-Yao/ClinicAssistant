package com.potato.kernel.Hardware;

public record RAM(double usage, double used, double free) {
    public double total() {
        return free + used;
    }
}
