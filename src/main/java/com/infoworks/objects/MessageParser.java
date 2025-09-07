package com.infoworks.objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class MessageParser {

    public static ObjectMapper getJsonSerializer(){
        ObjectMapper jsonSerializer = new ObjectMapper();
        jsonSerializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonSerializer.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        jsonSerializer.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return jsonSerializer;
    }

    public static boolean isValidJson(String json){
        if (json != null && !json.isEmpty()) {
            return json.trim().startsWith("{") || json.trim().startsWith("[");
        }
        return false;
    }

    private static <P> P internalUnmarshal(Object type, String payload, ObjectMapper mapper) throws IOException {
        if (isValidJson(payload) && type != null){
            mapper = (mapper == null) ? getJsonSerializer() : mapper;
            if (type instanceof TypeReference){
                P obj = mapper.readValue(payload, (TypeReference<P>) type);
                return obj;
            }else{
                P obj = mapper.readValue(payload, (Class<P>) type);
                return obj;
            }
        }
        return null;
    }

    public static <P> P unmarshal(Class<P> type, String payload) throws IOException {
        return internalUnmarshal(type, payload, getJsonSerializer());
    }

    public static <P> P unmarshal(Class<P> type, String payload, ObjectMapper mapper) throws IOException {
        return internalUnmarshal(type, payload, mapper);
    }

    public static <P> P unmarshal(TypeReference<P> type, String payload) throws IOException {
        return internalUnmarshal(type, payload, getJsonSerializer());
    }

    public static <P> P unmarshal(TypeReference<P> type, String payload, ObjectMapper mapper) throws IOException {
        return internalUnmarshal(type, payload, mapper);
    }

    public static <P> String marshal(P object, ObjectMapper mapper) throws IOException {
        if (object != null){
            mapper = (mapper == null) ? getJsonSerializer() : mapper;
            String value = mapper.writeValueAsString(object);
            return value;
        }
        return null;
    }

    public static <P> String marshal(P object) throws IOException {
        return marshal(object, getJsonSerializer());
    }

    public static <P>  String printString(P object) {
        return printString(object, getJsonSerializer());
    }

    public static <P>  String printString(P object, ObjectMapper mapper) {
        return printJson(object, mapper);
    }

    public static <P>  String printJson(P object) {
        return printJson(object, getJsonSerializer());
    }

    public static <P>  String printJson(P object, ObjectMapper mapper) {
        mapper = (mapper == null) ? getJsonSerializer() : mapper;
        try {
            String json = mapper.writeValueAsString(object);
            return json;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";
    }

}
