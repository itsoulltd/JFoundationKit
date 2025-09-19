package com.infoworks.utils.rest.client;

import com.infoworks.orm.Property;
import com.infoworks.utils.rest.base.HttpTask;
import org.junit.Test;

import static org.junit.Assert.*;

public class RestTaskTest {

    @Test
    public void getTest() {
        GetTask task = new GetTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        task.execute(null);
    }

}