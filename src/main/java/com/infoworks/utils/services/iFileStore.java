package com.infoworks.utils.services;

import com.infoworks.data.base.iDocument;
import com.infoworks.orm.Property;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface iFileStore<IOStream> extends iDocument<IOStream> {
    boolean save(String location, IOStream file) throws IOException;
    default void retry(boolean async) {}
    String[] fileNames();
    List<File> search(File searchDir, Property...query);
    void prepareZipEntryFrom(List<File> files, OutputStream oStream) throws IOException;
}
