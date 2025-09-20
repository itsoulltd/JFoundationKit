package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.function.Consumer;

public class DeleteTask extends RestTask<Message, Response> {

    public DeleteTask() { super(); }

    public DeleteTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
    }

    public DeleteTask(String baseUri, String requestUri, Property[] params, Consumer<String> response) {
        super(baseUri, requestUri, params, response);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        Response outcome = new Responses().setStatus(500);
        LOG.info(getUri());
        //Prepare request builder:
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(getUri()))
                .DELETE();
        //Prepare Http-Headers:
        Map<String, String> headers = createHeaderFrom(getToken());
        headers.put("User-Agent", "JavaHttpClient/11");
        headers.forEach(builder::header);
        //GET:
        try {
            HttpClient client = getClient();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            outcome = new Responses().setStatus(response.statusCode()).setMessage(response.body());
        } catch (IOException | InterruptedException e) {
            outcome.setError(e.getMessage());
        }
        return outcome;
    }
}
