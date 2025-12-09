import com.potato.Software.StressTestUtil;
import com.potato.Software.StressTestUtilBuilder;
import com.potato.Software.TestState;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class StressTestUtilTest {
    @Test
    void test() throws IOException, InterruptedException {
        StressTestUtilBuilder builder = new StressTestUtilBuilder();
        StressTestUtil util = builder.cpuTest(true).gpuTest(true).totalPower(100).build();

        util.runStressTest(7);

        System.out.println(util.isRunning());
        while (util.isRunning()) {
            TestState[] testStates = util.getTestStates();
            for (TestState testState : testStates) {
                if (testState == null) {
                    continue;
                }

                System.out.println("Hardware: " + testState.getHardware() + " with status: " + testState.getTestStatus() + " with info: " + testState.getInfo());
            }

            Thread.sleep(1000);
        }

        System.out.println("Done!");
        System.exit(0);
    }
}
