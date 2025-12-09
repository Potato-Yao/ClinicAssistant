package com.potato.Software;

public class PartitionItem {
    private int size;  // in MB
    private String label;
    private double bitlockerPercentage;

    public PartitionItem(int size, String label, double bitlockerPercentage) {
        this.size = size;
        this.label = label;
        this.bitlockerPercentage = bitlockerPercentage;
    }
}
