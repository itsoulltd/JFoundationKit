package com.infoworks.utils.excel.writer;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class StreamWriter extends AsyncWriter {

    public StreamWriter(Workbook workbook, OutputStream outputStream) {
        super(workbook, outputStream);
    }

    public StreamWriter(int rowSize, OutputStream outputStream) {
        this(new SXSSFWorkbook((rowSize <= 0) ? 100 : rowSize), outputStream);
    }

    public StreamWriter(int rowSize, String fileNameToWrite) throws IOException {
        this(rowSize, new FileOutputStream(fileNameToWrite, true));
    }
}
