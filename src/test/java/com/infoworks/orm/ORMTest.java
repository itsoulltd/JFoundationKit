package com.infoworks.orm;

import com.infoworks.data.impl.Person;
import com.infoworks.objects.MessageParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Map;

public class ORMTest {

    @Test
    public void propertyTest() {
        Property name = new Property("name", "Adams");
        Assert.assertTrue(name.getType() == DataType.STRING);
        System.out.println(name);

        Property age = new Property("age", 10);
        Assert.assertTrue(age.getType() == DataType.INT);
        System.out.println(age);

        Property active = new Property("active", true);
        Assert.assertTrue(active.getType() == DataType.BOOL);
        System.out.println(active);

        Property dob = new Property("dob", new Date());
        Assert.assertTrue(dob.getType() == DataType.SQLDATE);
        System.out.println(dob);
    }

    @Test
    public void rowTest() throws IOException {
        Row row = new Row().add(new Property("name", "Adams"))
                .add(new Property("age", 10))
                .add(new Property("active", true))
                .add(new Property("dob", new Date()));
        //
        Map<String, Object> data = row.keyObjectMap();
        Assert.assertNotNull(data);
        System.out.println(MessageParser.marshal(data));
    }

    @Test
    public void personToRowTest() throws IOException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Row row = new Row().add(new Property("name", "Adams"))
                .add(new Property("age", 10))
                .add(new Property("active", true))
                .add(new Property("dob", new Date()));
        //
        Person data = row.inflate(Person.class);
        Assert.assertNotNull(data);
        System.out.println(MessageParser.marshal(data));
    }

    @Test
    public void tableTest() {
        //
    }

}