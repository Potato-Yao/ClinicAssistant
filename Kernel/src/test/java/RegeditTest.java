import com.potato.Software.Regedit;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

public class RegeditTest {
    Regedit regedit = new Regedit();

    @Test
    void queryTest() throws IOException {
        Instant s = Instant.now();

        assert (regedit.queryHKCR("CLSID", "{352EC2B7-8B9A-11D1-B8AE-006008059382}"));  // Shell Application Manager
        assert (!regedit.queryHKCR("CLSID", "{3d09c1ca-2bcc-40b7-b9bb-3f3ec143a87bbbbbb}"));

        Instant e = Instant.now();
        System.out.println("Time taken: " + (e.getEpochSecond() - s.getEpochSecond()) + " seconds");
    }

    @Test
    void deleteTest() {
        try {
            regedit.deleteHKCR("CLSID\\TESTKEYS\\TESTKEY1");
        } catch (IOException e) {
        }
    }
}
