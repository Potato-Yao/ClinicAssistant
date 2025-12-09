import com.potato.Software.DiskItem;
import com.potato.Software.DiskManager;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

public class DiskManagerTest {
    @Test
    void test() throws IOException {
        DiskManager manager = DiskManager.getDiskManager();
        ArrayList<DiskItem> diskItems = manager.getDiskItems();
        for (DiskItem diskItem : diskItems) {
            System.out.println("Disk ID: " + diskItem.getId() + " size: " + diskItem.getSize());
        }
    }
}
