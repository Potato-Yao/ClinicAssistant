import com.potato.kernel.Hardware.HardwareInfoManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

public class HardwareInfoManagerTest {
    @Test
    void test() throws IOException, InterruptedException {
        HardwareInfoManager manager = HardwareInfoManager.getHardwareInfoManager();
        long time = 0;
        for (int i = 0; i < 10; i++) {
            Instant begin = Instant.now();
            System.out.println("Package temperature" + manager.getCpu().getPackageTemperature());
            System.out.println("Average temperature" + manager.getCpu().getAverageTemperature());
            System.out.println("GPU Temperature" + manager.getGpu().getTemperature());
            Instant end = Instant.now();
            System.out.println("Time taken: " + (end.toEpochMilli() - begin.toEpochMilli()) + " ms");
            time += (end.toEpochMilli() - begin.toEpochMilli());
            Thread.sleep(1000);
        }
        System.out.println("Average time: " + (time / 10) + " ms");
        manager.close();
    }
}
