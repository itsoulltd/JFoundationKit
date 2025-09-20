package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;

import java.util.function.Consumer;

public class PostTask extends RestTask<Message, Response> {

    public PostTask() {super();}

    public PostTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
    }

    public PostTask(String baseUri, String requestUri, Property[] params, Consumer<String> response) {
        super(baseUri, requestUri, params, response);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        LOG.info(getUri());
        return new Responses().setStatus(500);
    }
}
