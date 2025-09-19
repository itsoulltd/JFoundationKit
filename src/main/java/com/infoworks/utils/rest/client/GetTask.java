package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;

import java.util.function.Consumer;

public class GetTask extends RestTask<Message, Response> {

    public GetTask() {super();}

    public GetTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
    }

    public GetTask(String baseUri, String requestUri, Property[] params, Consumer<String> response) {
        this(baseUri, requestUri, params);
        addResponseListener(response);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        /*RestTemplate template = getClient();
        try {
            ResponseEntity<String> response = (getParams().length > 0)
                    ? template.exchange(getUri(), HttpMethod.GET, getBody(), String.class, getParams())
                    : template.exchange(getUri(), HttpMethod.GET, getBody(), String.class);
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
        System.out.println(getUri());
        return new Responses().setStatus(500);
    }
}
