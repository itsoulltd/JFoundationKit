package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

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

    @Override
    protected Duration connectionTimeout() {
        return Duration.ofSeconds(60);
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        Response outcome = new Responses().setStatus(500);
        if (fileType == null) return outcome.setError("MediaType cannot be null or empty.");
        if (uploadFile == null) return outcome.setError("UploadFile cannot be null or empty.");
        setContentType(MediaType.MULTIPART_FORM_DATA);
        try (FileInputStream inputStream = new FileInputStream(this.uploadFile)) {
            //Prepare request builder:
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getUri()))
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> inputStream));
            //Prepare Http-Headers:
            Map<String, String> headers = createAuthHeader(getToken());
            headers.put("User-Agent", "JavaHttpClient/11");
            headers.put(getContentType().key(), getContentType().value());
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
