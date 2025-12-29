package com.potato.kernel.External;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.potato.kernel.Utils.ProcessUtil.forceKillProcess;

/**
 * helper for running CPU burn test
 * <p>
 * the principle is call external tool cpuburn.exe
 */
public class CPUBurnHelper {
    private File exe;
    private Process runningProcess;

    public CPUBurnHelper() {
        Path toolsDir = ExternalTools.resolveToolsDir();
        File exe = new File(toolsDir.toFile(), "./cpuburn/cpuburn.exe");
        this.exe = exe;
    }

    public void runTest() throws IOException {
        if (runningProcess != null) {
            runningProcess.destroy();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(exe.getAbsolutePath());
        this.runningProcess = processBuilder.start();
    }

    public void stopTest() {
        if (runningProcess != null) {
            forceKillProcess(runningProcess);
            runningProcess = null;
        }
    }
}
