package com.infoworks.tasks.stack;

import com.infoworks.objects.Message;

public interface TaskCompletionListener {
    void failed(Message reason);
    void finished(Message result);
}
