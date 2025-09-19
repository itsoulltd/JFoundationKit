package com.infoworks.utils.rest.client;

import com.infoworks.utils.services.iResources;
import org.junit.Assert;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

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
                System.out.println("Media type: " + response.mediaType().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void downloadTaskTest2() {
        //TaskFlow:
        //Test Url-2: https://farm2.staticflickr.com/1090/4595137268_0e3f2b9aa7_z_d.jpg
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void downloadTaskTest3v2() {
        //TaskFlow:
        //Test Url-2: https://farm2.staticflickr.com/1090/4595137268_0e3f2b9aa7_z_d.jpg
        //
        DownloadTask task = new DownloadTask("https://farm2.staticflickr.com/1090/4595137268_0e3f2b9aa7_z_d.jpg"
                , null);
        task.setToken("my-token");
        task.addResponseListener((encoded) -> {
            System.out.println(encoded != null ? encoded.length() : "0");
            try {
                String decoded = new String(Base64.getDecoder().decode(encoded), "UTF-8");
                System.out.println(decoded);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        DownloadTask.ResourceResponse response = task.execute(null);
        System.out.println("Status: " + response.getStatus());
        //
        if (response.getResource() != null) {
            System.out.println("Image Downloaded: " + response.filename());
        }
    }

    @Test
    public void downloadTaskTest4() {
        //TaskFlow:
        //Test Url-4: https://farm2.staticflickr.com/1090/bb3_z_d.jpg
        //
        DownloadTask task = new DownloadTask("https://farm2.staticflickr.com/1090/bb3_z_d.jpg"
                , null);
        task.setToken("my-token");
        task.addResponseListener((encoded) -> {
            System.out.println(encoded != null ? encoded.length() : "0");
        });
        DownloadTask.ResourceResponse response = task.execute(null);
        System.out.println("Status: " + response.getStatus());
        System.out.println("Error: " + response.getError());
        //
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void downloadTaskTest5() {
        //TaskFlow:
        //Test Url-5: "" or Null
        //
        DownloadTask task = new DownloadTask(null, null);
        task.setToken("my-token");
        task.addResponseListener((encoded) -> {
            System.out.println(encoded != null ? encoded.length() : "0");
        });
        DownloadTask.ResourceResponse response = task.execute(null);
        System.out.println("Status: " + response.getStatus());
        System.out.println("Error: " + response.getError());
        //
    }

}