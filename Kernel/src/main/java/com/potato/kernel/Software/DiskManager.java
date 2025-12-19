package com.potato.kernel.Software;

import com.potato.kernel.Config;

import java.io.*;
import java.util.ArrayList;

/**
 * for getting info about disk and disks' partitions, providing some operations on them
 */
public class DiskManager {
    private static DiskManager diskManager;

    private boolean hasLoadInterpreter = false;

    private Process diskpartProcess;
    private ArrayList<DiskItem> diskItems = new ArrayList<>();
    private ArrayList<PartitionItem> partitionItems = new ArrayList<>();
    private BufferedWriter dpWriter;
    private BufferedReader dpReader;

    private DiskManager() {
    }

    /**
     * get a disk manager singleton
     *
     * @return
     * @throws IOException when failing to execute the command to get disk info
     */
    public static DiskManager getDiskManager() throws IOException {
        if (diskManager == null) {
            diskManager = new DiskManager();
            diskManager.init();
        }
        return diskManager;
    }

    /**
     * load diskpart interpreter, get initial disk and partition info
     * <p>
     * after init, there will have a thread that updating disk and partition info periodically by {@code config.HARDWARE_INFO_SEEK_RATE}
     *
     * @throws IOException
     */
    private void init() throws IOException {
        connect();

        updateDisks();

        // updater thread, updating partition info by a constant period
        // don't update disk info for two reasons: 1. disk info(its id and size) won't change frequently 2. disk info parser runs slowly since diskpart runs slowly
        Thread updater = new Thread(() -> {
            while (true) {
                try {
                    // only BitLocker info changes frequently
                    updatePartitionItems();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    Thread.sleep(Config.HARDWARE_INFO_SEEK_RATE);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        updater.setDaemon(true);
        updater.start();
    }

    /**
     * all diskpart commands can be executed under its interpreter, they can't be called externally
     * <p>
     * thus, this method is designed for executing command in the interpreter
     *
     * @param command
     * @return
     * @throws IOException the interpreter is not loaded(check if {@code connect(()} has been called), or the command just executed failed
     */
    private String executeDPCommand(String command) throws IOException {
        if (!hasLoadInterpreter) {
            throw new IOException("DiskManager has not connected to diskpart.");
        }

        dpWriter.write(command);
        dpWriter.newLine();
        dpWriter.flush();

        // wait for diskpart to process the command
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

    /**
     * update both disks and partition info
     * <p>
     * note: if you only want to update partition, use {@code updatePartitionItems()} instead, since update disk items is slow
     * <p>
     * note: {@code connect()} will call updating partition info automatically,
     * <p>
     * so unless you are certainly sure you need to call it manually, don't call it
     *
     * @throws IOException
     */
    private void updateDisks() throws IOException {
        updateDiskItems();
        updatePartitionItems();
    }

    /**
     * update disk info
     * <p>
     * if you want to update all thing about disk, use {@code updateDisks()} instead
     * <p>
     * note: this method will not change the items in {@code diskItems}, so it's safe to store references to one of them
     *
     * @throws IOException
     */
    private void updateDiskItems() throws IOException {
        String re = executeDPCommand("list disk");
        String[] responses = re.split("\n");

        if (diskItems.isEmpty()) {
            diskItems = parseDisks(responses);
        } else {
            parseDisks(responses).forEach((diskItem -> {
                for (DiskItem existedItem : diskItems) {
                    if (existedItem.getId() == diskItem.getId()) {
                        existedItem.setSize(diskItem.getSize());
                        return;
                    }
                }
            }));
        }
    }

    /**
     * update partition info
     * <p>
     * note: this method will not change the items in {@code partitionItems}, so it's safe to store references to one of them
     * <p>
     * note: {@code connect()} will call updating partition info automatically,
     * so unless you are certainly sure you need to call it manually, don't call it
     *
     * @throws IOException
     */
    private void updatePartitionItems() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("manage-bde", "-status");
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        if (partitionItems.isEmpty()) {
            partitionItems = parsePartitions(reader);
        } else {
            parsePartitions(reader).forEach((partitionItem -> {
                for (PartitionItem existedItem : partitionItems) {
                    if (existedItem.getLabel().equals(partitionItem.getLabel())) {
                        existedItem.setBitlockerPercentage(partitionItem.getBitlockerPercentage());
                        return;
                    }
                }
            }));
        }
    }

    /**
     * parse diskpart's output to list of {@code DiskItem}
     *
     * @param responses output of diskpart
     * @return
     */
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

    /**
     * parse manage-bde's output to list of {@code PartitionItem}
     *
     * @param reader
     * @return
     * @throws IOException
     */
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
        int currentSize = Config.INT_DEFAULT;
        double currentPercentage = Config.INT_DEFAULT;

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

            if (currentPartition != null && currentSize != Config.INT_DEFAULT && currentPercentage != Config.INT_DEFAULT) {
                partitionItems.add(new PartitionItem(currentSize, currentPartition, currentPercentage));

                currentPartition = null;
                currentSize = Config.INT_DEFAULT;
                currentPercentage = Config.INT_DEFAULT;
            }
        }

        return partitionItems;
    }

    /**
     * start the process to unlock bitlocker, but will not wait until it's done
     *
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

    /**
     * unlock bitlocker, and wait until it's done
     *
     * @param label
     * @throws InterruptedException
     */
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
                double percentage = partitionItems.get(labels.indexOf(label)).getBitlockerPercentage();
                if (percentage == 0) {
                    break;
                }

                try {
                    Thread.sleep(Config.HARDWARE_INFO_SEEK_RATE * 3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        thread.start();
        thread.join();
    }

    /**
     * connect to the necessary tools for disk management
     *
     * @throws IOException
     */
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
        while (dpReader.ready()) {
            dpReader.read();
        }

        // wait for diskpart processing
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        while (dpReader.ready()) {
            dpReader.read();
        }

        hasLoadInterpreter = true;
    }

    /**
     * disconnect to the tools
     */
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
