package com.potato.kernel.Software;

public class PartitionItem {
    private int size;  // in MB
    private String label;
    private double bitlockerPercentage;

    public PartitionItem(int size, String label, double bitlockerPercentage) {
        this.size = size;
        this.label = label;
        this.bitlockerPercentage = bitlockerPercentage;
    }

    public int getSize() {
        return size;
    }

    public String getLabel() {
        return label;
    }

    public double getBitlockerPercentage() {
        return bitlockerPercentage;
    }
}
