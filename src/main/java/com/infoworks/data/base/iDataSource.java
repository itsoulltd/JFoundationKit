package com.infoworks.data.base;

import com.infoworks.data.impl.SimpleDataSource;

import java.util.function.Consumer;

public interface iDataSource<Key, Value> {
    default Value read(Key key) {return null;}
    default Value[] readSync(int offset, int pageSize) {return null;}
    default void readAsync(int offset, int pageSize, Consumer<Value[]> consumer) {
        if (consumer != null)
            consumer.accept(null);
    }

    /**
     * Paginate over iDataSource. if pageCount <= 0 then all items will be iterate-over.
     * Page count means number of pages data you wanted to iterate over.
     * @param dataSource
     * @param pageSize
     * @param pageCount
     * @param consumer
     * @param <Key>
     * @param <Value>
     */
    static <Key, Value> void paginateOver(SimpleDataSource<Key, Value> dataSource, int pageSize, int pageCount, Consumer<Object[]> consumer) {
        //Null Check:
        if (consumer == null) {
            return;
        }
        //Validation:
        pageSize = (pageSize <= 0) ? 5 : pageSize;
        int maxCount = (pageSize == dataSource.size()) ? 1 : (dataSource.size() / pageSize) + 1;
        pageCount = (pageCount <= 0 || pageCount > maxCount) ? maxCount : pageCount;
        //Works:
        int currentPage = 1;
        int offset = 0; //iDataSource::readAsync is 0-based;
        while (currentPage <= pageCount) {
            Object[] objs  = dataSource.readSync(offset, pageSize);
            consumer.accept(objs);
            //Next page & offset:
            currentPage++;
            offset = (currentPage - 1) * pageSize;
        }
    }

    default boolean containsKey(Key key) {return false;}
    default void put(Key key, Value value) {}
    default Value remove(Key key) {return null;}
    default Value replace(Key key, Value value) {return null;}

    default boolean contains(Value value) throws RuntimeException {
        return containsKey((Key) Integer.valueOf(value.hashCode()));
    }
    default Key add(Value value) throws RuntimeException {
        Key key = (Key) Integer.valueOf(value.hashCode());
        put(key, value);
        return key;
    }
    default void delete(Value value) throws RuntimeException {
        remove((Key) Integer.valueOf(value.hashCode()));
    }

    default int size() {return 0;}
    default void clear() {}
}
