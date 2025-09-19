package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.utils.services.iResources;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;

public class DownloadTask extends GetTask {

    public DownloadTask() {super();}

    public DownloadTask(String baseUri, String requestUri, Object...params) {
        super(baseUri, requestUri, params);
    }

    public DownloadTask(String baseUri, String requestUri, Consumer<String> response) {
        super(baseUri, requestUri, response);
    }

    @Override
    public ResourceResponse execute(Message message) throws RuntimeException {
        ResourceResponse outcome = (ResourceResponse) new ResourceResponse().setStatus(500);
        //Prepare request builder:
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(getUri()))
                .GET();
        //Prepare Http-Headers:
        Map<String, String> headers = createHeaderFrom(getToken());
        headers.put("User-Agent", "JavaHttpClient/11");
        headers.put("Accept", "*/*");
        headers.forEach(builder::header);
        //GET the file-download:
        try {
            HttpClient client = getClient();
            HttpResponse<InputStream> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            HttpHeaders responseHeaders = response.headers();
            outcome.setHeaders(responseHeaders);
            outcome.setStatus(response.statusCode());
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                outcome.setResource(response.body());
                //Now convert into base64 string and accept-consumer:
                if (getResponseListener() != null && response.body() != null) {
                    String base64Encoded = null;
                    try (InputStream iso = response.body()) {
                        iResources service = iResources.create();
                        byte[] bytes = service.readAsBytes(iso);
                        base64Encoded = new String(Base64.getEncoder().encode(bytes), "UTF-8");
                    }
                    getResponseListener().accept(base64Encoded);
                }
            } else {
                outcome.setError("Failed to download file. HTTP status code: " + statusCode
                        + "\nResponse headers: " + responseHeaders.map());
            }
        } catch (IOException e) {
            outcome.setError(e.getMessage());
        } catch (InterruptedException e) {
            outcome.setError(e.getMessage());
        }
        return outcome;
    }

    public static class ResourceResponse extends Response {
        private InputStream resource;
        private HttpHeaders headers;
        private long contentLength = 0l;
        private String filename;
        private MediaType mediaType;

        public InputStream getResource() {
            return resource;
        }

        public ResourceResponse setResource(InputStream resource) {
            this.resource = resource;
            return this;
        }

        public HttpHeaders getHeaders() {
            return headers;
        }

        public ResourceResponse setHeaders(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }

        public long contentLength() {
            if (getHeaders() != null && this.contentLength == 0l) {
                getHeaders().firstValue("Content-Length").ifPresent(length -> {
                    try {
                        this.contentLength = Long.valueOf(length);
                    } catch (NumberFormatException e) {}
                });
            }
            return contentLength;
        }

        public String filename() {
            if (getHeaders() != null && this.filename == null) {
                //TODO:
            }
            return filename;
        }

        public MediaType mediaType() {
            if (getHeaders() != null && this.mediaType == null) {
                //TODO:
                this.mediaType = MediaType.BINARY_OCTET_STREAM;
            }
            return mediaType;
        }
    }
}
