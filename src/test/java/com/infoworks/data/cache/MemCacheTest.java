package com.infoworks.data.cache;

import com.infoworks.PLogger;
import com.infoworks.objects.Message;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;

public class MemCacheTest {

    private MemCache<Car> client;

    @Before
    public void setUp() throws Exception {
        String redisHost = "localhost";
        Integer redisPort = 6379;
        RedisClient client = RedisClient.create(new RedisURI(redisHost, redisPort, Duration.ofSeconds(30)));
        this.client = new MemCache(new LettuceDataSource(client, 20), Car.class);
    }

    @After
    public void tearDown() throws Exception {
        if (client != null) client.close();
    }

    @Test
    public void connectionTest() {
        long ttl = Duration.ofMillis(1000).toMillis();
        Assert.assertTrue(client.getClient().isConnectionOpen() == true);
    }

    @Test
    public void saveTest() {
        PLogger logger = new PLogger();
        Car newCar = new Car();
        newCar.setRegistration("112234ewe ");
        newCar.setChassis("jhwe654");
        newCar.setType("SEDAN");
        newCar.setColor("AccentBlue");
        client.put("NewCar", newCar);
        logger.printMillis("saveTest:put");
        //
        logger = new PLogger();
        boolean exist = client.containsKey("NewCar");
        logger.printMillis("saveTest:contains");
        Assert.assertTrue(exist);
    }

    @Test
    public void readTest() {
        PLogger logger = new PLogger();
        Car newCar = new Car();
        newCar.setRegistration("112234ewe ");
        newCar.setChassis("jhwe654");
        newCar.setType("SEDAN");
        newCar.setColor("AccentBlue");
        client.put("NewCar", newCar);
        logger.printMillis("readTest:put");
        //
        logger = new PLogger();
        boolean exist = client.containsKey("NewCar");
        if (exist){
            Car rCar = client.read("NewCar");
            Assert.assertNotNull(rCar);
            Assert.assertTrue(newCar.getRegistration().equalsIgnoreCase(rCar.getRegistration()));
        }
        logger.printMillis("readTest:read");
        //
    }

    @Test
    public void removeTest() {
        PLogger logger = new PLogger();
        Car newCar = new Car();
        newCar.setRegistration("112234ewe ");
        newCar.setChassis("jhwe654");
        newCar.setType("SEDAN");
        newCar.setColor("AccentBlue");
        client.put("NewCar", newCar);
        logger.printMillis("removeTest:put");
        //
        logger = new PLogger();
        boolean exist = client.containsKey("NewCar");
        if (exist) {
            Car rCar = client.remove("NewCar");
            Assert.assertNotNull(rCar);
            Assert.assertTrue(newCar.getRegistration().equalsIgnoreCase(rCar.getRegistration()));
        }
        logger.printMillis("removeTest:remove");
        //
    }

}

class Car extends Vehicle {
    private String registration;
    private String chassis;

    public Car() {}

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getChassis() {
        return chassis;
    }

    public void setChassis(String chassis) {
        this.chassis = chassis;
    }
}

class Vehicle extends Message {
    private String type;
    private int wheels;
    private String color;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWheels() {
        return wheels;
    }

    public void setWheels(int wheels) {
        this.wheels = wheels;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}