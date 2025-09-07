package com.infoworks.utils.eventq;

import com.infoworks.objects.Message;
import com.infoworks.objects.MessageParser;
import com.infoworks.tasks.Task;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SyncQueueManager extends EventQueueManager {

    public SyncQueueManager(ExecutorService service) {
        setService(service);
    }

    @Override
    public void start(Task task, Message message) {
        if (task != null){
            //Call Execute:
            boolean mustAbort = false;
            Future<Message> futureMsg = getService().submit(() -> {
                Message msg = new Message();
                try {
                    msg = task.execute(message);
                } catch (Exception e) {
                    msg.setPayload(String.format("{\"error\":\"%s\", \"status\":500}", e.getMessage()));
                }
                return msg;
            });
            //End Execute:
            Message msg = null;
            try {
                msg = futureMsg.get(task.getTimeoutDuration().toMillis(), TimeUnit.MILLISECONDS);
                if (msg != null) {
                    Map<String, Object> payload = MessageParser.unmarshal(Map.class, msg.getPayload());
                    if (payload != null) {
                        String status = Optional.ofNullable(payload.get("status")).orElse("200").toString();
                        mustAbort = Integer.valueOf(status) == 500;
                    }
                }
            } catch (TimeoutException e) {
                mustAbort = true;
                msg = new Message().setPayload(String.format("{\"error\":\"%s\", \"status\":500}"
                        , e.getMessage() == null
                                ? "TimeoutException During future.get(...)"
                                : e.getMessage()));
            } catch (Exception e) {}
            //
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
            Future<Message> future = getService().submit(() -> {
                Message msg = new Message();
                try {
                    msg = task.abort(reason);
                } catch (Exception e) {
                    msg.setPayload(String.format("{\"error\":\"%s\", \"status\":500}", e.getMessage()));
                }
                return msg;
            });
            //End Execute:
            Message msg = null;
            try {
                msg = future.get(task.getTimeoutDuration().toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                msg = new Message().setPayload(String.format("{\"error\":\"%s\", \"status\":500}"
                        , e.getMessage() == null
                                ? "TimeoutException During future.get(...)"
                                : e.getMessage()));
            } catch (Exception e) {}
            //
            if (getListener() != null) {
                getListener().failed(msg);
            }
        }
    }
}
