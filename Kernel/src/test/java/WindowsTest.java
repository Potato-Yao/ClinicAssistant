import com.potato.Software.Windows;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class WindowsTest {
    @Test
    void infoTest() throws IOException {
        Windows windows = Windows.getWindows();

        System.out.println("System Name: " + windows.getSystemName());
        System.out.println("System Version: " + windows.getSystemVersion());
        System.out.println("System Model: " + windows.getSystemModel());
        System.out.println("System Type: " + windows.getSystemType());
        System.out.println("Is Activated: " + windows.isActivated());
    }

    @Test
    void winActivateTest() throws IOException {
        Windows windows = Windows.getWindows();
        windows.activateWindows();
    }
}
