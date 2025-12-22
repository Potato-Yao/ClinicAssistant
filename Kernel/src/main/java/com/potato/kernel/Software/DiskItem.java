package com.potato.kernel.Software;

/**
 * records disk's id and size, fitting diskpart's output of list disks:
 */
public class DiskItem {
    private int id;
    private double size;  // in GB

    public DiskItem(int id, double size) {
        this.id = id;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * in GB
     * @return
     */
    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
}
