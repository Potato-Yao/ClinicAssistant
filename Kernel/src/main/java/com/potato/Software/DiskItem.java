package com.potato.Software;

import java.util.ArrayList;

public class DiskItem {
    private int id;
    private int size;
//    private ArrayList<> partitions;

    public DiskItem(int id, int size) {
        this.id = id;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }
}
