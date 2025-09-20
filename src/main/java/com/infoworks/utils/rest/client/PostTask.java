package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.MessageParser;
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
        Response outcome = new Responses().setStatus(500);
        LOG.info(getUri());
        Map<String, Object> data = getBody();
        String json = MessageParser.printJson(data, getMapperWithJVTimeModule());
        //Prepare request builder:
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(getUri()))
                .POST(HttpRequest.BodyPublishers.ofString(json));
        //Prepare Http-Headers:
        Map<String, String> headers = createHeaderFrom(getToken());
        headers.put("User-Agent", "JavaHttpClient/11");
        headers.put("Content-Type", "application/json");
        headers.forEach(builder::header);
        //POST:
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
