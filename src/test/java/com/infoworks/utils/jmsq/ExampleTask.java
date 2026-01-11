package com.infoworks.utils.jmsq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.tasks.AbstractTask;

import java.time.LocalDateTime;

//////////////////////////////Example of a minimal Task///////////////////////////
public class ExampleTask extends AbstractTask<Message, Response> {

    //Either override default constructor:
    public ExampleTask() {super();}
    //OR
    //Provide an custom constructor:
    public ExampleTask(String data) {
        this(data, LocalDateTime.now());
    }

    public ExampleTask(String data, LocalDateTime timestamp) {
        super(new Property("data", data), new Property("timestamp", timestamp));
    }

    private ObjectMapper mapper;

    @Override
    protected ObjectMapper getObjectMapper() {
        //Solution: Add Jackson JSR-310 Module. Jackson doesn't know how to (de)serialize java.time.LocalDateTime,
        // because Java 8 time types are not supported out-of-the-box unless you register the JSR-310 module.
        if (mapper == null) {
            mapper = super.getObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        return mapper;
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        String savedData = getPropertyValue("data").toString();
        Object timestamp = getPropertyValue("timestamp");
        //....
        //....
        return new Response().setMessage("data=" + savedData + "; timestamp=" + timestamp.toString()).setStatus(200);
    }

    @Override
    public Response abort(Message message) throws RuntimeException {
        String reason = message != null ? message.getPayload() : "UnknownError! @" + this.getClass().getSimpleName();
        return new Response().setMessage(reason).setStatus(500);
    }
}
