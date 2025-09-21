package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Response;
import com.infoworks.utils.rest.base.SSLContextFactory;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLParameters;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class UploadTaskTest {

    @Test
    public void uploadTest() {
        Path path = Paths.get("src","test","resources", "data", "final-architecture.png");
        File imfFile = new File(path.toFile().getAbsolutePath());
        //
        UploadTask task = new UploadTask("http://localhost:8080/files/upload", MediaType.PNG, imfFile);
        task.setToken("my-token");
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        if(response.getStatus() == 500) {
            if(response.getError() != null) System.out.println(response.getError());
            else System.out.println(response.getMessage());
        }
        else System.out.println(response.getMessage());
    }

    @Test
    public void uploadTestV2() throws NoSuchAlgorithmException, KeyManagementException {
        Path path = Paths.get("src","test","resources", "data", "JFoundationKit_Test.pdf");
        File imfFile = new File(path.toFile().getAbsolutePath());
        //
        UploadTask task = new UploadTask("https://localhost:8443/files/upload", MediaType.PDF, imfFile);
        //
        SSLParameters sslParams = new SSLParameters();
        sslParams.setEndpointIdentificationAlgorithm(""); // Disable hostname check
        task.setSslParameters(sslParams);
        task.setSecurity(SSLContextFactory.createDefaultContext());
        //
        Response response = task.execute(null);
        System.out.println(response.getStatus());
        if(response.getStatus() == 500) System.out.println(response.getError());
        else System.out.println(response.getMessage());
    }

    @Test
    public void mediaTypeFromFileTest_PNG() {
        Path path = Paths.get("src","test","resources", "data", "final-architecture.png");
        File imfFile = new File(path.toFile().getAbsolutePath());
        UploadTask task = new UploadTask("", null, imfFile);
        MediaType type = task.getFileType();
        Assert.assertEquals(type.value(), MediaType.PNG.value());
    }

    @Test
    public void mediaTypeFromFileTest_PDF() {
        Path path = Paths.get("src","test","resources", "data", "JFoundationKit_Test.pdf");
        File pdfFile = new File(path.toFile().getAbsolutePath());
        UploadTask task = new UploadTask("", null, pdfFile);
        MediaType type = task.getFileType();
        Assert.assertEquals(type.value(), MediaType.PDF.value());
    }

    @Test
    public void mediaTypeFromFileTest_Json() {
        Path path = Paths.get("src","test","resources", "data", "ride-mock-data.json");
        File jsonFile = new File(path.toFile().getAbsolutePath());
        UploadTask task = new UploadTask("", null, jsonFile);
        MediaType type = task.getFileType();
        Assert.assertEquals(type.value(), MediaType.JSON.value());
    }
}