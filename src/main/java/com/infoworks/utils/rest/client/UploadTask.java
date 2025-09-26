package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;
import com.infoworks.utils.rest.client.body.publisher.MultipartBodyPublisher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;

public class UploadTask extends PostTask {
    private String contentDispositionNameKey = "file";
    private MediaType mimeType;
    private File uploadFile;
    private MultipartBodyPublisher bodyPublisher;

    public UploadTask() {super();}

    public UploadTask(String uploadUri, MediaType mimeType, File uploadFile) {
        super(uploadUri, "", new Property[0]);
        this.mimeType = mimeType;
        this.uploadFile = uploadFile;
    }

    public MediaType getMimeType() {
        if (this.mimeType == null) {
            //Let's try to get from the file it-self:
            try {
                String mediaType = Files.probeContentType(getUploadFile().toPath());
                this.mimeType = new MediaType(mediaType, null);
            } catch (Exception e) {
                this.mimeType = MediaType.BINARY_OCTET_STREAM;
            }
        }
        return mimeType;
    }

    public void setMimeType(MediaType mimeType) {
        this.mimeType = mimeType;
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

    public MultipartBodyPublisher getBodyPublisher() {
        return bodyPublisher;
    }

    public void setBodyPublisher(MultipartBodyPublisher bodyPublisher) {
        this.bodyPublisher = bodyPublisher;
    }

    @Override
    protected Duration connectionTimeout() {
        return Duration.ofSeconds(60);
    }

    public String getContentDispositionNameKey() {
        return contentDispositionNameKey;
    }

    public void setContentDispositionNameKey(String contentDispositionNameKey) {
        this.contentDispositionNameKey = contentDispositionNameKey;
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        Response outcome = new Responses().setStatus(500);
        if (getMimeType() == null) return outcome.setError("MediaType cannot be null or empty.");
        if (getUploadFile() == null) return outcome.setError("UploadFile cannot be null or empty.");
        if (getBodyPublisher() == null) return outcome.setError("MultipartBodyPublisher cannot be null or empty.");
        setContentType(MediaType.MULTIPART_FORM_DATA);
        //Files.newInputStream(getUploadFile().toPath())
        try (InputStream inputStream = new FileInputStream(getUploadFile())) {
            //Prepare request builder:
            MultipartBodyPublisher publisher = getBodyPublisher();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getUri()))
                    .POST(publisher.ofMultipartBody(inputStream, getContentDispositionNameKey(), getFilename(), getMimeType()));
            //Prepare Http-Headers:
            Map<String, String> headers = createAuthHeader(getToken());
            headers.put("User-Agent", "JavaHttpClient/11");
            headers.put(publisher.contentTypeKey(), publisher.contentTypeValue(getContentType()));
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
}
