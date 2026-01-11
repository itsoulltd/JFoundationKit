package com.infoworks.utils.services;

import com.infoworks.objects.Message;
import io.jsonwebtoken.lang.Collections;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class iPropertiesTest {

    @Test
    public void test() {
        Path path = Paths.get("src","test","resources","app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        iProperties properties = iProperties.create(absolutePath, null);
        String val = properties.read("last.read");
        Assert.assertTrue(val.equals("100"));
        System.out.println(val);
    }

    @Test
    public void testRead() {
        Path resourceDirectory = Paths.get("src","test","resources","app.properties");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        //
        Map data = new HashMap();
        data.put("last.read", "100");
        iProperties properties = iProperties.create(absolutePath, data);
        String val = properties.read("last.read");
        Assert.assertTrue(val.equals("100"));
        System.out.println(val);
    }

    @Test
    public void testFalse() {
        Path path = Paths.get("src","test","resources","app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        iProperties properties = iProperties.create(absolutePath, null);
        String val = properties.read("last.read.vb");
        Assert.assertTrue(val.equals(""));
        System.out.println(val);
    }

    @Test
    public void testReplace() {
        Path path = Paths.get("src","test","resources","app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        Map data = new HashMap();
        data.put("last.read", "100");
        iProperties properties = iProperties.create(absolutePath, data);
        properties.replace("last.read", "109");
        String val = properties.read("last.read");
        Assert.assertTrue(val.equals("109"));
        System.out.println(val);
    }

    @Test
    public void testRemove() {
        Path path = Paths.get("src","test","resources","app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        Map data = new HashMap();
        data.put("last.read", "100");
        iProperties properties = iProperties.create(absolutePath, data);
        properties.remove("last.read");
        String val = properties.read("last.read");
        Assert.assertTrue(val.equals(""));
        System.out.println(val);
    }

    @Test
    public void testFileName() {
        Path path = Paths.get("src", "test", "resources", "app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        iProperties properties = iProperties.create(absolutePath, null);
        Assert.assertTrue(properties.fileName().equals("app.properties"));
    }

    @Test
    public void testReadSync() {
        Path path = Paths.get("src", "test", "resources", "app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        iProperties properties = iProperties.create(absolutePath, null);
        String[] values = properties.readSync(0, properties.size());
        Assert.assertTrue(values.length == properties.size());
    }

    @Test
    public void testReadSyncInPage() {
        Path path = Paths.get("src", "test", "resources", "app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        int offset = 0;
        int limit = 2;
        iProperties properties = iProperties.create(absolutePath, null);
        String[] values = properties.readSync(offset, limit);
        Assert.assertTrue(values.length == limit);
    }

    @Test
    public void testObjectFailed() throws IOException {
        Path path = Paths.get("src", "test", "resources", "app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        iProperties properties = iProperties.create(absolutePath, null);
        Car drive4D = properties.getObject("obj.val", Car.class);
        Assert.assertTrue(drive4D.regNo == null);
        Assert.assertTrue(drive4D.wheels == 0);
    }

    @Test
    public void testAddAndFlush() {
        //Path path = Paths.get("src","test","resources","app.properties");
        Path path = Paths.get("target", "app.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        Map<String, String> defaultData = new HashMap<>();
        defaultData.put("last.read", "100");
        //
        iProperties properties = iProperties.create(absolutePath, defaultData);
        String val = properties.read("last.read");
        Assert.assertTrue(val.equals("100"));
        //
        int nVal = 100 + (new Random().nextInt(9) + 1);
        properties.put("last.read.new", nVal + "");
        properties.flush();
        val = properties.read("last.read.new");
        Assert.assertTrue(val.equals(nVal + ""));
        System.out.println(val);
    }

    @Test
    public void testObject() throws IOException {
        //Path path = Paths.get("src", "test", "resources", "app.properties");
        Path path = Paths.get("target", "app1.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        iProperties properties = iProperties.create(absolutePath, null);
        Date dob = new Date();
        properties.putObject("obj.val", new Person("Subha", 23, dob));
        properties.save(false);
        //
        Person subha = properties.getObject("obj.val", Person.class);
        Assert.assertTrue(subha.name.equals("Subha"));
        Assert.assertTrue(subha.age == 23);
        Assert.assertTrue(subha.dob.getTime() == dob.getTime());
    }

    @Test
    public void testObject2() throws IOException {
        Path path = Paths.get("target", "app2.properties");
        String absolutePath = path.toAbsolutePath().toString();
        //
        iProperties properties = iProperties.create(absolutePath, null);
        properties.putObject("obj.val.car", new Car("KHA-324490", 4));
        properties.flush();
        //
        Car drive4D = properties.getObject("obj.val.car", Car.class);
        Assert.assertTrue(drive4D.regNo.equals("KHA-324490"));
        Assert.assertTrue(drive4D.wheels == 4);
    }

    private InputStream createFileInputStreamFromTestResources(String fileName) throws FileNotFoundException {
        Path path = Paths.get("src","test","resources", fileName);
        return createFileInputStream(path);
    }

    private InputStream createFileInputStream(Path path) throws FileNotFoundException {
        File imfFile = new File(path.toFile().getAbsolutePath());
        InputStream ios = new FileInputStream(imfFile);
        return ios;
    }

    @Test
    public void testIosReadSync() {
        try (InputStream ios = createFileInputStreamFromTestResources("app.properties")) {
            iProperties properties = iProperties.createInMemory(ios, null);
            String[] values = properties.readSync(0, properties.size());
            Assert.assertTrue(values.length == properties.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIosInMemory() {
        try (InputStream ios = createFileInputStreamFromTestResources("app.properties")) {
            iProperties properties = iProperties.createInMemory(ios, null);
            String[] values = properties.readSync(0, properties.size());
            Assert.assertTrue(values.length == properties.size());
            System.out.println(values);
            //
            Assert.assertTrue(properties.isInMemory());
            System.out.println("Status: in-memory " + properties.isInMemory());
            properties.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Person extends Message{
        private String name;
        private int age;
        private Date dob;

        public Person() {}

        public Person(String name, int age, Date dob) {
            this.name = name;
            this.age = age;
            this.dob = dob;
        }

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

        public Date getDob() {
            return dob;
        }

        public void setDob(Date dob) {
            this.dob = dob;
        }
    }

    private static class Car extends Message {
        private String regNo;
        private int wheels;

        public Car() {}

        public Car(String regNo, int wheels) {
            this.regNo = regNo;
            this.wheels = wheels;
        }

        public String getRegNo() {
            return regNo;
        }

        public void setRegNo(String regNo) {
            this.regNo = regNo;
        }

        public int getWheels() {
            return wheels;
        }

        public void setWheels(int wheels) {
            this.wheels = wheels;
        }
    }

}