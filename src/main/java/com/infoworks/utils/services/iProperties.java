package com.infoworks.utils.services;

import com.infoworks.data.base.DataSource;
import com.infoworks.data.base.DataStorage;
import com.infoworks.objects.Message;
import com.infoworks.utils.services.impl.AppProperties;

import java.io.IOException;
import java.util.Map;

public interface iProperties extends DataSource<String, String>, DataStorage {

    static iProperties create(String name, Map<String, String> defaultSet) {
        return new AppProperties(name, defaultSet);
    }

    void flush();
    String fileName();
    <E extends Message> void putObject(String key, E value) throws IOException;
    <E extends Message> E getObject(String key, Class<E> type) throws IOException;
}
