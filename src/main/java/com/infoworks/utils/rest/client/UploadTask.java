package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

public class UploadTask extends PostTask {

    private MediaType fileType;
    private File uploadFile;

    public UploadTask() {super();}

    public UploadTask(String uploadUri, MediaType fileType, File uploadFile) {
        super(uploadUri, "", new Property[0]);
        this.fileType = fileType;
        this.uploadFile = uploadFile;
    }

    public MediaType getFileType() {
        if (this.fileType == null) {
            //Let's try to get from the file it-self:
            try {
                String mediaType = Files.probeContentType(getUploadFile().toPath());
                this.fileType = new MediaType(mediaType, null);
            } catch (Exception e) {
                this.fileType = MediaType.BINARY_OCTET_STREAM;
            }
        }
        return fileType;
    }

    public void setFileType(MediaType fileType) {
        this.fileType = fileType;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(File uploadFile) {
        this.uploadFile = uploadFile;
    }

    public String getFilename() {
        if (getUploadFile() != null) {
            return getUploadFile().getName();
        }
        return null;
    }

    @Override
    protected Duration connectionTimeout() {
        return Duration.ofSeconds(60);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        Response outcome = new Responses().setStatus(500);
        if (getFileType() == null) return outcome.setError("MediaType cannot be null or empty.");
        if (getUploadFile() == null) return outcome.setError("UploadFile cannot be null or empty.");
        setContentType(MediaType.MULTIPART_FORM_DATA);
        //Files.newInputStream(getUploadFile().toPath())
        try (InputStream inputStream = new FileInputStream(getUploadFile())) {
            //Prepare request builder:
            byte[] pre = preamble(getFilename(), getFileType());
            byte[] cls = closing();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getUri()))
                    .POST(ofMultipartBody(pre, inputStream, cls));
            //Prepare Http-Headers:
            Map<String, String> headers = createAuthHeader(getToken());
            headers.put("User-Agent", "JavaHttpClient/11");
            headers.put(getContentType().key(), String.format("%s; boundary=%s", getContentType().value(), BOUNDARY));
            headers.put("accept", "*/*");
            headers.forEach(builder::header);
            //POST file-upload:
            HttpClient client = getClient();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            outcome = new Responses().setStatus(response.statusCode()).setMessage(response.body());
        } catch (IOException | InterruptedException e) {
            outcome.setError(e.getMessage());
        }
        return outcome;
    }

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

    public HttpRequest.BodyPublisher ofMultipartBody(byte[] preamble, InputStream ios, byte[] closing) {
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
}
