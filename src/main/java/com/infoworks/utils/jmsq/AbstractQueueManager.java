package com.infoworks.utils.jmsq;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.queue.QueuedTaskStateListener;
import com.infoworks.tasks.stack.StackManager;

public abstract class AbstractQueueManager implements StackManager {

    public abstract QueuedTaskStateListener getListener();
    public abstract void setListener(QueuedTaskStateListener listener);

    @Override
    public void start(Task task, Message message) {
        if (task != null){
            //Call Execute:
            boolean mustAbort = false;
            Message msg = null;
            try {
                msg = task.execute(message);
            } catch (RuntimeException e) {
                mustAbort = true;
                msg = new Message();
                msg.setPayload(String.format("{\"error\":\"%s\", \"status\":500}", e.getMessage()));
            }
            //End Execute:
            if (getListener() != null) {
                if (mustAbort) {
                    getListener().abort(task, msg);
                } else {
                    getListener().finished(msg);
                }
            }
        }
    }

    @Override
    public void stop(Task task, Message reason) {
        if (task != null){
            //Call Execute:
            Message msg = null;
            try {
                msg = task.abort(reason);
            } catch (RuntimeException e) {
                msg = new Message();
                msg.setPayload(String.format("{\"error\":\"%s\", \"status\":500}", e.getMessage()));
            }
            //End Execute:
            if (getListener() != null) {
                getListener().failed(msg);
            }
        }
    }
}
