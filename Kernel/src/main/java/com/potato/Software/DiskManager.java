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

    private String executeDPCommand(String command) throws IOException {
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

        // remove "\n DISKPART>"
        output.delete(output.length() - 11, output.length());

        return output.toString().trim();
    }

    private void updateDisks() throws IOException {
        diskItems.clear();

        String re = executeDPCommand("list disk");
        String[] responses = re.split("\n");
        diskItems = parseDisks(responses);

        ProcessBuilder processBuilder = new ProcessBuilder("manage-bde", "-status");
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        partitionItems = parsePartitions(reader);
    }

    private ArrayList<DiskItem> parseDisks(String[] responses) {
        /*
        sample:
          Disk ###  Status         Size     Free     Dyn  Gpt
          --------  -------------  -------  -------  ---  ---
          Disk 0    Online         1863 GB    50 GB        *
          Disk 1    Online          953 GB  1024 KB        *
         */
        ArrayList<DiskItem> diskItems = new ArrayList<>();

        for (int i = 2; i < responses.length; ++i) {
            String[] temp = responses[i].trim().split("\\s+");
            try {
                diskItems.add(new DiskItem(Integer.parseInt(temp[1]), Integer.parseInt(temp[3])));
            } catch (NumberFormatException e) {
                break;
            }
        }

        return diskItems;
    }

    private ArrayList<PartitionItem> parsePartitions(BufferedReader reader) throws IOException {
        /*
        sample:
        BitLocker Drive Encryption: Configuration Tool version 10.0.22621
        Copyright (C) 2013 Microsoft Corporation. All rights reserved.

        Disk volumes that can be protected with
        BitLocker Drive Encryption:
        Volume D: [Document]
        [Data Volume]

            Size:                 839.01 GB
            BitLocker Version:    None
            Conversion Status:    Fully Decrypted
            Percentage Encrypted: 0.0%
            Encryption Method:    None
            Protection Status:    Protection Off
            Lock Status:          Unlocked
            Identification Field: None
            Automatic Unlock:     Disabled
            Key Protectors:       None Found

        Volume E: [SharedFile]
        [Data Volume]

            Size:                 50.00 GB
            BitLocker Version:    None
            Conversion Status:    Fully Decrypted
            Percentage Encrypted: 0.0%
            Encryption Method:    None
            Protection Status:    Protection Off
            Lock Status:          Unlocked
            Identification Field: None
            Automatic Unlock:     Disabled
            Key Protectors:       None Found

        Volume C: [OS]
        [OS Volume]

            Size:                 926.17 GB
            BitLocker Version:    None
            Conversion Status:    Fully Decrypted
            Percentage Encrypted: 0.0%
            Encryption Method:    None
            Protection Status:    Protection Off
            Lock Status:          Unlocked
            Identification Field: None
            Key Protectors:       None Found
         */
        /*
        what we need to take care:
        Volume D: [Document]
        Size:                 839.01 GB
        Percentage Encrypted: 0.0%
         */
        String currentPartition = null;
        int currentSize = -1;
        double currentPercentage = -1;

        String line;
        ArrayList<PartitionItem> partitionItems = new ArrayList<>();
        // to be aware of missing info, we decide to use state machine
        int state = 0;  // 0: looking for volume, 1: looking for size, 2: looking for percentage
        while ((line = reader.readLine()) != null) {
            String[] lines = line.trim().split("\\s+");
            if (state == 0) {
                if (lines[0].equals("Volume")) {
                    assert lines.length >= 2;
                    currentPartition = lines[1].substring(0, 1);
                    state = 1;
                }
            } else if (state == 1) {
                if (lines[0].equals("Size:")) {
                    assert lines.length == 3;
                    if (lines[2].equals("MB")) {
                        currentSize = Integer.parseInt(lines[1]);
                        state = 2;
                    } else if (lines[2].equals("GB")) {
                        currentSize = (int) (Double.parseDouble(lines[1]) * 1024);
                        state = 2;
                    }
                }
            } else if (state == 2) {
                if (lines[0].equals("Percentage")) {
                    assert lines.length == 3;
                    currentPercentage = Double.parseDouble(lines[2].substring(0, lines[2].length() - 1));

                    state = 0;
                }
            }

            if (currentPartition != null && currentSize != -1 && currentPercentage != -1) {
                partitionItems.add(new PartitionItem(currentSize, currentPartition, currentPercentage));

                currentPartition = null;
                currentSize = -1;
                currentPercentage = -1;
            }
        }

        return partitionItems;
    }

    /**
     * Only start the process to unlock
     * @param label
     */
    public void unlockBitlocker(String label) throws IOException {
        ArrayList<String> labels = new ArrayList<>();
        partitionItems.forEach((partitionItem -> labels.add(partitionItem.getLabel())));
        if (!labels.contains(label)) {
            throw new IllegalArgumentException("Label " + label + " not found.");
        }

        if (partitionItems.get(labels.indexOf(label)).getBitlockerPercentage() == 0) {
            return;
        }

        ProcessBuilder processBuilder = new ProcessBuilder("manage-bde", "-off", label + ":");
        processBuilder.start();
    }

    public void unlockBitlockerUntilDone(String label) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                unlockBitlocker(label);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ArrayList<String> labels = new ArrayList<>();
            partitionItems.forEach((partitionItem -> labels.add(partitionItem.getLabel())));
            while (true) {
                try {
                    updateDisks();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                double percentage = partitionItems.get(labels.indexOf(label)).getBitlockerPercentage();
                if (percentage == 0) {
                    break;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();
        thread.join();
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

    public void disconnect() {
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
