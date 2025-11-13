package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.MessageParser;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.function.Consumer;

public class PutTask extends RestTask {

    public PutTask() {super();}

    public PutTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
    }

    public PutTask(String baseUri, String requestUri, Property[] params, Consumer<Response> response) {
        super(baseUri, requestUri, params, response);
    }

    @Override
    protected HttpRequest prepareRequest(Message message) {
        Map<String, Object> data = getBody();
        String json = MessageParser.printJson(data, getMapperWithJVTimeModule());
        //Prepare request builder:
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(getUri()))
                .PUT(HttpRequest.BodyPublishers.ofString(json));
        //Prepare Http-Headers:
        Map<String, String> headers = getDefaultHeaders();
        headers.forEach(builder::header);
        return builder.build();
    }
}
