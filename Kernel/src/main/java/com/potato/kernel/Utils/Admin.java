package com.potato.kernel.Utils;

import com.sun.security.auth.module.NTSystem;

/**
 * contains methods about administrative privileges on Windows
 */
public class Admin {
    /**
     * Check if the program is running under administrative privileges
     * @return only when running on Windows and being under admin privileges, the method returns true
     */
    public static boolean isAdmin() {
        // in case that we will extend to Linux
        // although this program will always run on Windows
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return false;
        }

        String[] groups = (new NTSystem()).getGroupIDs();
        for (String group : groups) {
            if (group.equals("S-1-5-32-544"))
                return true;
        }
        return false;
    }
}
