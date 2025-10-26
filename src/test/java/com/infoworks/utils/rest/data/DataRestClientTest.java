package com.infoworks.utils.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infoworks.objects.MessageParser;
import com.infoworks.orm.Property;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class DataRestClientTest {

    private ObjectMapper getMapperWithDatetimeOption() {
        //Solution: Add Jackson JSR-310 Module. Jackson doesn't know how to (de)serialize java.time.LocalDateTime,
        // because Java 8 time types are not supported out-of-the-box unless you register the JSR-310 module.
        ObjectMapper mapper = MessageParser.getJsonSerializer();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Test
    public void doLoadTest() throws Exception {
        //
        URL url = new URL("http://localhost:8080/api/data/users");
        SpringDataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);

        dataSource.setEnableLogging(true);
        PaginatedResponse response = dataSource.load();
        Assert.assertTrue(response != null);

        Page page = response.getPage();
        Assert.assertTrue(page != null);

        Links links = response.getLinks();
        Assert.assertTrue(links != null);
        //Close:
        dataSource.close();
    }

    @Test
    public void doAsyncLoadTest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        //
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        dataSource.setMapper(getMapperWithDatetimeOption());
        dataSource.load((response) -> {
            //In-case of exception:
            /*if (response.getStatus() >= 400) {
                System.out.println(response.getError());
                latch.countDown();
            }*/
            //When success:
            Assert.assertTrue(response != null);

            Page page = response.getPage();
            Assert.assertTrue(page != null);
            System.out.println("Page items: " + page.getTotalElements());

            Links links = response.getLinks();
            Assert.assertTrue(links != null);
            //
            latch.countDown();
        });

        latch.await();
        //Close:
        dataSource.close();
    }

    @Test
    public void addSingleItem() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        dataSource.load();
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        //
        User newUser = new User();
        newUser.setName("Sohana Islam Khan");
        newUser.setAge(28);
        newUser.setSex("FEMALE");
        newUser.setActive(true);
        newUser.setDob(new Date(Instant.now().minus(28 * 365, ChronoUnit.DAYS).toEpochMilli()));
        //Create:
        Object id = dataSource.add(newUser);
        Assert.assertTrue(id != null);
        //Close:
        dataSource.close();
    }

    @Test
    public void updateSingleItem() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        dataSource.load();
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        //
        Object[] passengers = dataSource.readSync(0, dataSource.size());
        Assert.assertTrue(passengers.length > 0);
        //
        User user = (User) passengers[0];
        user.setName("Dr. Sohana Khan");
        user.setActive(!user.isActive());
        //Update:
        Object id = user.parseId().orElse(null);
        if(id != null) dataSource.put(id, user);
        Assert.assertTrue(id != null);
        //Close:
        dataSource.close();
    }

    @Test
    public void readTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        dataSource.load();
        //
        User user = dataSource.read(1l);
        Assert.assertTrue(user != null);
        System.out.println(user.getName());
        //Close:
        dataSource.close();
    }

    @Test
    public void sizeTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        dataSource.load();
        //
        int size = dataSource.size();
        Assert.assertTrue(size >= 0);
        System.out.println("Size is: " + size);
        //Close:
        dataSource.close();
    }

    @Test
    public void readNextTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        dataSource.load();
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        //
        Optional<List<User>> passengers = dataSource.next();
        Assert.assertTrue(passengers.isPresent());
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        //Close:
        dataSource.close();
    }

    @Test
    public void readAsyncNextTest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        //
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        dataSource.load();
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        //
        dataSource.next((passengers) -> {
            Assert.assertTrue(passengers.isPresent());
            latch.countDown();
        });
        latch.await();
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        //Close:
        dataSource.close();
    }

    @Test
    public void CRUDTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        dataSource.load();
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        //
        User newUser = new User();
        newUser.setName("Sohana Islam Khan");
        newUser.setAge(28);
        newUser.setSex("FEMALE");
        newUser.setActive(true);
        newUser.setDob(new Date(Instant.now().minus(28 * 365, ChronoUnit.DAYS).toEpochMilli()));
        //Create:
        Object id = dataSource.add(newUser);
        Assert.assertTrue(id != null);
        //Read One:
        User read = dataSource.read(id);
        Assert.assertTrue(read != null);
        //Read from local:
        Object[] items = dataSource.readSync(0, dataSource.size());
        Stream.of(items).forEach(item -> {
            if (item instanceof User)
                System.out.println(((User) item).getName());
        });
        //Update:
        newUser.setName("Dr. Sohana Islam Khan");
        dataSource.put(id, newUser);
        //Read again: (will read from local)
        User readAgain = dataSource.read(id);
        System.out.println(readAgain.getName());
        //Delete:
        System.out.println("Count before delete: " + dataSource.size());
        dataSource.remove(id);
        System.out.println("Count after delete: " + dataSource.size());
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        dataSource.close();
    }

    @Test
    public void readAllPages() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient(User.class, url);
        //Read All Pages Until last page:
        dataSource.load();
        Optional<List<User>> opt;
        do {
            opt = dataSource.next();
            System.out.println("Current Page: " + dataSource.currentPage());
            System.out.println("Local Size: " + dataSource.size());
        } while (opt.isPresent());
        //
        Object[] all = dataSource.readSync(0, dataSource.size());
        Stream.of(all).forEach(item -> {
            if (item instanceof User)
                System.out.println(((User) item).getName());
        });
        //Close:
        dataSource.close();
    }

    @Test
    public void whenCreatesEmptyOptional_thenCorrect() {
        Optional<String> empty = Optional.empty();
        Assert.assertFalse(empty.isPresent());
        //Available on Java-11:
        //Assert.assertTrue(empty.isEmpty());
    }

    @Test
    public void givenOptional_whenIsPresentWorks_thenCorrect() {
        Optional<String> opt = Optional.of("Baeldung");
        Assert.assertTrue(opt.isPresent());

        opt = Optional.ofNullable(null);
        Assert.assertFalse(opt.isPresent());
    }

    @Test
    public void searchFindByAgeLimitTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient<>(User.class, url);
        dataSource.load();
        //
        Optional<List<User>> passengers = dataSource.search("findByAgeLimit"
                , new Property("min", "18"), new Property("max", "29"));
        Assert.assertTrue(passengers.isPresent());
        //Close:
        dataSource.close();
    }

    @Test
    public void searchAsyncFindByAgeLimitTest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        //
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient<>(User.class, url);
        dataSource.load();
        //
        dataSource.search("findByAgeLimit"
                , new Property[]{new Property("min", "18"), new Property("max", "29")}
                , (Optional<List<User>> passengers) -> {
                    Assert.assertTrue(passengers.isPresent());
                    passengers.orElse(new ArrayList<>())
                            .forEach(user ->
                                    System.out.println(user.getName() + ", Gender:" + user.getSex())
                            );
                    latch.countDown();
                });
        latch.await();
        //Close:
        dataSource.close();
    }

    @Test
    public void searchFindByNameTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient<>(User.class, url);
        dataSource.load();
        //
        Optional<List<User>> passengers = dataSource.search("/findByName", new Property("name", "Soha"));
        Assert.assertTrue(passengers.isPresent());
        //Close:
        dataSource.close();
    }

    @Test
    public void searchAsyncFindByNameTest() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        //
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient<>(User.class, url);
        dataSource.load();
        //
        dataSource.search("/findByName"
                , new Property[]{new Property("name", "Soha")}
                , (Optional<List<User>> passengers) -> {
                    Assert.assertTrue(passengers.isPresent());
                    latch.countDown();
                });
        latch.await();
        //Close:
        dataSource.close();
    }

    @Test
    public void searchFunctionIsExistTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = new SpringDataRestClient<>(User.class, url);
        dataSource.load();
        //
        boolean isExist = dataSource.isSearchActionExist("findByName");
        Assert.assertTrue(isExist);
        //
        isExist = dataSource.isSearchActionExist("findByNameAndOthers");
        Assert.assertFalse(isExist);
        //Close:
        dataSource.close();
    }

    /////////////////////////////////////////////////////////////////////////////

    public static class User extends Any<Long> {
        private String name;
        private String sex = "NONE";
        private int age = 18;
        private Date dob = new java.sql.Date(new Date().getTime());
        private boolean active;

        public User() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
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

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public void unmarshalling(Map<String, Object> data, boolean inherit) {
            Object dob = data.get("dob");
            if (dob != null) {
                try {
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    Date parsed = formatter.parse(dob.toString());
                    data.put("dob", parsed);
                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                }
            }
            super.unmarshalling(data, inherit);
        }
    }

    /////////////////////////////////////////////////////////////////////////////
}
