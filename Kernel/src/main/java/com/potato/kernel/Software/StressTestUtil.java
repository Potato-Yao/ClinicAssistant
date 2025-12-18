package com.potato.kernel.Software;

import com.potato.kernel.Config;
import com.potato.kernel.External.CPUBurnHelper;
import com.potato.kernel.External.FurmarkHelper;
import com.potato.kernel.Hardware.CPU;
import com.potato.kernel.Hardware.GPU;
import com.potato.kernel.Hardware.HardwareInfoManager;

import java.io.IOException;
import java.time.Instant;

public class StressTestUtil {
    public static final int AUTO_JUDGE_MODE = Integer.MIN_VALUE;
    public static final int INFINITY_MODE = Integer.MAX_VALUE;

    private FurmarkHelper furmarkHelper;
    private CPUBurnHelper cpuBurnHelper;
    private HardwareInfoManager hardwareInfoManager;

    /*
    0 -> cpu temperature info
    1 -> gpu temperature info
    2 -> power info
     */
    private TestState[] testStates = new TestState[16];
    private int totalPower = -1;
    private volatile boolean isRunning = false;
    private boolean testCPU = true;
    private boolean testGPU = true;

    private Instant testStartTime;
    private Instant nowTime;

    /*
    0 -> data collect time(in epoch seconds)
    1 -> cpu temperature 1min before
    2 -> gpu temperature 1min before
    3 -> cpu temperature 30s before
    4 -> gpu temperature 30s before
     */
    private double[] referenceValues = new double[16];

    public StressTestUtil() throws IOException {
        this.furmarkHelper = new FurmarkHelper();
        this.cpuBurnHelper = new CPUBurnHelper();
        this.hardwareInfoManager = HardwareInfoManager.getHardwareInfoManager();
    }

