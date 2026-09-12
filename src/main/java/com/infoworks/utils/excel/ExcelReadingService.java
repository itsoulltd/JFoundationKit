package com.infoworks.utils.excel;

import com.infoworks.data.base.iDataSource;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExcelReadingService implements iDataSource<Integer, List<String>> {

    private static final Logger LOG = Logger.getLogger(ExcelReadingService.class.getSimpleName());
    private final File file;

    public ExcelReadingService(File file) {
        this.file = file;
    }

    @Override
    public int size() {
        try {
            return size(0);
        } catch (IOException ignored) {}
        return 0;
    }

    public int size(Integer sheetAt) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file)) {
            configureWorkbook(workbook);
            Sheet sheet = workbook.getSheetAt(sheetAt);
            int maxCount = sheet.getLastRowNum() + 1;
            return maxCount;
        }
    }

    @Override @SuppressWarnings("unchecked")
    public List<String>[] readSync(int offset, int pageSize) {
        try {
            int till = offset + pageSize;
            Map<Integer, List<String>> data = read(0, offset, till);
            return data.values().toArray(new List[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override @SuppressWarnings("unchecked")
    public void readAsync(int offset, int pageSize, Consumer<List<String>[]> consumer) {
        try {
            readAsync(100, 0, offset, 0, pageSize, (rows) -> {
                if (consumer != null) consumer.accept(rows.values().toArray(new List[0]));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void readAsync(InputStream inputStream, Integer bufferSize, Integer sheetAt, Integer beginIndex, Integer endIndex, Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        try (Workbook workbook = StreamingReader.builder()
                .rowCacheSize(pageSize)
                .bufferSize(bufferSize)
                .open(inputStream)) {
            configureWorkbook(workbook);
            readBuffered(workbook, sheetAt, beginIndex, endIndex, pageSize, consumer);
        }
    }

    public void readAsync(Integer bufferSize, Integer sheetAt, Integer beginIndex, Integer endIndex, Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        try (Workbook workbook = StreamingReader.builder()
                .rowCacheSize(pageSize)
                .bufferSize(bufferSize)
                .open(file)) {
            configureWorkbook(workbook);
            readBuffered(workbook, sheetAt, beginIndex, endIndex, pageSize, consumer);
        }
    }

    /**
     *
     * @param workbook
     * @param sheetAt
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex the ending index, exclusive.
     * @param pageSize
     * @param consumer
     * @throws IOException
     */
    private static void readBuffered(Workbook workbook
            , Integer sheetAt
            , Integer beginIndex
            , Integer endIndex
            , Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        //
        Sheet sheet = workbook.getSheetAt(sheetAt);
        int maxCount = sheet.getLastRowNum() + 1;
        pageSize = (pageSize > maxCount) ? maxCount : pageSize;
        if (endIndex <= 0 || endIndex == Integer.MAX_VALUE) endIndex = maxCount;
        //
        int idx = -1;
        Map<Integer, List<String>> data = new HashMap<>();
        for (Row row : sheet){
            if (++idx < beginIndex) {continue;}
            if (idx >= endIndex) {break;}
            //
            data.put(idx, new ArrayList<>());
            for (Cell cell : row){
                addInto(data, idx, cell);
            }
            if (consumer != null && data.size() == pageSize ){
                Map xData = new HashMap(data);
                data.clear();
                consumer.accept(xData);
            }
        }
        //left-over
        if (consumer != null && data.size() > 0 ){
            Map xData = new HashMap(data);
            data.clear();
            consumer.accept(xData);
        }
    }

    public static void read(InputStream inputStream, Integer sheetAt, Integer startAt, Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            configureWorkbook(workbook);
            readAsync(workbook, sheetAt, startAt, pageSize, consumer);
        }
    }

    public void read(Integer sheetAt, Integer startAt, Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file)) {
            configureWorkbook(workbook);
            readAsync(workbook, sheetAt, startAt, pageSize, consumer);
        }
    }

    private static void readAsync(Workbook workbook
            , Integer sheetAt
            , Integer startAt
            , Integer pageSize
            , Consumer<Map<Integer, List<String>>> consumer) throws IOException {
        //
        Sheet sheet = workbook.getSheetAt(sheetAt);
        int maxCount = sheet.getLastRowNum() + 1;
        int loopCount = (pageSize == maxCount) ? 1 : (maxCount / pageSize) + 1;
        pageSize = (pageSize > maxCount) ? maxCount : pageSize;
        int index = 0;
        int start = (startAt < 0 || startAt >= maxCount) ? 0 : startAt;
        while (index < loopCount){
            int end = start + pageSize;
            if (end >= maxCount) end = maxCount;
            Map<Integer, List<String>> res = parseContent(workbook, sheetAt, start, end);
            if (consumer != null && res.size() > 0){
                consumer.accept(res);
            }
            //
            start += pageSize;
            index++;
        }
    }

    public static Map<Integer, List<String>> read(InputStream inputStream, Integer sheetAt, Integer start, Integer end) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            configureWorkbook(workbook);
            Map<Integer, List<String>> res = parseContent(workbook, sheetAt, start, end);
            return res;
        }
    }

    public static Map<Integer, List<String>> readXls(InputStream inputStream, Integer sheetAt, Integer start, Integer end) throws IOException {
        try (Workbook workbook = new HSSFWorkbook(inputStream)) {
            configureWorkbook(workbook);
            Map<Integer, List<String>> res = parseContent(workbook, sheetAt, start, end);
            return res;
        }
    }

    public Map<Integer, List<String>> read(Integer sheetAt, Integer start, Integer end) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(file)) {
            configureWorkbook(workbook);
            Map<Integer, List<String>> res = parseContent(workbook, sheetAt, start, end);
            return res;
        }
    }

    private static void configureWorkbook(Workbook workbook) {
        if (workbook != null){
            //Add All kind of setting for workbook:
            try {
                workbook.setMissingCellPolicy(Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            } catch (Exception e) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    private static Map<Integer, List<String>> parseContent(Workbook workbook, Integer sheetAt, Integer start, Integer end) throws IOException {
        //The math:
        Map<Integer, List<String>> data = new HashMap<>();
        Sheet sheet = workbook.getSheetAt(sheetAt);
        int maxCount = sheet.getLastRowNum() + 1;
        //
        if (end <= 0 || end > maxCount) end = maxCount;
        int idx = (start < 0) ? 0 : start;
        while (idx < end) {
            data.put(idx, new ArrayList<>());
            for (Cell cell : sheet.getRow(idx)) {
                addInto(data, idx, cell);
            }
            idx++;
        }
        return data;
    }

    private static void addInto(Map<Integer, List<String>> data, int idx, Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                data.get(idx).add(cell.getRichStringCellValue().getString());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    data.get(idx).add(cell.getDateCellValue() + "");
                } else {
                    data.get(idx).add(NumberToTextConverter.toText(cell.getNumericCellValue()));
                }
                break;
            case BOOLEAN:
                data.get(idx).add(cell.getBooleanCellValue() + "");
                break;
            case FORMULA:
                data.get(idx).add(cell.getStringCellValue() + "");
                break;
            default:
                data.get(idx).add(" ");
        }
    }

}
