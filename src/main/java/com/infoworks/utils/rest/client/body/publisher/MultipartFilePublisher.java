package com.infoworks.utils.rest.client.body.publisher;

import com.infoworks.objects.MediaType;
import com.infoworks.orm.Property;
import com.infoworks.orm.Row;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultipartFilePublisher implements MultipartBodyPublisher {

    private static String BOUNDARY = new BigInteger(256, new Random()).toString();

    @Override
    public HttpRequest.BodyPublisher ofMultipartBody(InputStream ios, Property...contentDispositions) throws IOException {
        List<byte[]> byteArrays = new ArrayList<>();
        byte[] separator = ("\r\n--" + BOUNDARY + "\r\n")
                .getBytes(StandardCharsets.UTF_8);
        //start:
        byteArrays.add(separator);
        //String name, String filename, String Content-Type;
        Row row = new Row();
        row.setProperties(Arrays.asList(contentDispositions));
        Map<String, Property> disposition = row.keyValueMap();
        String name = disposition.get("name").getValue().toString();
        String filename = disposition.get("filename").getValue().toString();
        String mimeType = disposition.get("Content-Type").getValue().toString();
        //
        byteArrays.add(("Content-Disposition: form-data; name=\"" + name + "\"; "
                + "filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(ios.readAllBytes());
        //end
        byteArrays.add(("\r\n--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    @Override
    public String contentTypeValue(MediaType type) {
        return String.format("%s;boundary=%s", type.value(), BOUNDARY);
    }
}
