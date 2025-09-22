package com.infoworks.utils.rest.client.publisher;

import com.infoworks.objects.MediaType;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.util.Random;

public class MultipartFilePublisher implements MultipartBodyPublisher {

    private static String BOUNDARY = new BigInteger(256, new Random()).toString();

    @Override
    public HttpRequest.BodyPublisher ofMultipartBody(String filename, MediaType fileType, InputStream ios) {
        return null;
    }

    @Override
    public String contentTypeValue(MediaType type) {
        return String.format("%s;boundary=%s", type.value(), BOUNDARY);
    }
}
