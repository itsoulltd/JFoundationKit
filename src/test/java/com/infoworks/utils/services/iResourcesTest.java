package com.infoworks.utils.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class iResourcesTest {

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    private InputStream createFileInputStream(String fileName) throws FileNotFoundException {
        return createFileInputStream(fileName, null);
    }

    private InputStream createFileInputStream(String fileName, iResources resources) throws FileNotFoundException {
        if (resources == null) {
            Path path = Paths.get("src","test","resources", fileName);
            File imfFile = new File(path.toFile().getAbsolutePath());
            InputStream ios = new FileInputStream(imfFile);
            return ios;
        } else {
            File imfFile = new File(fileName);
            InputStream ios = resources.createStream(imfFile);
            return ios;
        }
    }

    @Test
    public void readJson(){
        iResources manager = iResources.create();
        String json = manager.readAsString("data/rider-mock-data.json");
        System.out.println(json);

        List<Map<String, Object>> jObj = manager.readAsJsonObject(json);
        System.out.println(jObj.toString());
    }

    @Test
    public void imageAsString() throws IOException {

        iResources resources = iResources.create();
        InputStream ios = createFileInputStream("data/final-architecture.png", resources);
        //
        BufferedImage bufferedImage = resources.readAsImage(ios, BufferedImage.TYPE_INT_RGB);
        ios.close();
        String base64Image = resources.readImageAsBase64(bufferedImage, iResources.Format.PNG);
        System.out.println("Message: " + base64Image);
        //
        BufferedImage decryptedImg = resources.readImageFromBase64(base64Image);
        Assert.assertNotNull(decryptedImg);
    }

    @Test
    public void imageAsStringWithAlternativeFileReading() throws IOException {

        InputStream ios = createFileInputStream("/data/final-architecture.png");
        Assert.assertTrue(ios.available() > 0);
        //
        iResources resources = iResources.create();
        BufferedImage bufferedImage = resources.readAsImage(ios, BufferedImage.TYPE_INT_RGB);
        ios.close();
        String base64Image = resources.readImageAsBase64(bufferedImage, iResources.Format.PNG);
        System.out.println("Message: " + base64Image);
        //
        BufferedImage decryptedImg = resources.readImageFromBase64(base64Image);
        Assert.assertNotNull(decryptedImg);
    }

}