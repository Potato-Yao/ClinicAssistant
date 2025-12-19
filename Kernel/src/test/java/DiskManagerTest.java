import com.potato.kernel.Software.DiskItem;
import com.potato.kernel.Software.DiskManager;
import com.potato.kernel.Software.PartitionItem;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;

public class DiskManagerTest {
    @Test
    void getInfoTest() throws IOException {
        DiskManager manager = DiskManager.getDiskManager();
        ArrayList<DiskItem> diskItems = manager.getDiskItems();
        for (DiskItem diskItem : diskItems) {
            System.out.println("Disk ID: " + diskItem.id() + " size: " + diskItem.size());
        }

        ArrayList<PartitionItem> partitionItems = manager.getPartitionItems();
        for (PartitionItem partitionItem : partitionItems) {
            System.out.println("Partition on Disk label: " + partitionItem.getLabel() + " with size: " + partitionItem.getSize() + " and encrypted percentage: " + partitionItem.getBitlockerPercentage());
        }

        manager.disconnect();
    }

    @Test
    void bitlockerTest() throws IOException, InterruptedException {
        DiskManager manager = DiskManager.getDiskManager();
        manager.unlockBitlockerUntilDone("E");  // i have a E: with bitlocker on
    }
}
