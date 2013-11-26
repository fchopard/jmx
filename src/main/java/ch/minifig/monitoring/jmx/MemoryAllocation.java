package ch.minifig.monitoring.jmx;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;

public class MemoryAllocation implements MemoryAllocationMBean {
    public static final ObjectName DEFAULT_NAME = Jmx.newObjectName("ch.minifig.monitoring.jmx:type=MemoryAllocation");

    private static final ObjectName THREADING_MBEAN = Jmx.newObjectName("java.lang:type=Threading");

    private final Jmx jmx = new Jmx(ManagementFactory.getPlatformMBeanServer());

    @Override
    public long getTotalAllocatedBytes() {
        if (!isSupported()) {
            return -1;
        }

        long[] threadIds = getThreadIds();

        long totalAllocatedBytes = 0;
        for (long threadId : threadIds) {
            long threadAllocatedBytes = jmx.invoke(THREADING_MBEAN, "getThreadAllocatedBytes", new Object[]{threadId}, new String[]{"long"}, Long.class);
            totalAllocatedBytes += threadAllocatedBytes;
        }

        return totalAllocatedBytes;
    }

    @Override
    public boolean isSupported() {
        if (!jmx.isRegistered(THREADING_MBEAN)) {
            return false;
        }

        return jmx.getBooleanAttribute(THREADING_MBEAN, "ThreadAllocatedMemoryEnabled");
    }

    private long[] getThreadIds() {
        CompositeData[] threads = jmx.invoke(THREADING_MBEAN, "dumpAllThreads", new Object[]{Boolean.TRUE, Boolean.TRUE}, new String[]{"boolean", "boolean"}, CompositeData[].class);

        long[] threadIds = new long[threads.length];
        for (int i = 0; i < threads.length; i++) {
            threadIds[i] = (Long) threads[i].get("threadId");
        }

        return threadIds;
    }
}
