package com.infoworks.utils.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.ExecutableTask;

public class CreateOrderJmsTask extends ExecutableTask<Message, Response> {

    //Must need Zero param constructor in Case of JMSTask
    public CreateOrderJmsTask() {}

    public CreateOrderJmsTask(int orderId, String message, boolean nextRandom) {
        super(new Property("message", message)
                , new Property("orderId", orderId)
                , new Property("nextRandom", nextRandom));
    }

    public CreateOrderJmsTask(int orderId, String message) {
        this(orderId, message, true);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        String orderId = getPropertyValue("orderId").toString();
        String msg = getPropertyValue("message").toString() + "[" + orderId + "]";
        boolean nextRandom = (getPropertyValue("nextRandom") != null)
                ? Boolean.parseBoolean(getPropertyValue("nextRandom").toString())
                : true;
        //True will be Success, failed other-wise:
        if (nextRandom) {
            System.out.println(msg + "->" + "Commit: Order In DB [" + Thread.currentThread().getName() + "]");
            return new Response().setStatus(200).setMessage(msg);
        } else {
            throw new RuntimeException(msg + "->" + "Commit-Failed: Order In DB [" + Thread.currentThread().getName() + "]");
        }
    }
}
