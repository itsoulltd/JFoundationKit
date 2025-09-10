package com.infoworks.data.base;

import com.infoworks.orm.Property;

import java.util.Base64;
import java.util.List;
import java.util.function.BiConsumer;

public interface iDocument<File> extends iDataSource<String, File> {
    File findByName(String name);
    /**
     * TO Search files (containing file-name), pass new Property("filename", "abc");
     * where filename should be the key.
     * TO Search files in Directory (containing directory-name), pass new Property("dirname", "abc");
     * where dirname should be the key.
     * @param query
     * @return List<InputStream>
     */
    List<File> search(Property...query);
    default void search(Property[] query, BiConsumer<Long, List<File>> consumer) {
        if (consumer != null) consumer.accept(0l, search(query));
    }
    default long remove(Property...query) {return 0l;}
    static boolean isValidBase64String(String base64) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            decoder.decode(base64.getBytes());
            return true;
        } catch (Exception e) {}
        return false;
    }
}
