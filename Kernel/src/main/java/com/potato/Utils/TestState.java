package com.potato.Utils;

import com.potato.Hardware.HardwareItem;

public class TestState {
    private HardwareItem hardware;
    private TestStatus testStatus;
    private String info;

    public TestState(HardwareItem hardware, TestStatus testStatus, String info) {
        this.hardware = hardware;
        this.testStatus = testStatus;
        this.info = info;
    }

    public HardwareItem getHardware() {
        return hardware;
    }

    public TestStatus getTestStatus() {
        return testStatus;
    }

    public String getInfo() {
        return info;
    }

    public void setTestStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}

enum TestStatus {
    NORMAL,
    WARNING,
    CRITICAL
}
