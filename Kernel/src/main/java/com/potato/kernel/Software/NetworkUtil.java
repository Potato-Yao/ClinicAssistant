package com.potato.kernel.Software;

import java.io.IOException;

public class NetworkUtil {
    private Regedit regedit;

    public NetworkUtil() {
        regedit = new Regedit();
    }

    public void resetNetworkProxy() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("netsh", "winhttp", "reset", "proxy");
        processBuilder.start();
    }

    /**
     * useful for fixing network adapter's code 56 if VMware is not uninstalled properly
     * @throws IOException
     */
    public void deleteVMwareNetBridge() throws IOException {
        if (!regedit.queryHKCR("CLSID", "{3d09c1ca-2bcc-40b7-b9bb-3f3ec143a87b}")) {
            throw new RuntimeException("Can't find VMware Network Bridge registry key");
        }

        regedit.deleteHKCR("CLSID\\{3d09c1ca-2bcc-40b7-b9bb-3f3ec143a87b}");
    }
}
