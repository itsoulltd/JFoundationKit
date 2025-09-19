package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class UploadTask extends RestTask<Message, Response> {

    private MediaType type;
    private File uploadFile;

    public UploadTask() {super();}

    public UploadTask(String uploadUri, MediaType type, File uploadFile) {
        super(uploadUri, "", new Property[0]);
        this.type = type;
        this.uploadFile = uploadFile;
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        Response outcome = new Responses().setStatus(500);
        if (type == null) return outcome.setError("MediaType cannot be null or empty.");
        if (uploadFile == null) return outcome.setError("UploadFile cannot be null or empty.");
        try (FileInputStream inputStream = new FileInputStream(this.uploadFile)) {
            //Prepare request builder:
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(getUri()))
                    .header(this.type.key(), this.type.value())
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> inputStream));
            //Prepare Http-Headers:
            Map<String, String> headers = createHeaderFrom(getToken());
            headers.put("User-Agent", "JavaHttpClient/11");
            headers.forEach(builder::header);
            //POST file-upload:
            HttpClient client = getClient();
            HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            outcome = new Responses().setStatus(response.statusCode()).setMessage(response.body());
        } catch (FileNotFoundException e) {
            outcome.setError(e.getMessage());
        } catch (IOException e) {
            outcome.setError(e.getMessage());
        } catch (InterruptedException e) {
            outcome.setError(e.getMessage());
        }
        return outcome;
    }
}
