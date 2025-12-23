package com.potato.kernel.Utils;

import java.nio.file.Path;
import java.util.ArrayList;

public class FolderItem {
    private ArrayList<Path> files;

    public FolderItem(ArrayList<Path> files) {
        this.files = files;
    }

    public ArrayList<Path> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<Path> files) {
        this.files = files;
    }
}
