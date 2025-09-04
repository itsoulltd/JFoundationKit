package com.infoworks.utils.tasks;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.AbstractTask;

public class SendEmailTask extends AbstractTask<Message, Response> {

    public SendEmailTask(String sender, String receiver, String body, String templateId) {
        super(new Property("sender", sender)
                , new Property("receiver", receiver)
                , new Property("templateId",templateId)
                , new Property("body", body));
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        //TODO: DO THE BUSINESS LOGIC TO SEND EMAIL:
        String sender = getPropertyValue("sender").toString();
        String receiver = getPropertyValue("receiver").toString();
        String body = getPropertyValue("body").toString();
        String emailTemplateID = getPropertyValue("templateId").toString();
        //....
        System.out.println("Email Has Sent To " + receiver);
        //....
        return new Response().setMessage("").setStatus(200);
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        String reason = message != null ? message.getPayload() : "UnknownError! @" + this.getClass().getSimpleName();
        return new Response().setMessage(reason).setStatus(500);
    }
}
