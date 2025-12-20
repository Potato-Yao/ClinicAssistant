import com.potato.kernel.Software.StressTestUtil;
import com.potato.kernel.Software.StressTestUtilBuilder;
import com.potato.kernel.Software.TestState;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class StressTestUtilTest {
    @Test
    void test() throws IOException, InterruptedException {
        StressTestUtilBuilder builder = new StressTestUtilBuilder();
        StressTestUtil util = builder.cpuTest(true).totalPower(100).build();

        util.runStressTest(15);

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
