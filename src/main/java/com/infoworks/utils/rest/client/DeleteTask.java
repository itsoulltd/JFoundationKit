package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.function.Consumer;

public class DeleteTask extends RestTask {

    public DeleteTask() { super(); }

    public DeleteTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
    }

    public DeleteTask(String baseUri, String requestUri, Property[] params, Consumer<Response> response) {
        super(baseUri, requestUri, params, response);
    }

    @Override
    protected HttpRequest prepareRequest(Message message) {
        //Prepare request builder:
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(getUri()))
                .DELETE();
        //Prepare Http-Headers:
        Map<String, String> headers = createAuthHeader(getToken());
        headers.put("User-Agent", "JavaHttpClient/11");
        headers.forEach(builder::header);
        return builder.build();
    }
}
