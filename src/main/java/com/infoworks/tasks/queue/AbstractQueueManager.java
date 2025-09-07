package com.infoworks.tasks.queue;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;

public abstract class AbstractQueueManager implements QueueManager {

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
            } catch (Exception e) {
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
            } catch (Exception e) {
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
