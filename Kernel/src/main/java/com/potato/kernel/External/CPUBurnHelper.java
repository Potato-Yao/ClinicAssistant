package com.potato.kernel.External;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CPUBurnHelper {
    private File exe;
    private Process runningProcess;

    public CPUBurnHelper() {
        File projectRoot = new File(System.getProperty("user.dir"));
        File exe = new File(projectRoot, "../ExternalTools/cpuburn/cpuburn.exe");
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
            runningProcess.destroyForcibly();
            long pid = runningProcess.pid();

            try {
                new ProcessBuilder("taskkill", "/f", "/t", "/pid", String.valueOf(pid)).start().waitFor(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            runningProcess = null;
        }
    }
}
