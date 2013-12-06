package ch.minifig.monitoring.jmx;

public interface MemoryAllocationMBean {
    public long getTotalAllocatedBytes();

    public long getNewlyAllocatedBytes();

    public double getAllocatedBytesPerSecond();

    public boolean isSupported();
}
