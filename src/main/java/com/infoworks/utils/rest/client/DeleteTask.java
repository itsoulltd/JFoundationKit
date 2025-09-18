package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;

import java.util.function.Consumer;

public class DeleteTask extends RestTask<Message, Response> {

    public DeleteTask() { super(); }

    public DeleteTask(String baseUri, String requestUri, Object...params) {
        super(baseUri, requestUri, params);
    }

    public DeleteTask(String baseUri, String requestUri, Consumer<String> response) {
        super(baseUri, requestUri, response);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        /*RestTemplate template = getClient();
        try {
            ResponseEntity<String> response = template.exchange(getUri()
                    , HttpMethod.DELETE
                    , getBody()
                    , String.class
                    , getParams());
            if (getResponseListener() != null)
                getResponseListener().accept(response.getBody());
            return (Response) new Response()
                    .setStatus(200)
                    .setMessage(getUri())
                    .setPayload(response.getBody());
        } catch (Exception e) {
            return new Response()
                    .setStatus(500)
                    .setMessage(getUri())
                    .setError(e.getMessage());
        }*/
        //TODO:
        return new Responses().setStatus(500);
    }
}
