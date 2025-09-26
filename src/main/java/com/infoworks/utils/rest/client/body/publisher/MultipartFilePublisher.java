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
        //String name, String filename, String Content-Type;
        Map<String, Property> disposition = convert(contentDispositions);
        String name = disposition.get("name").getValue().toString();
        String filename = disposition.get("filename").getValue().toString();
        String mimeType = disposition.get("Content-Type").getValue().toString();
        //start:
        List<byte[]> byteArrays = new ArrayList<>();
        byte[] separator = ("\r\n--" + BOUNDARY + "\r\n").getBytes(StandardCharsets.UTF_8);
        byteArrays.add(separator);
        byteArrays.add(("Content-Disposition: form-data; name=\"" + name + "\"; "
                + "filename=\"" + filename + "\"\r\n"
                + "Content-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(ios.readAllBytes());
        byteArrays.add(("\r\n--" + BOUNDARY + "--\r\n").getBytes(StandardCharsets.UTF_8));
        //end
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    private Map<String, Property> convert(Property...properties) {
        Row row = new Row();
        row.setProperties(Arrays.asList(properties));
        return row.keyValueMap();
    }

    @Override
    public String contentTypeValue(MediaType type) {
        return String.format("%s;boundary=%s", type.value(), BOUNDARY);
    }
}
