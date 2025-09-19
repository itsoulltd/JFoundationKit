package com.infoworks.utils.rest.client;

import com.infoworks.objects.MediaType;
import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class DownloadTask extends GetTask {

    public DownloadTask() {super();}

    public DownloadTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
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
            //e.g. check content-length or x-ttfb-l
            if (getHeaders() != null && this.contentLength == 0l) {
                String val = getHeaders().firstValue("content-length").orElse("0l");
                if (val.equals("0l"))
                    val = getHeaders().firstValue("x-ttfb-l").orElse("0l");
                try {
                    this.contentLength = Long.valueOf(val);
                } catch (NumberFormatException e) {}
            }
            return contentLength;
        }

        public String filename() {
            //e.g. content-disposition=[attachment; filename=6115759179_86316c08ff_z.jpg]
            if (getHeaders() != null && this.filename == null) {
                String vals = getHeaders().firstValue("content-disposition").orElse(null);
                if (vals != null && !vals.isEmpty()) {
                    String[] parsed = vals.split("; filename=");
                    if (parsed != null && parsed.length > 0)
                        this.filename = parsed[1];
                    else this.filename = parsed[0];
                }
            }
            return filename;
        }

        public MediaType mediaType() {
            //e.g. content-type=[image/jpeg]
            if (getHeaders() != null && this.mediaType == null) {
                String vals = getHeaders().firstValue("content-type").orElse(null);
                this.mediaType = (vals != null) ? new MediaType(vals, null) : MediaType.BINARY_OCTET_STREAM;
            }
            return mediaType;
        }
    }
}
