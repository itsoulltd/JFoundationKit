package com.infoworks.objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MessageTest {

    @Test
    public void create() {
        Message message = new Message();
        message.setPayload("My name");
        System.out.println(message.toString());
    }

    @Test
    public void marshal() {
        Message message = new Message();
        message.setPayload("My name");
        //
        Map data = message.marshalling(false);
        Assert.assertTrue(data.isEmpty());
        System.out.println(data);
    }

    @Test
    public void marshalActual() {
        MyMessage message = new MyMessage();
        message.setPayload("My name");
        message.setActivate(true);
        message.setAge(23);
        message.setDob(LocalDateTime.now());
        message.setName("David");
        //
        Map data = message.marshalling(false);
        Assert.assertTrue(!data.isEmpty());
        System.out.println(data);
    }

    @Test
    public void marshalInheritFalse() {
        MyExtendedMessage message = new MyExtendedMessage();
        message.setName("David");
        message.setFirstName("Md.");
        message.setSecondName("Bishop");
        //
        Map data = message.marshalling(false);
        Assert.assertTrue(!data.isEmpty());
        Assert.assertTrue(data.get("name") == null);
        Assert.assertTrue(data.get("firstName") != null);
        Assert.assertTrue(data.get("secondName") != null);
        System.out.println(data);
    }

    @Test
    public void marshalInheritTrue() {
        MyExtendedMessage message = new MyExtendedMessage();
        message.setName("David");
        message.setFirstName("Md.");
        message.setSecondName("Bishop");
        //
        Map data = message.marshalling(true);
        Assert.assertTrue(!data.isEmpty());
        Assert.assertTrue(data.get("name") != null);
        Assert.assertTrue(data.get("firstName") != null);
        Assert.assertTrue(data.get("secondName") != null);
        System.out.println(data);
    }

    @Test
    public void externalizationTest() {
        //Since MyMessage does not have empty public constructor
        // following will raise exception.
        /*MyMessage message = new MyMessage();
        message.setName("Muller");
        message.setAge(23);*/
        //
        MyExtendedMessage message = new MyExtendedMessage();
        message.setName("David");
        message.setAge(30);
        message.setFirstName("Md.");
        message.setSecondName("Bishop");

        /**
         * 3. Important Notes
         * Your Externalizable class must have a public no-arg constructor, or readExternal() will fail.
         * writeExternal and readExternal must write and read fields in the same order.
         * You can simulate edge cases like missing fields or partial reads by corrupting the byte stream in the test.
         */

        // Serialize to byte array
        byte[] data = new byte[0];
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(message);
            data = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Deserialize from byte array
        MyMessage deserialized = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            deserialized = (MyMessage) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Assert values
        Assert.assertEquals(message.getName(), deserialized.getName());
        Assert.assertEquals(message.getAge(), deserialized.getAge());
        System.out.println(MessageParser.printString(deserialized));
    }

    private ObjectMapper getMapperWithDatetimeOption() {
        //Solution: Add Jackson JSR-310 Module. Jackson doesn't know how to (de)serialize java.time.LocalDateTime,
        // because Java 8 time types are not supported out-of-the-box unless you register the JSR-310 module.
        ObjectMapper mapper = MessageParser.getJsonSerializer();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Test
    public void jsonStringTest() {
        MyMessage message = new MyMessage();
        message.setActivate(true);
        message.setAge(23);
        message.setDob(LocalDateTime.now());
        message.setName("David");
        //
        ObjectMapper mapper = getMapperWithDatetimeOption();
        String json = MessageParser.printString(message, mapper);
        Assert.assertTrue(MessageParser.isValidJson(json));
        System.out.println(json);
    }

    @Test
    public void readBackAndForthObject() throws IOException {
        MyExtendedMessage message = new MyExtendedMessage();
        message.setActivate(true);
        message.setAge(23);
        message.setDob(LocalDateTime.now());
        message.setName("David");
        message.setFirstName("fname");
        message.setSecondName("sname");
        //
        ObjectMapper mapper = getMapperWithDatetimeOption();
        String json = MessageParser.printJson(message, mapper);
        Assert.assertTrue(MessageParser.isValidJson(json));
        System.out.println("Expected: " + json);
        //Convert MyExtendedMessage from Json:
        MyExtendedMessage fromJson = MessageParser.unmarshal(MyExtendedMessage.class, json, mapper);
        Assert.assertTrue(fromJson != null);
        Assert.assertEquals(message.getName(), fromJson.getName());
        Assert.assertEquals(message.getDob(), fromJson.getDob());
        Assert.assertEquals(message.getAge(), fromJson.getAge());
        //Revert to json string:
        String revert = MessageParser.marshal(fromJson, mapper);
        Assert.assertTrue(revert != null);
        Assert.assertEquals(json, revert);
        System.out.println("Reverted: " + revert);
    }

    @Test
    public void responseListSortTest() {
        Response response1 = new Response().setStatus(220).setMessage("Cris");
        Response response2 = new Response().setStatus(201).setMessage("Adams");
        Response response3 = new Response().setStatus(420).setMessage("James");
        Response response4 = new Response().setStatus(356).setMessage("Hayes");
        Responses responses = new Responses().setCollections(Arrays.asList(response1, response2, response3, response4));
        System.out.println("Before Sort: ");
        responses.getCollections().forEach(rsp -> System.out.println(rsp.toString()));
        //Test:01
        List<Response> ordered = responses.sort(SortOrder.ASC, "status");
        Assert.assertNotEquals(response1, ordered.get(0));
        System.out.println("After SortBy -> Status(ASC): ");
        ordered.forEach(rsp -> System.out.println(rsp.toString()));
        //Test:02
        List<Response> ordered2 = responses.sort(SortOrder.DESC, "message");
        Assert.assertNotEquals(response1, ordered2.get(0));
        System.out.println("After SortBy -> Message(DESC): ");
        ordered2.forEach(rsp -> System.out.println(rsp.toString()));
    }

}

class MyExtendedMessage extends MyMessage {
    private String firstName;
    private String secondName;

    /* to test Externalizable*/
    public MyExtendedMessage() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }
}

class MyMessage extends Message {
    private String name;
    private int age;
    private LocalDateTime dob;
    private Boolean isActivate;

    /**
     * Uncomment to test the effect of Externalizable,
     * during serialization/readExternal()
     */
    //public MyMessage() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDateTime getDob() {
        return dob;
    }

    public void setDob(LocalDateTime dob) {
        this.dob = dob;
    }

    public Boolean getActivate() {
        return isActivate;
    }

    public void setActivate(Boolean activate) {
        isActivate = activate;
    }
}