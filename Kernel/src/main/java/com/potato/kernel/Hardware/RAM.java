package com.potato.kernel.Hardware;

public class RAM {
    private int usedSize;
    private int freeSize;

    public RAM(int usedSize, int freeSize) {
        this.usedSize = usedSize;
        this.freeSize = freeSize;
    }

    public double getUsedPercentage() {
        if (getTotalSize() == 0) {
            return 0;
        }
        return (double) usedSize / getTotalSize() * 100;
    }

    public int getTotalSize() {
        return usedSize + freeSize;
    }

    public int getUsedSize() {
        return usedSize;
    }

    public void setUsedSize(int usedSize) {
        this.usedSize = usedSize;
    }

    public int getFreeSize() {
        return freeSize;
    }

    public void setFreeSize(int freeSize) {
        this.freeSize = freeSize;
    }
}
