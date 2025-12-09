import com.potato.External.CPUBurnHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CPUBurnHelperTest {
    @Test
    void test() throws IOException, InterruptedException {
        CPUBurnHelper helper = new CPUBurnHelper();
        helper.runTest();

        Thread.sleep(10 * 1000);

        helper.stopTest();
    }
}
