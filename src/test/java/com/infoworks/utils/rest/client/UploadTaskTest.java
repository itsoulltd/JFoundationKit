package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Response;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class UploadTaskTest {

    @Test
    public void uploadTest() {
        Path path = Paths.get("src","test","resources", "data", "final-architecture.png");
        File imfFile = new File(path.toFile().getAbsolutePath());
        //
        UploadTask task = new UploadTask("http://localhost:8080/api/upload", MediaType.PNG, imfFile);
        task.setToken("my-token");
        Response response = task.execute(null);
        System.out.println(response.getStatus());
    }
}