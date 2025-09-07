package com.infoworks.utils.transaction;

import com.infoworks.objects.Message;
import com.infoworks.tasks.Task;
import com.infoworks.tasks.stack.StackManager;
import com.infoworks.tasks.stack.StackedTaskStateListener;

import java.util.concurrent.TimeUnit;

public class TransactionStackManager implements StackManager {

    private StackedTaskStateListener listener;

    public TransactionStackManager() {}

    public TransactionStackManager(StackedTaskStateListener listener) {
        this.listener = listener;
    }

    public StackedTaskStateListener getListener() {
        return listener;
    }

    public void setListener(StackedTaskStateListener listener) {
        this.listener = listener;
    }

    @Override @SuppressWarnings("Duplicates")
    public void start(Task task, Message message) {
        //
        if (getListener() != null)
            getListener().before(task, StackManager.State.Forward);
        //Call Execute:
        Message result = null;
        boolean mustAbort = false;
        try {
            result = task.execute(message);
        } catch (Exception e) {
            mustAbort = true;
            result = new Message();
            result.setPayload(String.format("{\"error\":\"%s\", \"status\":500}", e.getMessage()));
        }
        //End Execute:
        if (getListener() != null)
            getListener().after(task, State.Forward);
        //
        if (mustAbort){
            stop(task, result);//ABORT-SEQUENCE:
        }else {
            if (task.next() == null){
                if (getListener() != null) {
                    Message converted = task.convert(result);
                    getListener().finished(converted);//TERMINATION:
                }
            }else{
                Message converted = task.convert(result);
                start(task.next(), converted);//START-NEXT:
            }
        }
        //
    }

    @Override
    public void stop(Task task, Message reason) {
        //
        if (getListener() != null)
            getListener().before(task, State.Backward);
        //Call Abort:
        Message result = null;
        try {
            result = task.abort(reason);
        } catch (Exception e) {}
        //End Abort:
        if (getListener() != null)
            getListener().after(task, State.Backward);
        //
        if (task.next() == null){
            if (getListener() != null)
                getListener().failed(result);
        }else{
            stop(task.next(), result);
        }
        //
    }

    @Override
    public void terminateRunningTasks(long timeout, TimeUnit unit) {}

    @Override
    public void close() throws Exception {
        terminateRunningTasks(0l, TimeUnit.SECONDS);
        this.listener = null;
    }

}
