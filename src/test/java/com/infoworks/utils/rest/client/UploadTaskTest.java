package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Response;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class UploadTaskTest {

    //@Test
    public void uploadTest() {
        UploadTask task = new UploadTask("http://localhost:8080/api/upload"
                , MediaType.PNG
                , new File("/data/final-architecture.png"));
        task.setToken("my-token");
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }
}