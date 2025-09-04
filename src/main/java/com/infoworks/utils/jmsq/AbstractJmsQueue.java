package com.infoworks.utils.jmsq;

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

    protected JmsMessage convert(Task task){
        //Defined:JmsMessage Protocol
        JmsMessage jmsMessage = new JmsMessage()
                .setTaskClassName(task.getClass().getName())
                .setMessageClassName(Message.class.getName());
        if (task.getMessage() != null) {
            jmsMessage.setMessageClassName(task.getMessage().getClass().getName())
                    .setPayload(task.getMessage().toString());
        }
        return jmsMessage;
    }

    protected JmsMessage convert(Task task, Message error){
        //Defined:JmsMessage Protocol
        JmsMessage jmsMessage = convert(task)
                .setErrorClassName(Message.class.getName());
        if (error != null){
            jmsMessage.setErrorClassName(error.getClass().getName())
                    .setErrorPayload(error.toString());
        }
        return jmsMessage;
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
