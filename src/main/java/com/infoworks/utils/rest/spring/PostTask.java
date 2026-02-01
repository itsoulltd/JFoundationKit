package com.infoworks.utils.rest.spring;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
        String uri = getUri();
        LOG.info(uri);
        RestTemplate template = getTemplate();
        try {
            ResponseEntity<String> response = template.exchange(uri
                    , HttpMethod.POST
                    , getBody()
                    , String.class
                    , getParams());
            if (getResponseListener() != null)
                getResponseListener().accept(response.getBody());
            return new Response()
                    .setStatus(200)
                    .setMessage(response.getBody());
        } catch (Exception e) {
            return new Response()
                    .setStatus(500)
                    .setMessage("")
                    .setError(e.getMessage());
        }
    }
}
