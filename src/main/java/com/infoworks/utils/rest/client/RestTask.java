package com.infoworks.utils.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infoworks.objects.*;
import com.infoworks.orm.Property;
import com.infoworks.utils.rest.base.HttpTask;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class RestTask extends HttpTask<Message, Response> {

    protected Authenticator authenticator;
    protected SSLContext security;
    protected SSLParameters sslParameters;
    protected Map<String, Object> body;
    protected HttpClient client;
    protected HttpClient.Version version;
    protected MediaType contentType = MediaType.JSON;
    protected Consumer<String> responseListener;

    public RestTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
        updateQueryParams(params);
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
        this.body = data;
    }

    public Map<String, Object> getBody() {
        return this.body;
    }

    protected HttpClient getClient() {
        if (this.client == null) {
            HttpClient.Builder builder = HttpClient.newBuilder()
                    .followRedirects(redirectPolicy())
                    .connectTimeout(connectionTimeout());
            builder = (getSecurity() != null) ? builder.sslContext(getSecurity()) : builder;
            builder = (getSslParameters() != null) ? builder.sslParameters(getSslParameters()) : builder;
            builder = (getAuthenticator() != null) ? builder.authenticator(getAuthenticator()) : builder ;
            builder = (getVersion() != null) ? builder.version(getVersion()) : builder;
            this.client = builder.build();
        }
        return this.client;
    }

    protected HttpClient.Redirect redirectPolicy() { return HttpClient.Redirect.NORMAL; }
    protected Duration connectionTimeout() { return Duration.ofMillis(700); }

    public RestTask setClient(HttpClient client) {
        this.client = client;
        return this;
    }

    public SSLContext getSecurity() {
        return security;
    }

    public void setSecurity(SSLContext security) {
        this.security = security;
    }

    public SSLParameters getSslParameters() {
        return sslParameters;
    }

    public void setSslParameters(SSLParameters sslParameters) {
        this.sslParameters = sslParameters;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public HttpClient.Version getVersion() {
        return version;
    }

    public void setVersion(HttpClient.Version version) {
        this.version = version;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    public HttpTask addResponseListener(Consumer<String> response) {
        this.responseListener = response;
        return this;
    }

    protected Consumer<String> getResponseListener() {
        return this.responseListener;
    }

    protected final Map<String, String> createAuthHeader(String token) {
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

    protected Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = createAuthHeader(getToken());
        headers.put("User-Agent", "JavaHttpClient/11");
        if (getContentType() != null)
            headers.put(getContentType().key(), getContentType().value());
        return headers;
    }

    protected ObjectMapper getMapperWithJVTimeModule() {
        //Solution: Add Jackson JSR-310 Module. Jackson doesn't know how to (de)serialize java.time.LocalDateTime,
        // because Java 8 time types are not supported out-of-the-box unless you register the JSR-310 module.
        ObjectMapper mapper = MessageParser.getJsonSerializer();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        LOG.info(getUri());
        return executeRequest(prepareRequest(message));
    }

    protected abstract HttpRequest prepareRequest(Message message);

    protected Response executeRequest(HttpRequest request) {
        Response outcome = new Responses().setStatus(500);
        try {
            HttpClient client = getClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            outcome = new Responses().setStatus(response.statusCode()).setMessage(response.body());
        } catch (IOException | InterruptedException e) {
            outcome.setError(e.getMessage());
        }
        return outcome;
    }
}
