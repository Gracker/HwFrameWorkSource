package com.android.server;

import android.os.SystemProperties;
import android.util.Log;
import com.android.server.am.ProcessList;

public final class SmartShrinker {
    private static final boolean DEBUG = false;
    private static volatile boolean ENABLE = false;
    public static final int RECLAIM_ALL_MODE = 2;
    public static final int RECLAIM_ANON_MODE = 1;
    public static final int RECLAIM_INACTIVE_MODE = 4;
    public static final int RECLAIM_SOFT_MODE = 3;
    private static final String TAG = "RMS.SmartShrinker";

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.config.hw_low_ram", false) || SystemProperties.getBoolean("ro.config.hw_smart_shrink", false)) {
            z = true;
        }
        ENABLE = z;
    }

    public static final void init_once(boolean enable) {
        ENABLE = enable;
    }

    public static final void reclaim(int pid, int mode) {
        if (ENABLE) {
            ProcessList.callProcReclaim(pid, mode);
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SmartShrinker is runing in pid =");
            stringBuilder.append(pid);
            stringBuilder.append(" reclaim mode = ");
            stringBuilder.append(mode);
            Log.w(str, stringBuilder.toString());
        }
    }
}
