package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.utils.rest.base.HttpTask;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class RestTask<In extends Message, Out extends Response> extends HttpTask<In, Out> {

    protected Map<String, Object> body;
    protected HttpClient client;

    protected Consumer<String> responseListener;

    public RestTask(String baseUri, String requestUri, Object...params) {
        super(baseUri, requestUri, params);
    }

    public RestTask(String baseUri, String requestUri) {
        this(baseUri, requestUri, new Object[0]);
    }

    public RestTask() {
        this("", "");
    }

    public RestTask(String baseUri, String requestUri, Object[] params, Consumer<String> responseListener) {
        this(baseUri, requestUri, params);
        this.responseListener = responseListener;
    }

    public void setBody(Map<String, Object> data) {
        this.body = data;
    }

    public Map<String, Object> getBody() {
        return this.body;
    }

    protected HttpClient getClient() {
        if (this.client == null) {
            this.client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofMillis(500))
                    .build();
        }
        return this.client;
    }

    public RestTask setClient(HttpClient client) {
        this.client = client;
        return this;
    }

    public HttpTask addResponseListener(Consumer<String> response) {
        this.responseListener = response;
        return this;
    }

    protected Consumer<String> getResponseListener() {
        return this.responseListener;
    }

    protected Map<String, String> createHeaderFrom(String token) {
        Map<String, String> httpHeaders = new HashMap<>();
        //CHECK token empty or null after prefix:
        if (token == null || token.trim().isEmpty()) return httpHeaders;
        String prefix = prefix();
        //Get rid of prefix in either-case:
        token = parseToken(token);
        //CHECK again token empty or null after prefix:
        if (token == null || token.trim().isEmpty()) return httpHeaders;
        httpHeaders.put(authorizationKey(), prefix + token);
        return httpHeaders;
    }
}
