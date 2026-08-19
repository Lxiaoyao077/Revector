package org.matrix.vector.util;

import android.os.SystemProperties;
import android.text.TextUtils;

/**
 * Framework-wide constants that do not belong to any one class.
 */
public class Utils {

    /** Whether this is a MIUI/HyperOS build, which needs its own deopt workaround. */
    public static final boolean isMIUI =
            !TextUtils.isEmpty(SystemProperties.get("ro.miui.ui.version.name"));
}
