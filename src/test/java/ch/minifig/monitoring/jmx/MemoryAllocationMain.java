package ch.minifig.monitoring.jmx;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MemoryAllocationMain {
    public static void main(String[] args) throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, InterruptedException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(new MemoryAllocation(), MemoryAllocation.DEFAULT_NAME);

        int numberOfThreads = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        long[] bytes = new long[1024 * 1024];
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                        }
                    }
                }
            });
        }

        Thread.sleep(10 * 60 * 1000);
        executorService.shutdown();
    }
}
