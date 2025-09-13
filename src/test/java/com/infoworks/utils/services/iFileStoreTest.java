package com.infoworks.utils.services;

import com.infoworks.orm.Property;
import com.infoworks.utils.services.impl.FileStore;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class iFileStoreTest {

    @Test
    public void notFound() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", "qwtep"));
        //Assert.assertTrue(stream.isEmpty());
        System.out.println("notFound Count: " + stream.size());
    }

    @Test
    public void notFoundEmpty() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", " "));
        //Assert.assertTrue(stream.isEmpty());
        System.out.println("notFoundEmpty Count: " + stream.size());
    }

    @Test
    public void fileExistAll() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("filename", "emn"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("fileExistAll Count: " + stream.size());
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
        System.out.println("contentLengthCheck Total Content Length: " + cLength);
        System.out.println("contentLengthCheck Count: " + stream.size());
    }

    @Test
    public void searchByFileNameInDir() {
        String dir = "src/test/resources/Users/Public";
        iFileStore<InputStream> iFile = new FileStore(dir);
        List<File> stream = iFile.search(Paths.get(dir).toFile(), new Property("filename", "a"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("searchByFileNameInDir Count: " + stream.size());
    }

    @Test
    public void searchByDirNameInDir() {
        String dir = "src/test/resources/Users/Public";
        iFileStore<InputStream> iFile = new FileStore(dir);
        List<File> stream = iFile.search(Paths.get(dir).toFile(), new Property("dirname", "Reports"));
        //Assert.assertTrue(stream.isEmpty());
        System.out.println("searchByDirNameInDir Count: " + stream.size());
    }

    @Test
    public void fileExistSub() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public/Downloads");
        List<InputStream> stream = iFile.search(new Property("filename", "emn"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("fileExistSub Count: " + stream.size());
    }

    @Test
    public void dirExist() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("dirname", "Downloads"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("dirExist Count: " + stream.size());
    }

    @Test
    public void emptyDirTest() {
        iFileStore<InputStream> iFile = new FileStore("src/test/resources/Users/Public");
        List<InputStream> stream = iFile.search(new Property("dirname", "Reports"));
        //Assert.assertTrue(!stream.isEmpty());
        System.out.println("emptyDirTest Count: " + stream.size());
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
        System.out.println("makingZip Count: " + files.size());
        //Cleaning zip file:
        Boolean deleted = toWrite.delete();
        System.out.println(fileName + " Deleted: " + deleted);
    }

    @Test
    public void uploadTest() {
        String readDir = "src/test/resources/Users/Public";
        iFileStore<InputStream> readFile = new FileStore(readDir);
        InputStream readStream = readFile.read("abc-1.txt");
        Assert.assertNotNull(readStream);
        //
        String uploadDir = "target/";
        iFileStore<InputStream> uploadFile = new FileStore(uploadDir);
        uploadFile.put("abc-1.txt", readStream);
        String[] names = uploadFile.filenames();
        Assert.assertTrue(names.length > 0);
        System.out.println("Uploaded: ");
        Stream.of(names).forEach(System.out::println);
    }

    @Test
    public void downloadTest() {
        String readDir = "src/test/resources/Users/Public/Downloads";
        iFileStore<InputStream> readFile = new FileStore(readDir);
        InputStream readStream = readFile.read("abc-2.txt");
        Assert.assertNotNull(readStream);
    }

    @Test
    public void removeTest() {
        String readDir = "src/test/resources/Users/Public";
        iFileStore<InputStream> readFile = new FileStore(readDir);
        InputStream readStream = readFile.read("abc-1.txt");
        Assert.assertNotNull(readStream);
        //
        String uploadDir = "target/";
        iFileStore<InputStream> uploadFile = new FileStore(uploadDir);
        uploadFile.put("abc-1.txt", readStream);
        String[] names = uploadFile.filenames();
        Assert.assertTrue(names.length > 0);
        //
        InputStream removed = uploadFile.remove("abc-1.txt");
        Assert.assertNull(removed);
        names = uploadFile.filenames();
        Assert.assertTrue(names.length == 0);
    }

}