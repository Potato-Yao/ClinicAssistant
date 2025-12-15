import com.potato.kernel.External.FurmarkHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FurmarkHelperTest {
    @Test
    void test() throws IOException, InterruptedException {
        FurmarkHelper helper = new FurmarkHelper();
        helper.runTest();

        Thread.sleep(10 * 1000);

        helper.stopTest();
    }
}
