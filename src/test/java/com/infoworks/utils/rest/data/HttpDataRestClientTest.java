package com.infoworks.utils.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infoworks.objects.MessageParser;
import com.infoworks.orm.Property;
import com.infoworks.utils.rest.data.model.Links;
import com.infoworks.utils.rest.data.model.Page;
import com.infoworks.utils.rest.data.model.PaginatedResponse;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

public class HttpDataRestClientTest {

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
        DataRestClient<User> dataSource = DataRestClient.defaultClient(User.class, url, true);

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
        DataRestClient<User> dataSource = DataRestClient.defaultClient(User.class, url, false);
        dataSource.setMapper(getMapperWithDatetimeOption());
        dataSource.load((response) -> {
            //In-case of exception:
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
    public void sizeTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = DataRestClient.defaultClient(User.class, url, false);
        dataSource.load();
        //
        int size = dataSource.size();
        Assert.assertTrue(size >= 0);
        System.out.println("Size is: " + size);
        //Close:
        dataSource.close();
    }

    @Test
    public void searchFunctionIsExistTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = DataRestClient.defaultClient(User.class, url, false);
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

    @Test
    public void readAllPages() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = DataRestClient.defaultClient(User.class, url, false);
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
    public void CRUDTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = DataRestClient.defaultClient(User.class, url, false);
        dataSource.load();
        //
        System.out.println("Is last page: " + dataSource.isLastPage());
        //
        User newUser = new User();
        newUser.setName("test_user");
        newUser.setEmail("test_user@gmail.com");
        newUser.setAge(28);
        newUser.setSex("FEMALE");
        newUser.setActive(true);
        newUser.setDob(new Date(Instant.now().minus(28 * 365, ChronoUnit.DAYS).toEpochMilli()));
        //Create:
        Object id = dataSource.add(newUser);
        //Read By Name: if-id-is-null
        if (id == null) {
            Optional<List<User>> users = dataSource.search("/findByName", new Property("name", "test_user"));
            User read = users.map(items -> items.get(0)).orElse(null);
            Assert.assertTrue(read != null);
            id = read.parseId().orElse(null);
        }
        //Read from local: (Only-if-Read-Add-Put-Next)
        /*Object[] items = dataSource.readSync(0, dataSource.size());
        Stream.of(items).forEach(item -> {
            if (item instanceof User)
                System.out.println(((User) item).getName());
        });*/
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
    public void deleteByIdTest() throws Exception {
        URL url = new URL("http://localhost:8080/api/data/users");
        DataRestClient<User> dataSource = DataRestClient.defaultClient(User.class, url, true);
        dataSource.load();
        //
        Optional<List<User>> users = dataSource.search("/findByName", new Property("name", "test_user"));
        if (users.isPresent() && !users.get().isEmpty()) {
            User user = users.get().get(0);
            long id = Long.parseLong(user.parseId().orElse("0").toString());
            User removed = dataSource.remove(id);
            Assert.assertTrue(removed != null);
            System.out.println(user.getName());
        }
        //Close:
        dataSource.close();
    }

}
