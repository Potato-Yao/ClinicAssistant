package com.potato.kernel.Utils;

import com.sun.security.auth.module.NTSystem;

public class Admin {
    public static boolean isAdmin() {
        // in case that we will extend to linux
        // although this program will always run on windows
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return false;
        }

        String groups[] = (new NTSystem()).getGroupIDs();
        for (String group : groups) {
            if (group.equals("S-1-5-32-544"))
                return true;
        }
        return false;
    }
}
