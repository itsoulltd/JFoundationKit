package com.infoworks.utils.rest.client.publisher;

import com.infoworks.objects.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultipartFilePublisher implements MultipartBodyPublisher {

    private static String BOUNDARY = new BigInteger(256, new Random()).toString();

    @Override
    public HttpRequest.BodyPublisher ofMultipartBody(String filename, MediaType fileType, InputStream ios) throws IOException {
        List<byte[]> byteArrays = new ArrayList<>();
        byte[] separator = ("\r\n--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        //start:
        byteArrays.add(separator);
        String mimeType = fileType.value();
        byteArrays.add(("\"" + "file"
                + "\"; filename=\"" + filename
                + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
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
