package com.infoworks.utils.eventq;

import com.infoworks.tasks.queue.AbstractQueueManager;
import com.infoworks.tasks.queue.QueuedTaskStateListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class EventQueueManager extends AbstractQueueManager {

    protected static Logger LOG = Logger.getLogger(EventQueueManager.class.getSimpleName());
    private QueuedTaskStateListener listener;
    private ExecutorService service;

    @Override
    public QueuedTaskStateListener getListener() {
        return listener;
    }

    @Override
    public void setListener(QueuedTaskStateListener queuedTaskLifecycleListener) {
        this.listener = queuedTaskLifecycleListener;
    }

    public ExecutorService getService() {
        if (service == null){
            synchronized (this){
                service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2 + 1);
            }
        }
        return service;
    }

    public void setService(ExecutorService service) {
        this.service = service;
    }

    /**
     * Note:
     * Send termination to jms-template for stopping current processing or abandon all active task from
     * e.g. exeQueue, abortQueue, testQueue
     * @param delay
     * @param timeUnit
     */
    @Override
    public void terminateRunningTasks(long delay, TimeUnit timeUnit) {
        try {
            if (!getService().isShutdown()){
                if (delay <= 0l)
                    getService().shutdownNow();
                else {
                    getService().shutdown();
                    getService().awaitTermination(delay, timeUnit);
                }
            }
        } catch (Exception e) {}
        service = null;
    }

    @Override
    public void close() throws Exception {
        terminateRunningTasks(0l, TimeUnit.SECONDS);
        this.listener = null;
    }

}
