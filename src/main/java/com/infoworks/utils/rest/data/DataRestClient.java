package com.infoworks.utils.rest.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.data.base.iDataSource;
import com.infoworks.orm.Property;
import com.infoworks.utils.rest.data.model.Any;
import com.infoworks.utils.rest.data.model.PaginatedResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface DataRestClient<Value extends Any> extends iDataSource<Object, Value>, AutoCloseable {
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
