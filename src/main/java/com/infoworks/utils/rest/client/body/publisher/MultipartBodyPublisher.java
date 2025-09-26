package com.infoworks.utils.rest.client.body.publisher;

import com.infoworks.objects.MediaType;
import com.infoworks.orm.Property;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;

public interface MultipartBodyPublisher {
    default String contentTypeKey() { return MediaType.Key; } //Content-Type
    String contentTypeValue(MediaType type);
    HttpRequest.BodyPublisher ofMultipartBody(InputStream ios, Property...contentDispositions) throws IOException;
    default HttpRequest.BodyPublisher ofMultipartBody(InputStream ios, String nameKey, String filename, MediaType mimeType) throws IOException {
        return ofMultipartBody(ios, new Property("name", nameKey), new Property("filename", filename), new Property("Content-Type", mimeType.value()));
    }
}
