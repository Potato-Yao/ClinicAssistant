package com.potato.Software;

import com.sun.source.tree.WhileLoopTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Regedit {
    public Regedit() {}

    // todo reg query runs too fucking slow
    private boolean queryReg(String keyPath, String keyName) throws IOException {
        ProcessBuilder processBuilder;
        if (keyName == null) {
            processBuilder = new ProcessBuilder("reg", "query", keyPath, "/s");
        } else {
            processBuilder = new ProcessBuilder("reg", "query", keyPath, "/s", "/f", keyName);
        }

        Process process = processBuilder.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            // key path doesn't exist
            if (line.contains("ERROR: The system was unable to find the specified registry key or value.")) {
                return false;
            }

            // an example: End of search: 1 match(es) found.
            if (line.contains("End of search:")) {
                String[] parts = line.split(" ");
                if (parts[3].equals("0")) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        return false;
    }

    private void deleteReg(String key) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder("reg", "delete", key, "/f");
        Process process = processBuilder.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
//            if (line.contains("The operation completed successfully.")) {
//                break;
//            }

            if (line.contains("ERROR: The system was unable to find the specified registry key or value.")) {
                throw new RuntimeException("Key not found: " + key);
            }
        }
    }

    public boolean queryHKCR(String keyPath, String keyName) throws IOException {
        return queryReg("HKCR\\" + keyPath, keyName);
    }

    public boolean queryHKLM(String keyPath, String keyName) throws IOException {
        return queryReg("HKLM\\" + keyPath, keyName);
    }

    public boolean queryHKCU(String keyPath, String keyName) throws IOException {
        return queryReg("HKCU\\" + keyPath, keyName);
    }

    public void deleteHKCR(String key) throws IOException {
        deleteReg("HKCR\\" + key);
    }

    public void deleteHKLM(String key) throws IOException {
        deleteReg("HKLM\\" + key);
    }

    public void deleteHKCU(String key) throws IOException {
        deleteReg("HKCU\\" + key);
    }
}
