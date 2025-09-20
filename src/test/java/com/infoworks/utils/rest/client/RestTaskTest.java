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
        RestTask task = new GetTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }

    @Test
    public void getTestV2() {
        RestTask task = new GetTask("https://jsonplaceholder.typicode.com/posts", "/1");
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

    @Test
    public void postTest() {
        RestTask task = new PostTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        //
        task.setBody(new Row().add("name", "sohana"), null);
        task.execute(null);
    }

    @Test
    public void postTestV2() {
        RestTask task = new PostTask("https://jsonplaceholder.typicode.com/posts", null);
        task.setBody(new Row().add("title", "foo")
                .add("body", "bar")
                .add("userId", "1"), null);
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

    @Test
    public void putTest() {
        RestTask task = new PutTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        task.execute(null);
    }

    @Test
    public void putTestV2() {
        RestTask task = new PutTask("https://jsonplaceholder.typicode.com/posts", "/1");
        task.setBody(new Row().add("id", "1")
                .add("title", "foo updated title")
                .add("body", "bar updated body")
                .add("userId", "1"), null);
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

    @Test
    public void deleteTest() {
        RestTask task = new DeleteTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        task.execute(null);
    }

    @Test
    public void deleteTestV2() {
        RestTask task = new DeleteTask("https://jsonplaceholder.typicode.com/posts", "/1");
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

}