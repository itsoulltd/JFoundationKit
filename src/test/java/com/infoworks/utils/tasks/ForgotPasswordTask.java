package com.infoworks.utils.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.AbstractTask;

public class ForgotPasswordTask extends AbstractTask<Message, Response> {

    public ForgotPasswordTask(String email) {
        super(new Property("email", email));
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        //TODO: DO THE BUSINESS LOGIC TO FORGOT PASSWORD:
        String email = getPropertyValue("email").toString();
        //....
        System.out.println("Forget Pass is Successful for " + email);
        //....
        return new Response().setMessage("").setStatus(200);
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        String reason = message != null ? message.getPayload() : "UnknownError! @" + this.getClass().getSimpleName();
        System.out.println("Forget Pass: Abort: " + reason);
        return new Response().setMessage(reason).setStatus(500);
    }
}
