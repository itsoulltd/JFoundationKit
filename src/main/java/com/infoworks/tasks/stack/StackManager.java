package com.infoworks.tasks.stack;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;

import java.util.concurrent.TimeUnit;

public interface StackManager extends AutoCloseable {

    enum State{
        Forward,
        Backward
    }

    void start(Task task, Message message);
    void stop(Task task, Message reason);
    void terminateRunningTasks(long timeout, TimeUnit unit);
}
