package com.infoworks.utils.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infoworks.objects.MessageParser;
import com.infoworks.utils.rest.data.model.Links;
import com.infoworks.utils.rest.data.model.Page;
import com.infoworks.utils.rest.data.model.PaginatedResponse;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

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
}
