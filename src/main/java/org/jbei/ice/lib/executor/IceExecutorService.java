package org.jbei.ice.lib.executor;

import org.jbei.ice.lib.common.logging.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Hector Plahar
 */
public class IceExecutorService {

    private static final IceExecutorService INSTANCE = new IceExecutorService();
    private ExecutorService pool;

    private IceExecutorService() {
    }

    public static IceExecutorService getInstance() {
        return INSTANCE;
    }

    public void startService() {
        if (pool != null)
            return;

        Logger.info("Starting executor service");
        pool = Executors.newFixedThreadPool(5, r -> {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
    }

    public void stopService() {
        if (pool == null)
            return;

        Logger.info("Shutting down executor service");
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(10, TimeUnit.SECONDS))
                    Logger.info("Executor service did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public void runTask(Task task) {
        if (task == null || pool == null)
            return;

        Logger.info("Adding task to executor service");
        pool.execute(new TaskHandler(task));
    }
}
