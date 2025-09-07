package com.infoworks.utils.eventq;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.queue.AbstractQueueManager;
import com.infoworks.tasks.queue.QueuedTaskStateListener;
import com.infoworks.objects.MessageParser;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class EventQueueManager extends AbstractQueueManager {

    protected static Logger LOG = Logger.getLogger(EventQueueManager.class.getSimpleName());
    private QueuedTaskStateListener listener;
    private ExecutorService service;

    public EventQueueManager(ExecutorService service) {
        this.service = service;
    }

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

    @Override
    public void start(Task task, Message message) {
        if (task != null){
            //Call Execute:
            boolean mustAbort = false;
            Future<Message> futureMsg = getService().submit(() -> {
                Message msg = new Message();
                try {
                    msg = task.execute(message);
                } catch (RuntimeException e) {
                    msg.setPayload(String.format("{\"error\":\"%s\", \"status\":500}", e.getMessage()));
                }
                return msg;
            });
            //End Execute:
            Message msg = null;
            try {
                msg = futureMsg.get(task.getTimeoutDuration().toMillis(), TimeUnit.MILLISECONDS);
                if (msg != null) {
                    Map<String, Object> payload = MessageParser.unmarshal(Map.class, msg.getPayload());
                    if (payload != null) {
                        String status = Optional.ofNullable(payload.get("status")).orElse("200").toString();
                        mustAbort = Integer.valueOf(status) == 500;
                    }
                }
            } catch (TimeoutException e) {
                mustAbort = true;
                msg = new Message().setPayload(String.format("{\"error\":\"%s\", \"status\":500}"
                        , e.getMessage() == null
                                ? "TimeoutException During future.get(...)"
                                : e.getMessage()));
            } catch (Exception e) {}
            //
            if (getListener() != null) {
                if (mustAbort) {
                    getListener().abort(task, msg);
                } else {
                    getListener().finished(msg);
                }
            }
        }
    }

    @Override
    public void stop(Task task, Message reason) {
        if (task != null){
            //Call Execute:
            Future<Message> future = getService().submit(() -> {
                Message msg = new Message();
                try {
                    msg = task.abort(reason);
                } catch (RuntimeException e) {
                    msg.setPayload(String.format("{\"error\":\"%s\", \"status\":500}", e.getMessage()));
                }
                return msg;
            });
            //End Execute:
            Message msg = null;
            try {
                msg = future.get(task.getTimeoutDuration().toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                msg = new Message().setPayload(String.format("{\"error\":\"%s\", \"status\":500}"
                        , e.getMessage() == null
                                ? "TimeoutException During future.get(...)"
                                : e.getMessage()));
            } catch (Exception e) {}
            //
            if (getListener() != null) {
                getListener().failed(msg);
            }
        }
    }
}
