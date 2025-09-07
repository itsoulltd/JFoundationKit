package com.infoworks.utils.eventq;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.queue.AbstractQueueManager;
import com.infoworks.tasks.queue.QueuedTaskStateListener;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.tasks.stack.TaskCompletionListener;
import com.infoworks.tasks.stack.TaskStack;

import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class EventQueue implements TaskQueue, QueuedTaskStateListener {

    protected static Logger LOG = Logger.getLogger(EventQueue.class.getSimpleName());
    private BiConsumer<Message, TaskStack.State> callback;
    private TaskCompletionListener listener;
    private AbstractQueueManager manager;

    public EventQueue(ExecutorService service, boolean async) {
        this.manager = async
                ? new AsyncEventQueueManager(service)
                : new EventQueueManager(service);
        this.manager.setListener(this);
    }

    public EventQueue(ExecutorService service) {
        this(service, false);
    }

    @Override
    public void abort(Task task, Message error) {
        manager.stop(task, error);
    }

    @Override
    public TaskQueue add(Task task) {
        manager.start(task, null);
        return this;
    }

    @Override
    public TaskQueue cancel(Task task) {
        //No chance of cancellation:
        return this;
    }

    @Override
    public void onTaskComplete(BiConsumer<Message, TaskStack.State> biConsumer) {
        this.callback = biConsumer;
    }

    @Override
    public void onTaskComplete(TaskCompletionListener taskCompletionListener) {
        this.listener = taskCompletionListener;
    }

    @Override
    public void failed(Message message) {
        try {
            if (callback != null){
                callback.accept(message, TaskStack.State.Failed);
            }else if (listener != null){
                listener.failed(message);
            }
        } catch (Exception e) {}
    }

    @Override
    public void finished(Message message) {
        try {
            if (callback != null){
                callback.accept(message, TaskStack.State.Finished);
            }else if (listener != null){
                listener.finished(message);
            }
        } catch (Exception e) {}
    }
}
