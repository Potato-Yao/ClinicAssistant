package com.potato.External;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FurmarkHelper {
    private File exe;
    private Process runningProcess;

    public FurmarkHelper() {
        File projectRoot = new File(System.getProperty("user.dir"));
        File exe = new File(projectRoot, "../ExternalTools/Furmark_win64/furmark.exe");
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
            long pid = runningProcess.pid();
            runningProcess.destroyForcibly();

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