    /**
     * run stress test, give feedback by hardware info automatically
     *
     * @param remainTimeSeconds the remain time of stress test.
     *                          if remainTimeSeconds is set to AUTO_JUDGE_MODE,
     *                          the stress test will keep running until it judges the test shows a clear ok or warning signal
     * @throws IOException
     */
    public void runStressTest(int remainTimeSeconds) throws IOException {
        if (isRunning) {
            return;
        }
        isRunning = true;
        testStartTime = Instant.now();

        if (testGPU) {
            furmarkHelper.runTest();
        }

        if (testCPU) {
            cpuBurnHelper.runTest();
        }

        Thread thread = new Thread(() -> {
            long endTime = System.currentTimeMillis() + remainTimeSeconds * 1000L;

            while (true) {
                try {
                    Thread.sleep(Config.HARDWARE_INFO_SEEK_RATE);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    update();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (remainTimeSeconds == AUTO_JUDGE_MODE) {
                    if (judgeToStop()) {
                        break;
                    }
                } else if (remainTimeSeconds == INFINITY_MODE) {
                    if (testStates[0].getTestStatus() == TestStatus.CRITICAL
                            || testStates[1].getTestStatus() == TestStatus.CRITICAL) {
                        break;
                    }
                } else if (System.currentTimeMillis() > endTime) {
                    break;
                }
            }

            stopStressTest();
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void update() throws IOException {
        nowTime = Instant.now();
        long nowSecond = nowTime.getEpochSecond();

        if (nowSecond - referenceValues[0] > 30) {
            referenceValues[3] = referenceValues[1];
            referenceValues[4] = referenceValues[2];
            referenceValues[1] = hardwareInfoManager.getCpu().getPackageTemperature();
            referenceValues[2] = hardwareInfoManager.getGpu().getTemperature();
            referenceValues[0] = nowSecond;
        }

        judgeTestStates(this.testStates);
    }

    private void judgeTestStates(TestState[] testStates) {
        for (int i = 0; i < testStates.length; i++) {
            testStates[i] = null;
        }

        CPU cpu = hardwareInfoManager.getCpu();
        GPU gpu = hardwareInfoManager.getGpu();

        testStates[0] = new TestState(cpu, null, null);
        testStates[1] = new TestState(gpu, null, null);
        testStates[2] = new TestState(null, null, null);

        if (cpu.getPackageTemperature() > 95) {
            testStates[0].setTestStatus(TestStatus.CRITICAL);
            testStates[0].setInfo("CPU package temperature is dangerously high, stop the stress test immediately and check the cooling system.");
        } else if (cpu.getPackageTemperature() > 90) {
            testStates[0].setTestStatus(TestStatus.WARNING);
            testStates[0].setInfo("CPU package temperature is too high, turn down the stress test and check the cooling system.");
        } else {
            testStates[0].setTestStatus(TestStatus.NORMAL);
            testStates[0].setInfo("CPU package temperature is OK.");
        }

        if (gpu.getTemperature() > 100) {
            testStates[1].setTestStatus(TestStatus.CRITICAL);
            testStates[1].setInfo("GPU package temperature is dangerously high, stop the stress test immediately and check the cooling system.");
        } else if (cpu.getPackageTemperature() > 95) {
            testStates[1].setTestStatus(TestStatus.WARNING);
            testStates[1].setInfo("GPU package temperature is too high, turn down the stress test and check the cooling system.");
        } else {
            testStates[1].setTestStatus(TestStatus.NORMAL);
            testStates[1].setInfo("GPU package temperature is OK.");
        }

        long durationTime = nowTime.getEpochSecond() - testStartTime.getEpochSecond();
        double expectedPower = totalPower * 0.8;

        // by experience, power of cpu and gpu will be stable after 10 seconds of stress test
        if (durationTime > 10) {
            if (cpu.getPower() + gpu.getPower() < expectedPower * 0.6) {
                testStates[2].setTestStatus(TestStatus.CRITICAL);
                testStates[2].setInfo("The total power is too low! Check the cooling system.");
            } else if (cpu.getPower() + gpu.getPower() < expectedPower * 0.8) {
                testStates[2].setTestStatus(TestStatus.CRITICAL);
                testStates[2].setInfo("The total power is low! Check system power mode and charger.");
            } else {
                testStates[2].setTestStatus(TestStatus.NORMAL);
                testStates[2].setInfo("The total power is OK.");
            }
        } else {
            testStates[2].setTestStatus(TestStatus.NORMAL);
            testStates[2].setInfo("Waiting for power data be stable");
        }
    }

    /**
     * rule to judge whether to stop the stress test:
     * 1. already run for at least 3 min
     * 2. temperature and power states are ok
     * 3. temperature and power states have been stable, which means the values last in [-0.05, 0.05] percentage period for at least 1 min
     * otherwise, the test will keep running until the temperature is in critical state or has run for 10mins but still in an unstable state
     *
     * @return
     */
    private boolean judgeToStop() {
        if (nowTime.getEpochSecond() - testStartTime.getEpochSecond() < 3 * 60) {
            return false;
        }

        if (testStates[0].getTestStatus() != TestStatus.NORMAL ||
                testStates[1].getTestStatus() != TestStatus.NORMAL ||
                (testStates[2] != null && testStates[2].getTestStatus() != TestStatus.NORMAL)) {
            return false;
        }

        double referenceCPUTemperature = (referenceValues[1] + referenceValues[3]) / 2;
        double referenceGPUTemperature = (referenceValues[2] + referenceValues[4]) / 2;
        CPU cpu = hardwareInfoManager.getCpu();
        GPU gpu = hardwareInfoManager.getGpu();

        if (cpu.getPackageTemperature() / referenceCPUTemperature > 1.05 ||
                cpu.getPackageTemperature() / referenceGPUTemperature < 0.95 ||
                gpu.getTemperature() / referenceGPUTemperature > 1.05 ||
                gpu.getTemperature() / referenceGPUTemperature < 0.95) {
            return false;
        }

        return true;
    }

    public void stopStressTest() {
        if (!isRunning) {
            return;
        }

        if (testCPU) {
            cpuBurnHelper.stopTest();
        }

        if (testGPU) {
            furmarkHelper.stopTest();
        }

        isRunning = false;
        testStartTime = null;
    }

    public TestState[] getTestStates() {
        return testStates;
    }

    public void setTestCPU(boolean testCPU) {
        this.testCPU = testCPU;
    }

    public void setTestGPU(boolean testGPU) {
        this.testGPU = testGPU;
    }

    public void setTotalPower(int totalPower) {
        this.totalPower = totalPower;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
