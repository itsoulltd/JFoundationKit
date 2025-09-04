package com.infoworks.tasks.queue;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.stack.TaskCompletionListener;

public interface QueuedTaskStateListener extends TaskCompletionListener {
    void abort(Task task, Message error);
}
