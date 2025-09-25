package com.infoworks.utils.rest.client.body.publisher;

import com.infoworks.objects.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;

public interface MultipartBodyPublisher {
    default String contentTypeKey() { return MediaType.Key; } //Content-Type
    String contentTypeValue(MediaType type);
    HttpRequest.BodyPublisher ofMultipartBody(String filename, MediaType fileType, InputStream ios) throws IOException;
}
