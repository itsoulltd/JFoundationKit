package com.infoworks.tasks.stack;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;

import java.util.function.BiConsumer;

public interface TaskStack {

    enum State{
        None,
        Running,
        Finished,
        Failed,
        Canceled
    }

    TaskStack push(Task task);
    void commit(boolean reverse, BiConsumer<Message, State> onComplete);
    void commit(boolean reverse, TaskCompletionListener onComplete);
    void cancel();
}
