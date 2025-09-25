package com.infoworks.utils.rest.client.body.publisher;

import com.infoworks.objects.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MultipartIStreamPublisher implements MultipartBodyPublisher {

    private static String BOUNDARY = "----Boundary" + UUID.randomUUID();
    private static String LINE_FEED = "\r\n";

    public byte[] closing() {
        return (LINE_FEED + "--" + BOUNDARY + "--" + LINE_FEED).getBytes();
    }

    public byte[] preamble(String filename, MediaType fileType) {
        StringBuilder preambleBuilder = new StringBuilder();
        preambleBuilder.append("--").append(BOUNDARY).append(LINE_FEED);
        preambleBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(filename).append("\"").append(LINE_FEED);
        preambleBuilder.append("Content-Type: ").append(fileType.value()).append(LINE_FEED).append(LINE_FEED);
        return preambleBuilder.toString().getBytes();
    }

    public HttpRequest.BodyPublisher ofMultipartBody(String filename, MediaType fileType, InputStream ios) throws IOException {
        final byte[] preamble = preamble(filename, fileType);
        final byte[] closing = closing();
        return HttpRequest.BodyPublishers.ofInputStream(() -> {
            List<InputStream> streams = Arrays.asList(
                    new ByteArrayInputStream(preamble)
                    , ios
                    , new ByteArrayInputStream(closing)
            );
            SequenceInputStream sios = new SequenceInputStream(Collections.enumeration(streams));
            return sios;
        });
    }

    @Override
    public String contentTypeValue(MediaType type) {
        return String.format("%s; boundary=%s", type.value(), BOUNDARY);
    }
}
