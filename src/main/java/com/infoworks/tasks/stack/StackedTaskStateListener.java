package com.infoworks.tasks.stack;

import com.infoworks.tasks.Task;

public interface StackedTaskStateListener extends TaskCompletionListener{
    void before(Task task, StackManager.State state);
    void after(Task task, StackManager.State state);
}
