package com.potato.kernel.External;

import com.potato.kernel.Utils.FolderItem;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class OPHelper {
    private static OPHelper opHelper;

    private HashMap<String, FolderItem> toolMap = new HashMap<>();

    private OPHelper() {
        Path toolsDir = ExternalTools.resolveToolsDir();
        File folder = new File(toolsDir.toFile(), "./CLINIC_OP/");
        File[] dirs = folder.listFiles();
        if (dirs == null) {
            return;
        }

        for (File dir : dirs) {
            if (!dir.isDirectory()) {
                continue;
            }

            ArrayList<Path> tools = new ArrayList<>();
            File[] files = dir.listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".exe")) {
                    tools.add(file.toPath());
                }
            }
            toolMap.put(dir.getName(), new FolderItem(tools));
        }
    }

    public static OPHelper getOpHelper() {
        if (opHelper == null) {
            opHelper = new OPHelper();
        }
        return opHelper;
    }

    public HashMap<String, FolderItem> getToolMap() {
        return toolMap;
    }
}
