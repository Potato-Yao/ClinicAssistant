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
            System.out.println("CPU Power " + manager.getCpu().getPower());
            System.out.println("GPU Power " + manager.getGpu().getPower());
            System.out.println("Battery Capacity " + manager.getBattery().getCapacity());
            System.out.println("Battery Remain Capacity " + manager.getBattery().getRemainCapacity());
            System.out.println("Battery Voltage " + manager.getBattery().getVoltage());
            System.out.println("Battery Current " + manager.getBattery().getCurrent());
            System.out.println("Battery Charge/discharge Rate " + manager.getBattery().getRate());
            System.out.println("Battery Designed Capacity " + manager.getBattery().getDesignedCapacity());
            Instant end = Instant.now();
            System.out.println("Time taken: " + (end.toEpochMilli() - begin.toEpochMilli()) + " ms");
            time += (end.toEpochMilli() - begin.toEpochMilli());
            Thread.sleep(1000);
        }
        System.out.println("Average time: " + (time / 10) + " ms");
        manager.close();
    }
}
