package com.infoworks.utils.jmsq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.queue.QueuedTaskStateListener;
import com.infoworks.tasks.queue.TaskQueue;
import com.infoworks.tasks.stack.TaskCompletionListener;
import com.infoworks.tasks.stack.TaskStack;

import java.util.function.BiConsumer;

public abstract class AbstractJmsQueue implements TaskQueue, QueuedTaskStateListener {

    private BiConsumer<Message, TaskStack.State> callback;
    private TaskCompletionListener listener;

    public abstract ObjectMapper getObjectMapper();
    public abstract void setObjectMapper(ObjectMapper objectMapper);

    protected JmsMessage convert(Task task){
        return JmsMessage.convert(task, getObjectMapper());
    }

    protected JmsMessage convert(Task task, Message error){
        return JmsMessage.convert(task, error, getObjectMapper());
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
