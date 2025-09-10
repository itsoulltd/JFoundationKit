package com.infoworks.utils.services;

import com.infoworks.orm.Property;
import com.infoworks.utils.services.impl.FileStore;
import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public class iFileStoreTest {

    @Test
    public void notFound() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", "qwtep"));
        //Assert.assertTrue(stream.isEmpty());
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void notFoundEmpty() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", " "));
        //Assert.assertTrue(stream.isEmpty());
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void fileExistAll() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", "emn"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void contentLengthCheck() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", "emn"));
        //Assert.assertTrue(!stream.isEmpty());
        int cLength = stream.stream()
                .flatMapToInt(ios -> {
                    try {
                        int length = ios.available();
                        return IntStream.of(length);
                    } catch (IOException e) {}
                    return IntStream.of(0);
                }).sum();
        System.out.println("Total Content Length: " + cLength);
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void searchByFileNameInDir() {
        String dir = "src/test/resources/Users";
        iFileStore<InputStream> iFile = new FileStore(dir);
        List<File> stream = iFile.search(Paths.get(dir).toFile(), new Property("filename", "a"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void searchByDirNameInDir() {
        String dir = "src/test/resources/Users/Public";
        iFileStore<InputStream> iFile = new FileStore(dir);
        List<File> stream = iFile.search(Paths.get(dir).toFile(), new Property("filename", "Reports"));
        //Assert.assertTrue(stream.isEmpty());
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void fileExistSub() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public/Downloads");
        List<InputStream> stream = iFile.search(new Property("filename", "emn"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void dirExist() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", "Downloads"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void emptyDirTest() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", "Reports"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("Count: " + stream.size());
    }

    @Test
    public void makingZip() throws IOException {
        String readDir = "src/test/resources/Users/Public/Downloads";
        iFileStore<InputStream> iFile = new FileStore(readDir);
        List<File> files = iFile.search(Paths.get(readDir).toFile(), new Property("filename", "emn"));
        SimpleDateFormat fileNameDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = String.format("%s_%s.zip", UUID.randomUUID().toString().substring(0, 8)
                , fileNameDateFormatter.format(new Date()));

        String writeDir = "target/";
        File toWrite = Paths.get(writeDir, fileName).toFile();
        //
        if (!files.isEmpty()) {
            //Searching By File-Names:-
            OutputStream fos = new FileOutputStream(toWrite);
            iFile.prepareZipEntryFrom(files, fos);
            fos.flush();
            fos.close();
        }
        System.out.println("Count: " + files.size());
    }

}