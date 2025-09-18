package com.infoworks.utils.rest.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.objects.MessageParser;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;
import com.infoworks.orm.Row;
import com.infoworks.tasks.ExecutableTask;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class HttpTask<In extends Message, Out extends Response> extends ExecutableTask<In, Out> {
    public static String authorizationKey() {return "Authorization";}
    public static String authorizationValue(String token){return prefix() + parseToken(token);}
    public static String prefix(){return "Bearer ";}
    public static String parseToken(String token){
        if (token == null) return null;
        final String prefix = prefix();
        if (token.trim().startsWith(prefix.trim()) || token.trim().startsWith(prefix.trim().toLowerCase())){
            String pToken = token.trim().substring(prefix.trim().length());
            return pToken.trim();
        }
        return token;
    }

    protected String baseUri;
    protected String requestUri;
    protected Object[] params = new Object[0];
    protected String token;

    public HttpTask(String baseUri, String requestUri, Object...params) {
        this.baseUri = baseUri;
        this.requestUri = requestUri;
        this.params = params;
    }

    public HttpTask setBaseUri(String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    public HttpTask setRequestUri(String requestUri) {
        this.requestUri = requestUri;
        return this;
    }

    public HttpTask setParams(Object...params) {
        this.params = params;
        return this;
    }

    protected Object[] getParams() {
        return this.params;
    }

    public HttpTask setBody(Message body, String token) {
        Map<String, Object> data = (body != null)
                ? body.marshalling(true)
                : null;
        return setBody(data, token);
    }

    public HttpTask setBody(Row row, String token) {
        Map<String, Object> data = (row != null)
                ? row.keyObjectMap()
                : null;
        return setBody(data, token);
    }

    public final HttpTask setBody(Map<String, Object> data, String token) {
        setToken(token);
        setBody(data);
        return this;
    }

    public abstract void setBody(Map<String, Object> body);

    public void setToken(String token) {
        this.token = (token == null) ? "" : token;
    }

    public String getToken() {
        return token;
    }

    protected String getUri() {
        StringBuilder builder = new StringBuilder();
        if (this.baseUri != null && !this.baseUri.isEmpty()) {
            builder.append(this.baseUri.endsWith("/")
                    ? this.baseUri.substring(0, (this.baseUri.length() - 1))
                    : this.baseUri);
        }
        if (this.requestUri != null && !this.requestUri.isEmpty()) {
            builder.append(this.requestUri.startsWith("/")
                    ? this.requestUri
                    : "/" + this.requestUri);
        }
        return builder.toString();
    }

    protected String urlencodedQueryParam(Property...params) {
        if (params == null) return "";
        StringBuilder buffer = new StringBuilder();
        //Separate Paths:
        List<String> pathsBag = new ArrayList<>();
        for (Property query : params) {
            if (query.getValue() != null && !query.getValue().toString().isEmpty()) {
                continue;
            }
            pathsBag.add(query.getKey());
        }
        buffer.append(validatePaths(pathsBag.toArray(new String[0])));
        //Incorporate QueryParams:
        buffer.append("?");
        for (Property query : params){
            if (query.getValue() == null || query.getValue().toString().isEmpty()){
                continue;
            }
            buffer.append(query.getKey()
                    + "="
                    + URLEncoder.encode(query.getValue().toString(), StandardCharsets.UTF_8)  //"UTF-8"
                    + "&");
        }
        String value = buffer.toString();
        value = value.substring(0, value.length()-1);
        return value;
    }

    protected StringBuilder validatePaths(String... params) {
        StringBuilder buffer = new StringBuilder();
        for(String str : Arrays.asList(params)) {
            String trimmed = str.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.length() > 2 && trimmed.endsWith("/"))
                trimmed = trimmed.substring(0, trimmed.length() - 1);

            if(trimmed.startsWith("/"))
                buffer.append(trimmed);
            else
                buffer.append("/" + trimmed);
        }
        return buffer;
    }

    protected <T extends Response> List<T> inflateJson(String json, Class<T> type) throws IOException {
        ObjectMapper mapper = MessageParser.getJsonSerializer();
        if (json != null && !json.isEmpty()){
            if (json.startsWith("{")){
                return Arrays.asList(mapper.readValue(json, type));
            }else if(json.startsWith("[")){
                List result = new ArrayList();
                List items = mapper.readValue(json, ArrayList.class);
                Iterator itr = items.iterator();
                while (itr.hasNext()){
                    Object dts = itr.next();
                    if (dts instanceof Map){
                        T instance = mapper.convertValue(dts, type);
                        result.add(instance);
                    }
                }
                Responses rsList = new Responses(result);
                return (List<T>) Arrays.asList(rsList);
            }
        }
        return (List<T>) Arrays.asList(new Response().setMessage(json));
    }

}
