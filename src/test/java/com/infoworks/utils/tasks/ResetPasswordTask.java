package com.infoworks.utils.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.AbstractTask;

public class ResetPasswordTask extends AbstractTask<Message, Response> {

    public ResetPasswordTask(String token, String oldPass, String newPass) {
        super(new Property("token", token), new Property("oldPass", oldPass), new Property("newPass", newPass));
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        //TODO: DO THE BUSINESS LOGIC TO RESET PASSWORD:
        String token = getPropertyValue("token").toString();
        String oldPass = getPropertyValue("oldPass").toString();
        String newPass = getPropertyValue("newPass").toString();
        //....
        System.out.println("Reset Pass is Successful for " + token);
        //....
        return new Response().setMessage("").setStatus(200);
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        String reason = message != null ? message.getPayload() : "UnknownError! @" + this.getClass().getSimpleName();
        return new Response().setMessage(reason).setStatus(500);
    }
}
