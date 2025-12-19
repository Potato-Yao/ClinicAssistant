package com.potato.kernel.Software;

import java.io.IOException;

/**
 * builder for {@code StressTestUtil}
 */
public class StressTestUtilBuilder {
    private final StressTestUtil util;

    public StressTestUtilBuilder() throws IOException {
        this.util = new StressTestUtil();
    }

    public StressTestUtilBuilder cpuTest(boolean needTest) {
        util.setTestCPU(needTest);

        return this;
    }

    public StressTestUtilBuilder gpuTest(boolean needTest) {
        util.setTestGPU(needTest);

        return this;
    }

    public StressTestUtilBuilder totalPower(int power) {
        util.setTotalPower(power);

        return this;
    }

    public StressTestUtil build() {
        return util;
    }
}
