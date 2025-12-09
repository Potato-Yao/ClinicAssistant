package com.potato.Software;

import java.io.*;
import java.util.ArrayList;

public class DiskManager {
    private static DiskManager diskManager;

    private boolean hasLoadInterpreter = false;
    private int selectedDiskId = -1;

    private Process diskpartProcess;
    private ArrayList<DiskItem> diskItems = new ArrayList<>();
    private ArrayList<PartitionItem> partitionItems = new ArrayList<>();
    private BufferedWriter dpWriter;
    private BufferedReader dpReader;

    private DiskManager() {
    }

    public static DiskManager getDiskManager() throws IOException {
        if (diskManager == null) {
            diskManager = new DiskManager();
            diskManager.init();
        }
        return diskManager;
    }

    private void init() throws IOException {
        connect();

        updateDisks();
    }

    private String executeCommand(String command) throws IOException {
        if (!hasLoadInterpreter) {
            throw new IOException("DiskManager has not connected to diskpart.");
        }

        dpWriter.write(command);
        dpWriter.newLine();
        dpWriter.flush();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        StringBuilder output = new StringBuilder();
        while (dpReader.ready()) {
            output.append((char) dpReader.read());
        }

        // remove "DISKPART>"
        output.delete(output.length() - 11, output.length());
//        String line;
//        while ((line = dpReader.readLine()) != null) {
//            if (line.isBlank()) {
//                continue;
//            }
//            output.append(line).append("\n");
//
//            if (line.contains("DISKPART>")) {
//                break;
//            }
//        }

        return output.toString().trim();
    }

    private void updateDisks() throws IOException {
        diskItems.clear();

        String re = executeCommand("list disk");
        String[] responses = re.split("\n");
        /*
        sample:
          Disk ###  Status         Size     Free     Dyn  Gpt
          --------  -------------  -------  -------  ---  ---
          Disk 0    Online         1863 GB    50 GB        *
          Disk 1    Online          953 GB  1024 KB        *
         */

        for (int i = 2; i < responses.length; ++i) {
            String[] temp = responses[i].trim().split("\\s+");
            try {
                diskItems.add(new DiskItem(Integer.parseInt(temp[1]), Integer.parseInt(temp[3])));
            } catch (NumberFormatException e) {
                break;
            }
        }
    }

    public void connect() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("diskpart");
        diskpartProcess = pb.start();
        dpWriter = new BufferedWriter(new OutputStreamWriter(diskpartProcess.getOutputStream()));
        dpReader = new BufferedReader(new InputStreamReader(diskpartProcess.getInputStream()));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // consume initial output
        StringBuilder stringBuilder = new StringBuilder();
        while (dpReader.ready()) {
            stringBuilder.append((char) dpReader.read());
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (dpReader.ready()) {
            stringBuilder.append((char) dpReader.read());
        }
//        String line;
//        while ((line = dpReader.readLine()) != null) {
//            if (line.isBlank()) {
//                break;
//            }
////            if (line.contains("DISKPART>")) {
////                break;
////            }
//        }

        hasLoadInterpreter = true;
    }

    public void disconnect() throws IOException {
        if (!hasLoadInterpreter) {
            return;
        }

        diskpartProcess.destroy();
        hasLoadInterpreter = false;
    }

    public ArrayList<DiskItem> getDiskItems() {
        return diskItems;
    }

    public ArrayList<PartitionItem> getPartitionItems() {
        return partitionItems;
    }
}
