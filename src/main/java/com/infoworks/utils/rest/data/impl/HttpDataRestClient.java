package com.infoworks.utils.rest.data.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.data.impl.SimpleDataSource;
import com.infoworks.objects.MessageParser;
import com.infoworks.objects.Response;
import com.infoworks.orm.Property;
import com.infoworks.utils.rest.client.*;
import com.infoworks.utils.rest.data.DataRestClient;
import com.infoworks.utils.rest.data.model.*;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class HttpDataRestClient<Value extends Any> extends SimpleDataSource<Object, Value> implements DataRestClient<Value> {

    private final URL baseUrl;
    private ExecutorService service;
    private HttpClient template;
    private PaginatedResponse baseResponse;
    private Class<? extends Any> anyClassType;
    private boolean enableLogging;
    private ObjectMapper mapper;
    private String token;

    public HttpDataRestClient(Class<? extends Any> anyClassType, URL baseUrl, HttpClient template, ExecutorService service, boolean enableLogging) {
        this.baseUrl = baseUrl;
        this.service = service;
        this.template = template;
        this.anyClassType = anyClassType;
        this.enableLogging = enableLogging;
    }

    protected ExecutorService getService() {
        if (service == null){
            service = Executors.newSingleThreadExecutor();
        }
        return service;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    @Override
    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public void close() throws Exception {
        //Do all memory clean-up and terminate running process:
        clear();
        baseResponse = null;
        //immediate shutdown all enqueued tasks and return
        service.shutdown();
        service = null;
    }

    /**
     * execute(HttpActions)
     * @param method
     * @param entity
     * @param rootURL
     * @return
     * @throws RuntimeException
     */
    protected String exchange(HttpMethod method, Map<String, Object> entity, String rootURL, Property...args)
            throws RuntimeException {
        Response response = null;
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            RestTask task = (method == HttpMethod.POST)
                    ? new PostTask(rootURL, "", args)
                    : new PutTask(rootURL, "", args);
            task.setClient(template);
            task.setToken(getToken());
            task.setBody(entity);
            response = task.execute(null);
        } else if (method == HttpMethod.DELETE) {
            DeleteTask task = new DeleteTask(rootURL, "", args);
            task.setClient(template);
            task.setToken(getToken());
            response = task.execute(null);
        } else { //GET
            GetTask task = new GetTask(rootURL, "", args);
            task.setClient(template);
            task.setToken(getToken());
            response = task.execute(null);
        }
        //Handle response:
        if (response.getStatus() >= 400)
            throw new RuntimeException( response.getError() + ". Status Code: " + response.getStatus());
        String result = response.getMessage();
        if (result == null || result.isEmpty())
            result = "Response Code: " + response.getStatus();
        return result;
    }

    /**
     * Load the baseUrl and its result into local cache.
     * If we need to re-load, then first do close() and then call load() again.
     * @return
     * @throws RuntimeException
     */
    public PaginatedResponse load() throws RuntimeException {
        if (baseResponse != null) return baseResponse;
        //Load the base URL:
        Map body = new HashMap();
        String rootURL = baseUrl.toString();
        try {
            String result = exchange(HttpMethod.GET, body, rootURL);
            if(isEnableLogging()) System.out.println(result);
            Map<String, Object> dataMap = MessageParser.unmarshal(new TypeReference<Map<String, Object>>() {}, result, getMapper());
            baseResponse = new PaginatedResponse(dataMap);
            //Parse page items and cache in local:
            parsePageItemAndCacheInMemory(dataMap);
            return baseResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Value> parsePageItemAndCacheInMemory(Map<String, Object> dataMap) {
        //Parse items:
        List<Value> items = parsePageItems(dataMap);
        //Add into in-memory store:
        if (items != null || !items.isEmpty()) {
            items.forEach(item -> {
                Object key = item.parseId().orElse(null);
                if(key != null) super.put(key.toString(), item);
            });
        }
        return items;
    }

    protected List<Value> parsePageItems(Map<String, Object> dataMap) {
        List<Value> typedObjects = new ArrayList<>();
        if (dataMap == null) return typedObjects;
        //Parse DataMap to get-objects:
        Map<String, List<Map<String, Object>>> embedded =
                (Map) dataMap.get("_embedded");
        List<Map<String, Object>> objects = getCollectionResourceRel(embedded);
        if (objects == null) return typedObjects;
        //Try to re-recreate objects:
        for (Map<String, Object> entry : objects) {
            try {
                Value parsed = (Value) anyClassType.newInstance();
                parsed.unmarshalling(entry, true);
                typedObjects.add(parsed);
            } catch (InstantiationException
                     | IllegalAccessException e) {}
        }
        return typedObjects;
    }

    /**
     * Declared in Spring-Data-Rest repository annotation:
     * e.g. @RepositoryRestResource(collectionResourceRel = "passengers")
     * @return
     */
    protected List<Map<String, Object>> getCollectionResourceRel(Map<String, List<Map<String, Object>>> embedded) {
        if (embedded == null) return null;
        Optional<String> possibleKey = embedded.keySet().stream().findFirst();
        return possibleKey.isPresent() ? embedded.get(possibleKey.get()) : null;
    }

    /**
     * Declared in Spring-Data-Rest repository annotation:
     * e.g. @RepositoryRestResource(path = "passengers")
     * @return
     */
    protected String getApiPathName() {
        String path = baseUrl.getPath();
        String[] paths = path.split("/");
        String pathName = paths[paths.length - 1];
        return pathName;
    }

    /**
     * Load asynchronously the baseUrl and its result into local cache.
     * @param consumer
     */
    public void load(Consumer<PaginatedResponse> consumer) {
        if (consumer == null) return;
        if (baseResponse != null)
            consumer.accept(baseResponse);
        //Load the base URL:
        getService().submit(() -> {
            try {
                baseResponse = load();
                consumer.accept(baseResponse);
            } catch (RuntimeException e) {
                PaginatedResponse response = new PaginatedResponse();
                response.setError(e.getMessage());
                response.setStatus(400);
                consumer.accept(response);
            }
        });
    }

    /**
     * Fetch Next page Until the End of Line.
     * Also add paged items into local cache.
     * @return
     */
    public Optional<List<Value>> next() {
        if (isLastPage()) return Optional.ofNullable(null);
        if (baseResponse != null){
            Page page = baseResponse.getPage();
            Map<String, Object> dataMap = fetchNext(page).orElse(null);
            //Update Next page info:
            baseResponse.updatePage(dataMap);
            baseResponse.updateLinks(dataMap);
            List<Value> items = parsePageItemAndCacheInMemory(dataMap);
            return Optional.ofNullable(items);
        }
        return Optional.ofNullable(null);
    }

    /**
     * May return Null
     * @param current
     * @return
     */
    protected Optional<Map<String, Object>> fetchNext(Page current) {
        int currentPage = current.getNumber();
        int pageSize = current.getSize();
        int nextPage = currentPage + 1;
        //
        Map body = new HashMap();
        String nextPagePath = baseUrl.toString() + "?page={page}&size={size}";
        String result = exchange(HttpMethod.GET, body, nextPagePath, new Property("page", nextPage), new Property("size", pageSize));
        if(isEnableLogging()) System.out.println(result);
        //
        Map<String, Object> dataMap = null;
        try {
            dataMap = MessageParser.unmarshal(
                    new TypeReference<Map<String, Object>>() {}, result, getMapper());
        } catch (IOException e) {}
        return Optional.ofNullable(dataMap);
    }

    /**
     * Asynchronous version of next()
     * @param consumer
     */
    public void next(Consumer<Optional<List<Value>>> consumer) {
        if (consumer != null) {
            getService().submit(() -> consumer.accept(next()));
        }
    }

    /**
     * Search using declared func-name and search params:
     * @param function
     * @param params
     * @return
     */
    public Optional<List<Value>> search(String function, Property...params) {
        if (Objects.isNull(function) || function.isEmpty()) return Optional.ofNullable(null);
        if (function.startsWith("/")) function = function.replaceFirst("/", "");
        PaginatedResponse response = load();
        Object href = response.getLinks().getSearch().get("href");
        if (href != null) {
            String searchAction = function + encodedQueryParams(params);
            String searchUrl = href + "/" + searchAction;
            String result = exchange(HttpMethod.GET, new HashMap<>(), searchUrl);
            try {
                Map<String, Object> dataMap =
                        MessageParser.unmarshal(new TypeReference<Map<String, Object>>() {}, result, getMapper());
                List<Value> items = parsePageItems(dataMap);
                return Optional.ofNullable(items);
            } catch (IOException e) {}
        }
        return Optional.ofNullable(null);
    }

    /**
     * Async version of search:
     * @param function
     * @param params
     * @param consumer
     */
    public void search(String function, Property[] params, Consumer<Optional<List<Value>>> consumer) {
        if (consumer != null) {
            getService().submit(() -> consumer.accept(search(function, params)));
        }
    }

    public boolean isSearchActionExist(String function) {
        boolean outcome = false;
        if (Objects.isNull(function) || function.isEmpty()) return outcome;
        if (function.startsWith("/")) function = function.replaceFirst("/", "");
        PaginatedResponse response = load();
        Object href = response.getLinks().getSearch().get("href");
        if (href != null) {
            String searchUrl = href.toString();
            String result = exchange(HttpMethod.GET, new HashMap<>(), searchUrl);
            try {
                Map<String, Object> dataMap =
                        MessageParser.unmarshal(new TypeReference<Map<String, Object>>() {}, result, getMapper());
                //outcome
                Object data = dataMap.get("_links");
                if (data != null && data instanceof Map) {
                    Map<String, Object> functions = (Map<String, Object>) data;
                    outcome = functions.containsKey(function);
                }
            } catch (IOException e) {}
        }
        return outcome;
    }

    /**
     * "page" : {
     *     "size" : 5,
     *     "totalElements" : 50,
     *     "totalPages" : 10,
     *     "number" : 0
     *   }
     * At the bottom is extra data about the page settings,
     * including the size of a page, total elements, total pages, and the page number you are currently viewing.
     * @return
     */
    public boolean isLastPage() {
        if (baseResponse == null) return true;
        Page current = baseResponse.getPage();
        int currentPage = current.getNumber();
        return (currentPage >= current.getTotalPages()) ? true : false;
    }

    @Override
    public int currentPage() {
        return number();
    }

    @Override
    public int number() {
        if (baseResponse == null) return 0;
        Page current = baseResponse.getPage();
        return current.getNumber();
    }

    @Override
    public int totalPages() {
        if (baseResponse == null) return 0;
        Page current = baseResponse.getPage();
        return current.getTotalPages();
    }

    @Override
    public int totalElements() {
        if (baseResponse == null) return 0;
        Page current = baseResponse.getPage();
        return current.getTotalElements();
    }

    /**
     * Read from cache if not exist then fetch from server and update the cache.
     * @param key
     * @return
     * @throws RuntimeException
     */
    @Override
    public Value read(Object key) throws RuntimeException {
        //First check in Cache:
        key = key.toString();
        Value any = super.read(key);
        if (any != null) return any;
        //Read will do GET
        try {
            Map<String, Object> body = new HashMap();
            String getPath = baseUrl.toString() + "/" + key;
            String getResult = exchange(HttpMethod.GET, body, getPath);
            if(isEnableLogging()) System.out.println(getResult);
            Value value = (Value) MessageParser.unmarshal(anyClassType, getResult, getMapper());
            super.put(key, value);
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Remove an Entity from server and cache.
     * @param key
     * @return
     * @throws RuntimeException
     */
    @Override
    public Value remove(Object key) throws RuntimeException {
        //Remove will do DELETE
        key = key.toString();
        Map<String, Object> body = new HashMap();
        String deletePath = baseUrl.toString() + "/" + key;
        String deleteResult = exchange(HttpMethod.DELETE, body, deletePath);
        if(isEnableLogging()) System.out.println(deleteResult);
        //Now remove from local if exist in cache:
        Value any = super.remove(key);
        return any;
    }

    @Override
    public void delete(Value value) throws RuntimeException {
        this.remove(value.getId());
    }

    /**
     * Create an Entity from server and cache.
     * @param value
     * @return
     * @throws RuntimeException
     */
    @Override
    public Object add(Value value) throws RuntimeException {
        //Add will do POST
        try {
            Map<String, Object> postBody = value.marshalling(true);
            String rootURL = baseUrl.toString();
            String result = exchange(HttpMethod.POST, postBody, rootURL);
            if(isEnableLogging()) System.out.println(result);
            Value created = (Value) MessageParser.unmarshal(anyClassType, result, getMapper());
            Object key = created.parseId().orElse(null);
            if(key != null) super.put(key.toString(), value);
            return key;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create Or Update an Entity from server and cache.
     * @param key
     * @param value
     * @throws RuntimeException
     */
    @Override
    public void put(Object key, Value value) throws RuntimeException {
        //Put will do PUT
        key = key.toString();
        Map<String, Object> putBody = value.marshalling(true);
        String updatePath = baseUrl.toString() + "/" + key;
        String updateResult = exchange(HttpMethod.PUT, putBody, updatePath);
        if(isEnableLogging()) System.out.println(updateResult);
        if(containsKey(key))
            super.replace(key, value);
    }
}
