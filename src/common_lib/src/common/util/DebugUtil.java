package common.util;

import java.lang.management.ManagementFactory;

public class DebugUtil {

	public static final boolean DEBUG_MODE = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;

}
