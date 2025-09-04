package com.infoworks.tasks.queue;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.stack.TaskStateListener;

public interface QueuedTaskStateListener extends TaskStateListener {
    void abort(Task task, Message error);
}
