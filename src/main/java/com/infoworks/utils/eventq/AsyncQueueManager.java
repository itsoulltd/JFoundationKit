package com.infoworks.utils.eventq;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class AsyncQueueManager extends EventQueueManager {

    protected static Logger LOG = Logger.getLogger(AsyncQueueManager.class.getSimpleName());
    private final Queue<Task> abortQueue;

    public AsyncQueueManager(ExecutorService service) {
        setService(service);
        this.abortQueue = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void start(Task task, Message message) {
        if (task == null) return;
        getService().submit(() -> super.start(task, message));
    }

    @Override
    public void stop(Task task, Message reason) {
        if (task == null) return;
        getService().submit(() -> super.stop(task, reason));
    }

}
