package com.infoworks.utils.rest.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.objects.MessageParser;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;
import com.infoworks.tasks.ExecutableTask;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public abstract class BaseRequest<In extends Message, Out extends Response> extends ExecutableTask<In, Out> {
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
            try {
                buffer.append(query.getKey()
                        + "="
                        + URLEncoder.encode(query.getValue().toString(), "UTF-8")
                        + "&");
            } catch (UnsupportedEncodingException e) {}
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

    protected HttpHeaders createHeaderFrom(String token) {
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
