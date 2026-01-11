package com.infoworks.utils.eventq;

import com.infoworks.tasks.queue.AbstractQueueManager;
import com.infoworks.tasks.queue.QueuedTaskStateListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class EventQueueManager extends AbstractQueueManager {

    protected static Logger LOG = Logger.getLogger(EventQueueManager.class.getSimpleName());
    private QueuedTaskStateListener listener;
    private ExecutorService service;
    private ScheduledExecutorService scheduleService;

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

    public ScheduledExecutorService getScheduleService() {
        if (scheduleService == null){
            synchronized (this){
                scheduleService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() / 2 + 1);
            }
        }
        return scheduleService;
    }

    public void setScheduleService(ScheduledExecutorService scheduleService) {
        this.scheduleService = scheduleService;
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
        //Shutdown ExecutorService:
        try {
            if (this.service != null && !this.service.isShutdown()) {
                if (delay <= 0l)
                    this.service.shutdownNow();
                else {
                    this.service.shutdown();
                    this.service.awaitTermination(delay, timeUnit);
                }
            }
        } catch (Exception e) {}
        this.service = null;
        //Shutdown ScheduledExecutorService:
        try {
            if (this.scheduleService != null && !this.scheduleService.isShutdown()) {
                if (delay <= 0l)
                    this.scheduleService.shutdownNow();
                else {
                    this.scheduleService.shutdown();
                    this.scheduleService.awaitTermination(delay, timeUnit);
                }
            }
        } catch (Exception e) {}
        this.scheduleService = null;
    }

    @Override
    public void close() throws Exception {
        terminateRunningTasks(0l, TimeUnit.SECONDS);
        this.listener = null;
    }

}
