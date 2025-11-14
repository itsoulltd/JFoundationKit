package com.infoworks.utils.rest.spring;

import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.orm.Row;
import com.infoworks.utils.rest.base.HttpTask;
import org.junit.Test;

public class RestTaskTest {

    @Test
    public void getTest() {
        GetTask task = new GetTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana_islam"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }

    @Test
    public void getTestV1() {
        GetTask task = new GetTask(
                "http://localhost:8080", null
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }

    @Test
    public void getTestV2() {
        GetTask task = new GetTask(
                "http://localhost:8080", ""
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }

    @Test
    public void getTestV3() {
        GetTask task = new GetTask(
                "http://localhost:8080", "/"
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }

    @Test
    public void getTestV4() {
        GetTask task = new GetTask(
                "http://localhost:8080/", "/"
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }

    @Test
    public void getTestV5() {
        GetTask task = new GetTask("https://jsonplaceholder.typicode.com/posts", "/1");
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

    @Test
    public void getTestV5_2() {
        GetTask task = new GetTask("https://jsonplaceholder.typicode.com/posts"
                , "/1"
                , new Property("name", "islam"), new Property("age", 39));
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

    @Test
    public void getTestV5_3() {
        GetTask task = new GetTask("https://jsonplaceholder.typicode.com/posts", "/1"
                , new Property[] {new Property("name", "islam"), new Property("age", 39)}
                , (response) -> {
                    //Result:
                    System.out.println(response);
                }
        );
        task.execute(null);
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
        PostTask task = new PostTask("https://jsonplaceholder.typicode.com/posts", null);
        task.setBody(new Row().add("title", "foo")
                .add("body", "bar")
                .add("userId", "1"), null);
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

    @Test
    public void postTestV2_2() {
        PostTask task = new PostTask("https://jsonplaceholder.typicode.com/posts", null);
        task.setBody(new Row().add("title", "foo")
                .add("body", "bar")
                .add("userId", "1"), null);
        task.addResponseListener((response) -> {
            //Result:
            System.out.println(response);
        });
        task.execute(null);
    }

    @Test
    public void putTest() {
        PutTask task = new PutTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        task.execute(null);
    }

    @Test
    public void putTestV2() {
        PutTask task = new PutTask("https://jsonplaceholder.typicode.com/posts", "/1");
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
        DeleteTask task = new DeleteTask(
                "http://localhost:8080"
                , String.format("/api/auth/%s", HttpTask.encodeUrlParam("sohana@gmail.com"))
                , new Property("full name", "sohana islam"), new Property("email", "sohana@gmail.com"));
        task.execute(null);
    }

    @Test
    public void deleteTestV2() {
        DeleteTask task = new DeleteTask("https://jsonplaceholder.typicode.com/posts", "/1");
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        System.out.println(response.getMessage());
    }

}