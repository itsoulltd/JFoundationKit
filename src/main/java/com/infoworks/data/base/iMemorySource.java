package com.infoworks.data.base;

import java.util.Map;

public interface iMemorySource extends iDataSource<String, Map<String, Object>>, AutoCloseable {
    void put(String key, Map<String, Object> entity, long ttl);
    void setTimeToLive(long ttl);
    long getTimeToLive();
    boolean isConnectionOpen();
    default String[] keys(String prefix) { return new String[0]; }
}
