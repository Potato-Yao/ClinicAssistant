package com.potato.kernel.Hardware;

public class RAM {
    private int totalSize;
    private int usedSize;
    private int freeSize;

    public RAM(int usedSize, int freeSize) {
        this.usedSize = usedSize;
        this.freeSize = freeSize;
        this.totalSize = usedSize + freeSize;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
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
