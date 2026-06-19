package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class UploadTask extends PostTask {
    private byte[] uploadBytes;

    public UploadTask() {super();}

    public UploadTask(String uploadUri, byte[] bytesToUpload) {
        super(uploadUri, "", new Property[0]);
        this.uploadBytes = bytesToUpload;
    }

    @Override
    protected Duration connectionTimeout() {
        return Duration.ofSeconds(60);
    }

    public byte[] getUploadBytes() {
        return uploadBytes;
    }

    public void setUploadBytes(byte[] uploadBytes) {
        this.uploadBytes = uploadBytes;
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        Response outcome = new Responses().setStatus(500);
        if (getUploadBytes().length == 0) return outcome.setError("UploadBytes cannot be null or empty.");
        setContentType(MediaType.BINARY_OCTET_STREAM);
        //Files.newInputStream(getUploadFile().toPath())
        try {
            //Prepare request builder:
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getUri()))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(getUploadBytes()));
            //Prepare Http-Headers:
            Map<String, String> headers = createAuthHeader(getToken());
            headers.put("User-Agent", "JavaHttpClient/11");
            headers.put(MediaType.Key, getContentType().value());
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
