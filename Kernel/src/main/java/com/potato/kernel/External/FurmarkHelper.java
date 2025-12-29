package com.potato.kernel.External;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.potato.kernel.Utils.ProcessUtil.forceKillProcess;

/**
 * helper for running GPU burn test
 * <p>
 * the principle is call external tool furmark.exe
 */
public class FurmarkHelper {
    private File exe;
    private Process runningProcess;

    public FurmarkHelper() {
        Path toolsDir = ExternalTools.resolveToolsDir();
        File exe = new File(toolsDir.toFile(), "./Furmark_win64/furmark.exe");
        this.exe = exe;
    }

    public void runTest() throws IOException {
        if (runningProcess != null) {
            runningProcess.destroy();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(exe.getAbsolutePath(), "--hpgfx", "1", "--demo", "furmark-gl");
        this.runningProcess = processBuilder.start();
    }

    public void stopTest() {
        if (runningProcess != null) {
            forceKillProcess(runningProcess);
            runningProcess = null;
        }
    }
}
