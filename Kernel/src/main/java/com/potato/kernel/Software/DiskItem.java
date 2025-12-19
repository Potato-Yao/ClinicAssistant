package com.potato.kernel.Software;

/**
 * records disk's id and size, fitting diskpart's output of list disks:
 */
public class DiskItem {
    private int id;
    private int size;

    public DiskItem(int id, int size) {
        this.id = id;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
