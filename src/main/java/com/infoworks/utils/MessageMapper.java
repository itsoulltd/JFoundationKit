package com.infoworks.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class MessageMapper {

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

    public static <P> P unmarshal(Class<P> type, String payload) throws IOException {
        return internalUnmarshal(type, payload);
    }

    public static <P> P unmarshal(TypeReference<P> type, String payload) throws IOException {
        return internalUnmarshal(type, payload);
    }

    private static <P> P internalUnmarshal(Object type, String payload) throws IOException {
        if (isValidJson(payload) && type != null){
            final ObjectMapper mapper = getJsonSerializer();
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

    public static <P> String marshal(P object) throws IOException {
        if (object != null){
            final ObjectMapper mapper = getJsonSerializer();
            String value = mapper.writeValueAsString(object);
            return value;
        }
        return null;
    }

    public static <P>  String printString(P object, ObjectMapper mapper) {
        mapper = (mapper == null) ? getJsonSerializer() : mapper;
        if (mapper != null){
            try {
                String json = mapper.writeValueAsString(object);
                return json;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static <P>  String printString(P object) {
        return printString(object, getJsonSerializer());
    }

}
