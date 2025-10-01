package com.infoworks.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.objects.Message;
import com.infoworks.orm.Property;
import com.infoworks.orm.Row;
import com.infoworks.objects.MessageParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractTask<In extends Message, Out extends Message> implements Task<In, Out> {

    private Task nextTask;
    private Message message;
    private Function<Message, Message> converter;

    public AbstractTask() {
        this.message = new Message();
    }

    public AbstractTask(String message) {
        this();
        this.message.setPayload(message);
    }

    public AbstractTask(String message, Function<Message, Message> converter) {
        this(message);
        this.converter = converter;
    }

    public AbstractTask(Property...properties){
        this();
        Row row = new Row();
        row.setProperties(Arrays.asList(properties));
        try {
            this.message.setPayload(MessageParser.marshal(row.keyObjectMap(), getObjectMapper()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AbstractTask(Property[] properties, Function<Message, Message> converter){
        this(properties);
        this.converter = converter;
    }

    public AbstractTask(In message) {
        this.message = message;
    }

    public AbstractTask(In message, Function<Message, Message> converter) {
        this(message);
        this.converter = converter;
    }

    @Override
    public Task next() {
        return nextTask;
    }

    @Override
    public void linkedTo(Task task) {
        nextTask = task;
    }

    @Override
    public In getMessage() {
        return (In) message;
    }

    public void setMessage(In message) {
        this.message = message;
    }

    @Override
    public Function<Message, Message> getConverter() {
        return converter;
    }

    public void setConverter(Function<Message, Message> converter) {
        this.converter = converter;
    }

    protected Object getPropertyValue(String key) throws RuntimeException{
        String message = getMessage().getPayload();
        if (message != null && MessageParser.isValidJson(message)){
            if(message.startsWith("[")) {throw new RuntimeException("AbstractTask: JsonArray is not supported.");}
            try {
                Map<String, Object> data = MessageParser.unmarshal(new TypeReference<Map<String, Object>>() {}, message);
                Object obj = data.get(key);
                if (obj == null) throw new IOException("AbstractTask: Invalid Property Access");
                return obj;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("AbstractTask: Invalid Property Access");
    }

    protected void updateProperties(Property...properties) throws RuntimeException {
        String payload = getMessage().getPayload();
        if (MessageParser.isValidJson(payload)){
            if (payload.startsWith("[")) { throw new RuntimeException("AbstractTask: JsonArray is not supported."); }
            try {
                Map<String, Object> old = MessageParser.unmarshal(new TypeReference<Map<String, Object>>() {}, payload);
                for (Property property : properties) {
                    old.put(property.getKey(), property.getValue());
                }
                payload = MessageParser.marshal(old, getObjectMapper());
                getMessage().setPayload(payload);
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("AbstractTask: Invalid Property Access");
    }

    /**
     * Override in sub-task if:
     * 1. Global-Configured ObjectMapper Spring-Bean.
     * 2. Need to use with special ObjectMapper Configuration.
     * e.g. Add Jackson JSR-310 Module. Jackson doesn't know how to (de)serialize java.time.LocalDateTime,
     * because Java 8 time types are not supported out-of-the-box unless you register the JSR-310 module.
     * 3. Once initialized, then reused as local-variable.
     * @return
     */
    protected ObjectMapper getObjectMapper() {
        return MessageParser.getJsonSerializer();
    }

}
