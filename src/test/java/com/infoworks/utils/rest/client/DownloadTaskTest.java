package com.infoworks.utils.rest.client;

import com.infoworks.utils.services.iResources;
import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.junit.Assert.*;

public class DownloadTaskTest {

    @Test
    public void downloadTaskTest() {
        //TaskFlow:
        //Test Url-1: https://farm7.staticflickr.com/6089/6115759179_86316c08ff_z_d.jpg
        //
        DownloadTask task = new DownloadTask("https://farm7.staticflickr.com/6089/6115759179_86316c08ff_z_d.jpg"
                , null);
        task.setToken("my-token");
        DownloadTask.ResourceResponse response = task.execute(null);
        System.out.println("Status: " + response.getStatus());
        //
        if (response.getResource() != null) {
            try (InputStream iso = response.getResource()) {
                iResources service = iResources.create();
                BufferedImage img = service.readAsImage(iso, TYPE_INT_RGB);
                Assert.assertNotNull(img);
                System.out.println("Image Downloaded: " + response.filename());
                System.out.println("Image Size: " + response.contentLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}