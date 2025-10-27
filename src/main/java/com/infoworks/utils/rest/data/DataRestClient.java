package com.infoworks.utils.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.data.base.iDataSource;
import com.infoworks.orm.Property;
import com.infoworks.utils.rest.data.impl.HttpDataRestClient;
import com.infoworks.utils.rest.data.model.Any;
import com.infoworks.utils.rest.data.model.PaginatedResponse;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public interface DataRestClient<Value extends Any> extends iDataSource<Object, Value>, AutoCloseable {

    static DataRestClient defaultClient(Class<? extends Any> anyClassType, URL baseUrl, HttpClient template, ExecutorService service, boolean enableLogging) {
        return new HttpDataRestClient(anyClassType, baseUrl, template, service, enableLogging);
    }

    static DataRestClient defaultClient(Class<? extends Any> anyClassType, URL baseUrl, boolean enableLogging) {
        HttpClient template = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(700))
                .build();
        ExecutorService service = Executors.newSingleThreadExecutor();
        return defaultClient(anyClassType, baseUrl, template, service, enableLogging);
    }

    PaginatedResponse load();
    void load(Consumer<PaginatedResponse> consumer);

    Optional<List<Value>> next();
    void next(Consumer<Optional<List<Value>>> consumer);

    Optional<List<Value>> search(String function, Property...params);
    void search(String function, Property[] params, Consumer<Optional<List<Value>>> consumer);
    boolean isSearchActionExist(String function);

    boolean isLastPage();
    int currentPage();
    int number();
    int totalPages();
    int totalElements();
    ObjectMapper getMapper();
    void setMapper(ObjectMapper mapper);

    default String encodedQueryParams(Property...params) {
        StringBuilder buffer = new StringBuilder("?");
        for (Property query : params) {
            if (query.getValue() == null || query.getValue().toString().isEmpty()) continue;
            try {
                buffer.append(
                        URLEncoder.encode(query.getKey(), "UTF-8")
                                + "="
                                + URLEncoder.encode(query.getValue().toString(), "UTF-8")
                                + "&");
            } catch (UnsupportedEncodingException e) {}
        }
        String value = buffer.toString();
        value = value.substring(0, value.length() - 1);
        return value;
    }
}
