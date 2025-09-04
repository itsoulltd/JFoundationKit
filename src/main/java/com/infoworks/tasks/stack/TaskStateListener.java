package com.infoworks.tasks.stack;

import com.infoworks.tasks.Task;

public interface TaskStateListener extends TaskCompletionListener{
    void before(Task task, StackManager.State state);
    void after(Task task, StackManager.State state);
}
