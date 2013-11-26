package ch.minifig.monitoring.jmx;

public interface MemoryAllocationMBean {
    public long getTotalAllocatedBytes();

    public boolean isSupported();
}
