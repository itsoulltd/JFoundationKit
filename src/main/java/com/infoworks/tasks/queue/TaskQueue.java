package com.infoworks.tasks.queue;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.stack.TaskCompletionListener;
import com.infoworks.tasks.stack.TaskStack;

import java.util.function.BiConsumer;

public interface TaskQueue {

    TaskQueue add(Task task);
    TaskQueue cancel(Task task);
    void onTaskComplete(BiConsumer<Message, TaskStack.State> onComplete);
    void onTaskComplete(TaskCompletionListener onComplete);
}
