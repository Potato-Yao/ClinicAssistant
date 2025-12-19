package com.potato.kernel.Utils;

import java.io.IOException;

/**
 * contains useful methods about processes
 */
public class ProcessUtil {
    /**
     * kill a process forcibly
     * @param process
     */
    public static void forceKillProcess(Process process) {
        long pid = process.pid();
        ProcessBuilder killerBuilder = new ProcessBuilder("kill", Long.toString(pid));
        try {
            killerBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        process.destroy();
    }
}
