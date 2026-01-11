package com.infoworks.utils.services;

import com.infoworks.data.base.iDataSource;
import com.infoworks.data.base.iDataStore;
import com.infoworks.objects.Message;
import com.infoworks.utils.services.impl.AppProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface iProperties extends iDataSource<String, String>, iDataStore {

    static iProperties create(String name, Map<String, String> defaultSet) {
        return new AppProperties(name, defaultSet);
    }

    static iProperties createInMemory(InputStream ios, Map<String, String> defaultSet) {
        return new AppProperties(ios, defaultSet);
    }

    boolean isInMemory();
    void flush();
    String fileName();
    <E extends Message> void putObject(String key, E value) throws IOException;
    <E extends Message> E getObject(String key, Class<E> type) throws IOException;
}
