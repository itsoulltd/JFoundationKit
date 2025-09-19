package com.infoworks.utils.rest.client;

import com.infoworks.objects.Message;
import com.infoworks.objects.Response;
import com.infoworks.objects.Responses;
import com.infoworks.orm.Property;

import java.util.*;
import java.util.function.Consumer;

public class GetTask extends RestTask<Message, Response> {

    public GetTask() {super();}

    public GetTask(String baseUri, String requestUri, Property...params) {
        super(baseUri, requestUri, params);
        updateQueryParams(params);
    }

    public GetTask(String baseUri, String requestUri, Property[] params, Consumer<String> response) {
        this(baseUri, requestUri, params);
        addResponseListener(response);
    }

    private Map<String, Object> paramsKeyMaps = new HashMap<>();

    private void updateRequestUriWithQueryParams(String requestUri, Property[] params) {
        //Update paths?<query-params>
        String queryParam = urlencodedQueryParam(params);
        requestUri = requestUri.trim();
        if (requestUri.contains("?")) {
            String paths = requestUri.substring(0, requestUri.indexOf("?"));
            setRequestUri(paths + queryParam);
        } else {
            setRequestUri(requestUri + queryParam);
        }
    }

    public void updateQueryParams(Property...params) {
        //Filter-Out null and empty:
        Arrays.stream(params)
                .filter(param -> param.getValue() != null && !param.getValue().toString().isEmpty())
                .forEach(param -> paramsKeyMaps.put(param.getKey(), param.getValue()));
        //
        List<Property> paramList = new ArrayList<>();
        this.paramsKeyMaps.forEach((key, value) -> paramList.add(new Property(key, value.toString())));
        updateRequestUriWithQueryParams(this.requestUri, paramList.toArray(new Property[0]));
    }

    @Override
    public Response execute(Message message) throws RuntimeException {
        /*RestTemplate template = getClient();
        try {
            ResponseEntity<String> response = (getParams().length > 0)
                    ? template.exchange(getUri(), HttpMethod.GET, getBody(), String.class, getParams())
                    : template.exchange(getUri(), HttpMethod.GET, getBody(), String.class);
            if (getResponseListener() != null)
                getResponseListener().accept(response.getBody());
            return (Response) new Response()
                    .setStatus(200)
                    .setMessage(getUri())
                    .setPayload(response.getBody());
        } catch (Exception e) {
            return new Response()
                    .setStatus(500)
                    .setMessage(getUri())
                    .setError(e.getMessage());
        }*/
        //TODO:
        return new Responses().setStatus(500);
    }
}
