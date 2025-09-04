package com.infoworks.utils.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.ExecutableTask;

public class DispatchDeliveryTask extends ExecutableTask<Message, Response> {

    public DispatchDeliveryTask(String message) {
        super(new Property("message", message));
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        String msg = getPropertyValue("message").toString();
        System.out.println(msg + "->" + "Dispatch: Order Delivery");
        return new Response().setStatus(200).setMessage(msg);
    }
}
