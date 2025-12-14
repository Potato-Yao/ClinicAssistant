package com.potato.Software;

import java.io.*;

public class Windows {
    private String systemName;
    private String systemVersion;
    private String systemModel;
    private boolean isActivated;
    private SystemType systemType;

    private static Windows windows;

    private Windows() throws IOException {
        parseSystemInfo();
    }

    public static Windows getWindows() throws IOException {
        if (windows == null) {
            windows = new Windows();
        }
        return windows;
    }

    private void parseSystemInfo() throws IOException {
        ProcessBuilder systemInfoPB = new ProcessBuilder("systeminfo");
        Process systemInfoProcess = systemInfoPB.start();
        BufferedReader systemInfoReader = new BufferedReader(new InputStreamReader(systemInfoProcess.getInputStream()));

        /*
        sample
        Host Name:                 POTATOZEPHYRUS
        OS Name:                   Microsoft Windows 11 Pro
        OS Version:                10.0.22631 N/A Build 22631
        OS Manufacturer:           Microsoft Corporation
        OS Configuration:          Standalone Workstation
        OS Build Type:             Multiprocessor Free
        Registered Owner:          N/A
        Registered Organization:   N/A
        Product ID:                00330-80000-00000-00000
        Original Install Date:     9/5/2025, 7:46:36 PM
        System Boot Time:          12/2/2025, 9:45:18 AM
        System Manufacturer:       ASUSTeK COMPUTER INC.
        System Model:              ROG Zephyrus G16 GU605MV_GU605MV
        System Type:               x64-based PC  ### ARM64-based on arm machines ###
        Processor(s):              1 Processor(s) Installed.
                                   [01]: Intel64 Family 6 Model 170 Stepping 4 GenuineIntel ~2300 Mhz
        BIOS Version:              American Megatrends International, LLC. GU605MV.329, 6/6/2025
        Windows Directory:         C:\WINDOWS
        System Directory:          C:\WINDOWS\system32
        Boot Device:               \Device\HarddiskVolume5
        System Locale:             zh-cn;Chinese (China)
        Input Locale:              en-us;English (United States)
        Time Zone:                 (UTC+08:00) Beijing, Chongqing, Hong Kong, Urumqi
        Total Physical Memory:     32,168 MB
        Available Physical Memory: 4,780 MB
        Virtual Memory: Max Size:  71,080 MB
        Virtual Memory: Available: 30,257 MB
        Virtual Memory: In Use:    40,823 MB
        Page File Location(s):     C:\pagefile.sys
        Domain:                    WORKGROUP
        Logon Server:              \\POTATOZEPHYRUS
        Hotfix(s):                 4 Hotfix(s) Installed.
                                   [01]: KB5045935
                                   [02]: KB5027397
                                   [03]: KB5065431
                                   [04]: KB5064743
        Network Card(s):           7 NIC(s) Installed.
                                   [01]: Intel(R) Wi-Fi 6E AX211 160MHz
                                         Connection Name: WLAN
                                         Status:          Media disconnected
                                         ### and others, omitted ###
        Hyper-V Requirements:      A hypervisor has been detected. Features required for Hyper-V will not be displayed.
         */

        String line;
        while ((line = systemInfoReader.readLine()) != null) {
            String[] lines = line.trim().split("\\s+");

            if (lines.length < 3) {
                continue;
            }

            if (lines[0].equals("OS")) {
                if (lines[1].equals("Name:")) {
                    this.systemName = readValue(lines);
                } else if (lines[1].equals("Version:")) {
                    this.systemVersion = readValue(lines);
                }
            }
            if (lines[0].equals("System")) {
                if (lines[1].equals("Model:")) {
                    this.systemModel = readValue(lines);
                } else if (lines[1].equals("Type:")) {
                    String type = readValue(lines);
                    if (type.contains("x64")) {
                        this.systemType = SystemType.x64;
                    } else if (type.contains("ARM64")) {
                        this.systemType = SystemType.arm;
                    } else if (type.contains("x86")) {
                        this.systemType = SystemType.x86;
                    }
                }
            }
        }

        ProcessBuilder slmgrPB = new ProcessBuilder("cscript", "//NoLogo", "C:\\Windows\\System32\\slmgr.vbs", "/xpr");
        Process slmgrProcess = slmgrPB.start();
        BufferedReader slmgrReader = new BufferedReader(new InputStreamReader(slmgrProcess.getInputStream()));

        /*
        sample
        Microsoft (R) Windows Script Host Version 5.812
        Copyright (C) Microsoft Corporation. All rights reserved.

        Windows(R), Professional edition:
            The machine is permanently activated.
         */
        while ((line = slmgrReader.readLine()) != null) {
            if (line.contains("permanently activated")) {
                this.isActivated = true;
                break;
            }
        }
    }

    public void activateWindows() throws IOException {
        if (isActivated) {
            return;
        }

        File projectRoot = new File(System.getProperty("user.dir"));
        File activateTool = new File(projectRoot, "../ExternalTools/win-activate/MAS_AIO.cmd");
        if (!activateTool.exists()) {
            throw new RuntimeException("Activation tool not found, which should located at " + activateTool.getAbsolutePath());
        }

        Process activateToolProcess = new ProcessBuilder(activateTool.getAbsolutePath(), "/HWID").start();
        BufferedWriter toolWriter = new BufferedWriter(new OutputStreamWriter(activateToolProcess.getOutputStream()));

        // the command for activating
        toolWriter.write("1");
        toolWriter.newLine();
        toolWriter.flush();

        try {
            activateToolProcess.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String readValue(String[] lines) {
        assert lines.length >= 3;

        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < lines.length; i++) {
            sb.append(lines[i]).append(" ");
        }

        return sb.toString().trim();
    }

    public String getSystemName() {
        return systemName;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public String getSystemModel() {
        return systemModel;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public SystemType getSystemType() {
        return systemType;
    }
}
