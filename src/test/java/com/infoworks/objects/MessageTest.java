package com.infoworks.objects;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
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

}

class MyExtendedMessage extends MyMessage {
    private String firstName;
    private String secondName;

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