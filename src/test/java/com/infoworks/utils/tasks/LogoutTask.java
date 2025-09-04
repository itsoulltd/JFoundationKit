package com.infoworks.utils.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.AbstractTask;

public class LogoutTask extends AbstractTask<Message, Response> {

    public LogoutTask(String token) {
        super(new Property("token", token));
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        //TODO: DO THE BUSINESS LOGIC TO LOGOUT:
        String token = getPropertyValue("token").toString();
        //....
        System.out.println("Logout is Successful for " + token);
        //....
        return new Response().setMessage("").setStatus(200);
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        String reason = message != null ? message.getPayload() : "UnknownError! @" + this.getClass().getSimpleName();
        return new Response().setMessage(reason).setStatus(500);
    }
}
