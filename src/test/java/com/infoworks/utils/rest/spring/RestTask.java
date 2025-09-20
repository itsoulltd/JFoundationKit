package com.infoworks.utils.rest.spring;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.utils.rest.base.HttpTask;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Consumer;

public abstract class RestTask<In extends Message, Out extends Response> extends HttpTask<In, Out> {

    protected HttpEntity body;
    protected RestTemplate template;

    protected Consumer<String> responseListener;

    public RestTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
    }

    public RestTask(String baseUri, String requestUri) {
        this(baseUri, requestUri, new Property[0]);
    }

    public RestTask() {
        this("", "");
    }

    public RestTask(String baseUri, String requestUri, Property[] params, Consumer<String> responseListener) {
        this(baseUri, requestUri, params);
        this.responseListener = responseListener;
    }

    public void setBody(Map<String, Object> data) {
        this.body = new HttpEntity(data, createAuthHeader(getToken()));
    }

    public HttpEntity getBody() {
        if (this.body == null) {
            return new HttpEntity(null, createAuthHeader(getToken()));
        }
        return this.body;
    }

    protected RestTemplate getTemplate() {
        if (this.template == null) {
            this.template = new RestTemplate();
        }
        return this.template;
    }

    public RestTask setTemplate(RestTemplate template) {
        this.template = template;
        return this;
    }

    public HttpTask addResponseListener(Consumer<String> response) {
        this.responseListener = response;
        return this;
    }

    protected Consumer<String> getResponseListener() {
        return this.responseListener;
    }

    protected final HttpHeaders createAuthHeader(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        //CHECK token empty or null after prefix:
        if (token == null || token.trim().isEmpty()) return httpHeaders;
        String prefix = prefix();
        //Get rid of prefix in either-case:
        token = parseToken(token);
        //CHECK again token empty or null after prefix:
        if (token == null || token.trim().isEmpty()) return httpHeaders;
        httpHeaders.set(HttpHeaders.AUTHORIZATION, prefix + token);
        return httpHeaders;
    }
}
