package com.infoworks.utils.rest.spring;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

public class PutTask extends RestTask<Message, Response> {

    public PutTask() {super();}

    public PutTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
    }

    public PutTask(String baseUri, String requestUri, Property[] params, Consumer<String> response) {
        super(baseUri, requestUri, params, response);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        RestTemplate template = getTemplate();
        try {
            ResponseEntity<String> response = template.exchange(getUri()
                    , HttpMethod.PUT
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
        }
    }
}
