package com.infoworks.utils.excel.writer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class AsyncWriter implements AutoCloseable {

    protected final Workbook workbook;
    protected final OutputStream outputStream;

    public AsyncWriter(Workbook workbook, OutputStream outputStream) {
        this.workbook = workbook;
        this.outputStream = outputStream;
    }

    public AsyncWriter(boolean xssf, OutputStream outputStream) throws IOException {
        this(WorkbookFactory.create(xssf), outputStream);
    }

    public AsyncWriter(boolean xssf, String fileNameToWrite) throws IOException {
        this(xssf, new FileOutputStream(fileNameToWrite, true));
    }

    public void flush() throws IOException {
        if (workbook != null)
            workbook.write(outputStream);
    }

    @Override
    public void close() throws Exception {
        if (workbook != null) {
            if (outputStream != null) {
                outputStream.close();
            }
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
            workbook.close();
        }
    }

    public void write(String sheetName, Map<Integer, List<String>> data) {
        write(sheetName, data, true);
    }

    public void write(String sheetName, Map<Integer, List<String>> data, boolean skipZeroIndex) {
        //DoTheMath:
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) sheet = workbook.createSheet(sheetName);
        int rowIndex = 0;
        for (Map.Entry<Integer, List<String>> entry : data.entrySet()) {
            Row row = sheet.createRow((skipZeroIndex) ? entry.getKey() : rowIndex);
            int cellIndex = 0;
            for (String cellVal : entry.getValue()) {
                Cell cell = row.createCell(cellIndex);
                cell.setCellValue(cellVal);
                if (sheet instanceof XSSFSheet)
                    sheet.autoSizeColumn(cellIndex);
                cellIndex++;
            }
            rowIndex++;
        }
    }

    public static Map<Integer, List<String>> convert(List<Map<String, Object>> response, int startIndex, String... keys) {
        List<String> keysList = Arrays.stream(keys).collect(Collectors.toList());
        Map<Integer, List<String>> result = new HashMap<>();
        int index = Math.max(startIndex, 0);
        for (Map<String, Object> row : response) {
            List<String> rowList = keysList.stream()
                    .map(key -> Optional.ofNullable(row.get(key)).orElse("").toString())
                    .collect(Collectors.toList());
            result.put(index++, rowList);
        }
        return result;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public Workbook getWorkbook() {
        return workbook;
    }
}
