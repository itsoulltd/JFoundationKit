package com.infoworks.utils.rest.client.body.publisher;

import com.infoworks.objects.MediaType;
import com.infoworks.orm.Property;
import com.infoworks.orm.Row;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.http.HttpRequest;
import java.util.*;

public class MultipartIStreamPublisher implements MultipartBodyPublisher {

    private static String BOUNDARY = "----Boundary" + UUID.randomUUID();
    private static String LINE_FEED = "\r\n";

    public byte[] closing() {
        return (LINE_FEED + "--" + BOUNDARY + "--" + LINE_FEED).getBytes();
    }

    public byte[] preamble(String name, String filename, String fileType) {
        StringBuilder preambleBuilder = new StringBuilder();
        preambleBuilder.append("--").append(BOUNDARY).append(LINE_FEED);
        preambleBuilder.append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"; filename=\"").append(filename).append("\"").append(LINE_FEED);
        preambleBuilder.append("Content-Type: ").append(fileType).append(LINE_FEED).append(LINE_FEED);
        return preambleBuilder.toString().getBytes();
    }

    public HttpRequest.BodyPublisher ofMultipartBody(InputStream ios, Property...contentDispositions) throws IOException {
        //String name, String filename, String Content-Type;
        Map<String, Property> disposition = convert(contentDispositions);
        String name = disposition.get("name").getValue().toString();
        String filename = disposition.get("filename").getValue().toString();
        String mimeType = disposition.get("Content-Type").getValue().toString();
        //
        final byte[] preamble = preamble(name, filename, mimeType);
        final byte[] data = ios.readAllBytes();
        final byte[] closing = closing();
        return HttpRequest.BodyPublishers.ofInputStream(() -> {
            List<InputStream> streams = Arrays.asList(
                    new ByteArrayInputStream(preamble)
                    , new ByteArrayInputStream(data)
                    , new ByteArrayInputStream(closing)
            );
            SequenceInputStream sios = new SequenceInputStream(Collections.enumeration(streams));
            return sios;
        });
    }

    private Map<String, Property> convert(Property...properties) {
        Row row = new Row();
        row.setProperties(Arrays.asList(properties));
        return row.keyValueMap();
    }

    @Override
    public String contentTypeValue(MediaType type) {
        return String.format("%s; boundary=%s", type.value(), BOUNDARY);
    }
}
