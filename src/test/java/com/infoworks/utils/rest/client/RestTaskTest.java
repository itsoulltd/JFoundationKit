package com.infoworks.utils.rest.client;

import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.orm.Row;
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
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }

    @Test
    public void getTestV2() {
        GetTask task = new GetTask("https://jsonplaceholder.typicode.com/posts", "/1");
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

    @Test
    public void postTest() {
        PostTask task = new PostTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        //
        task.setBody(new Row().add("name", "sohana"), null);
        task.execute(null);
    }

    @Test
    public void putTest() {
        HttpTask task = new PutTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        task.execute(null);
    }

    @Test
    public void deleteTest() {
        HttpTask task = new DeleteTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        task.execute(null);
    }

}