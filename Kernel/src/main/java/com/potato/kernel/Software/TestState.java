package com.potato.kernel.Software;

import com.potato.kernel.Hardware.HardwareItem;

/**
 * stores how hardware performed in stress test
 */
public class TestState {
    // instance of the hardware item. store this for identify the hardware
    private final HardwareItem hardware;
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
