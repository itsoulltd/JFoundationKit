package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.utils.services.iResources;

import java.io.InputStream;
import java.util.Base64;
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
        /*RestTemplate template = getClient();
        try {
            ResponseEntity<Resource> response = (getParams().length > 0)
                    ? template.exchange(getUri(), HttpMethod.GET, getBody(), Resource.class, getParams())
                    : template.exchange(getUri(), HttpMethod.GET, getBody(), Resource.class);
            if (getResponseListener() != null) {
                String base64Encoded = null;
                if (response.hasBody()) {
                    try (InputStream iso = response.getBody().getInputStream()) {
                        if (iso != null) {
                            iResources service = iResources.create();
                            byte[] bytes = service.readAsBytes(iso);
                            base64Encoded = new String(Base64.getEncoder().encode(bytes), "UTF-8");
                        }
                    }
                }
                getResponseListener().accept(base64Encoded);
            }
            return (ResourceResponse) new ResourceResponse()
                    .setResource(response.getBody())
                    .setStatus(200)
                    .setMessage(getUri());
        } catch (Exception e) {
            return (ResourceResponse) new ResourceResponse()
                    .setResource(null)
                    .setStatus(500)
                    .setMessage(getUri())
                    .setError(e.getMessage());
        }*/
        //TODO:
        return (ResourceResponse) new ResourceResponse().setStatus(500);
    }

    public static class ResourceResponse extends Response {
        private InputStream resource;

        public InputStream getResource() {
            return resource;
        }

        public ResourceResponse setResource(InputStream resource) {
            this.resource = resource;
            return this;
        }
    }
}
