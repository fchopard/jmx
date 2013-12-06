package ch.minifig.monitoring.jmx;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class MemoryAllocation implements MemoryAllocationMBean {
    public static final ObjectName DEFAULT_NAME = Jmx.newObjectName("ch.minifig.monitoring.jmx:type=MemoryAllocation");

    private static final ObjectName THREADING_MBEAN = Jmx.newObjectName("java.lang:type=Threading");

    private final Jmx jmx = new Jmx(ManagementFactory.getPlatformMBeanServer());
    private long totalAllocatedBytes;
    private long sampleTimeStamp;
    private long lastTotalAllocatedBytes;
    private long lastSampleTimeStamp;

    @Override
    public synchronized long getTotalAllocatedBytes() {
        if (getNewSampleIfSupported()) {
            return -1;
        }

        return totalAllocatedBytes;
    }

    @Override
    public synchronized long getNewlyAllocatedBytes() {
        if (getNewSampleIfSupported()) {
            return -1;
        }

        return totalAllocatedBytes - lastTotalAllocatedBytes;
    }

    @Override
    public synchronized double getAllocatedBytesPerSecond() {
        if (getNewSampleIfSupported()) {
            return -1.0;
        }

        double newlyAllocatedBytes = totalAllocatedBytes - lastTotalAllocatedBytes;
        double timeSplit = sampleTimeStamp - lastSampleTimeStamp;
        return newlyAllocatedBytes / timeSplit * TimeUnit.SECONDS.toMillis(1);
    }

    private boolean getNewSampleIfSupported() {
        if (!isSupported()) {
            return true;
        }

        long now = System.currentTimeMillis();
        if (now - sampleTimeStamp < 1000) {
            // no new sampling during the same second
            return false;
        }
        newSample();
        return false;
    }

    private void newSample() {
        long[] threadIds = getThreadIds();

        long updatedTotalAllocatedBytes = 0;
        for (long threadId : threadIds) {
            long threadAllocatedBytes = jmx.invoke(THREADING_MBEAN, "getThreadAllocatedBytes", new Object[]{threadId}, new String[]{"long"}, Long.class);
            updatedTotalAllocatedBytes += threadAllocatedBytes;
        }

        lastTotalAllocatedBytes = totalAllocatedBytes;
        lastSampleTimeStamp = sampleTimeStamp;
        totalAllocatedBytes = updatedTotalAllocatedBytes;
        sampleTimeStamp = System.currentTimeMillis();
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
