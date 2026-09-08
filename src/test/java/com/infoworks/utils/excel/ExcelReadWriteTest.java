package com.infoworks.utils.excel;

import com.infoworks.PLogger;
import com.infoworks.data.impl.Person;
import com.infoworks.data.impl.SimpleDataSource;
import com.infoworks.orm.Row;
import com.infoworks.utils.excel.writer.AsyncWriter;
import com.infoworks.utils.excel.writer.StreamWriter;
import com.infoworks.utils.services.iFileStore;
import com.infoworks.utils.services.iResources;
import com.infoworks.utils.services.impl.FileStore;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExcelReadWriteTest {

    private static Logger LOG = Logger.getLogger(ExcelReadWriteTest.class.getSimpleName());
    private PLogger pLogger;

    @Before
    public void before() {
        pLogger = new PLogger(LOG);
    }

    private InputStream createInputStream(String fileName, iResources resources) throws FileNotFoundException {
        if (resources == null) {
            Path path = Paths.get("src","test", "resources", fileName);
            File imfFile = new File(path.toFile().getAbsolutePath());
            InputStream ios = new FileInputStream(imfFile);
            return ios;
        } else {
            File imfFile = new File(fileName);
            InputStream ios = resources.createStream(imfFile);
            return ios;
        }
    }

    private File createCopyFrom(String filename) throws IOException {
        Path tempDir = Files.createTempDirectory("temp-");
        Path target = tempDir.resolve(Path.of(filename).getFileName().toString());
        try (InputStream inputStream = iResources.create().createStream(new File(filename))) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target.toFile();
    }

    @Test
    public void rowCountExcelFile() throws IOException {
        File file = createCopyFrom("data/Balance_Sheet_1787924075343.xlsx");
        int count = new ExcelReadingService(file).size(0);
        pLogger.printMillis("Row count: " + count);
    }

    @Test
    public void readSyncExcelHeader() throws IOException {
        File file = createCopyFrom("data/Balance_Sheet_1787924075343.xlsx");
        List<String>[] items = new ExcelReadingService(file).readSync(0, 1); //this will give you the top-most-row.
        pLogger.printMillis("readSync row count: " + items.length);
        if (items.length == 1)
            LOG.info(String.join("|", items[0].toArray(new String[0])));
    }

    @Test
    public void readSyncExcelFile() throws IOException {
        File file = createCopyFrom("data/Balance_Sheet_1787924075343.xlsx");
        List<String>[] items = new ExcelReadingService(file).readSync(0, 10);
        pLogger.printMillis("readSync row count: " + items.length);
    }

    @Test
    public void readSyncExcelFile_02() throws IOException {
        File file = createCopyFrom("data/Balance_Sheet_1787924075343.xlsx");
        List<String>[] items = new ExcelReadingService(file).readSync(90, 120);
        pLogger.printMillis("readSync row count: " + items.length);
    }

    @Test
    public void readSyncExcelFile_03() throws IOException {
        File file = createCopyFrom("data/Balance_Sheet_1787924075343.xlsx");
        List<String>[] items = new ExcelReadingService(file).readSync(55, 5);
        pLogger.printMillis("readSync row count: " + items.length);
    }

    @Test
    public void readExcelFile() throws IOException {
        iResources resources = iResources.create();
        try (InputStream ios = createInputStream("data/Balance_Sheet_1787924075343.xlsx", resources)) {
            Map<Integer, List<String>> rows = ExcelReadingService.read(ios,0, 0, 10);
            rows.forEach((idx, row) -> {
                LOG.info(String.join(" | ", row));
            });
        }
        pLogger.printMillis("Simple-Read-Complete");
    }

    @Test
    public void readExcelInputStream_Async() throws IOException {
        AtomicLong pageCounter = new AtomicLong(0);
        //
        List<String> columnNames = new ArrayList<>();
        iResources resources = iResources.create();
        //
        try (InputStream ios = createInputStream("data/Balance_Sheet_1787924075343.xlsx", resources)) {
            ExcelReadingService.readAsync(ios, 50, 0, 0, 15, 5
                    , (rows) -> {
                        //Print rows:
                        rows.forEach((idx, row) -> {
                            //LOG.info(String.join(" | ", row));
                            //Since begin-index is 0, means top-row will return, which could be the column-names.
                            if (idx == 0) columnNames.addAll(row);
                            else LOG.info(convert(idx, row, columnNames.toArray(new String[0])).toString());
                        });
                        pLogger.printMillis("Page: " + pageCounter.incrementAndGet());
                    });
        }
        pLogger.printMillis("Async-Read-Complete");
    }

    private Row convert(int rIndex, List<String> rowData, String...keys) {
        Row row = new Row();
        int idx = 0;
        while (idx < keys.length) {
            try { row.add(keys[idx], rowData.get(idx)); }
            catch (Exception ignore) { /*LOG.warning("ERROR: row-convertion at rIndex: " + rIndex);*/ }
            idx++;
        }
        return row;
    }

    @Test
    public void readExcelFile_Async() throws IOException {
        AtomicLong pageCounter = new AtomicLong(0);
        //
        File file = createCopyFrom("data/Balance_Sheet_1787924075343.xlsx");
        new ExcelReadingService(file).readAsync(100, 0, 1, 30, 10
                , (rows) -> {
                    //Print rows:
                    rows.forEach((idx, row) -> {
                        LOG.info(String.join(" | ", row));
                    });
                    pLogger.printMillis("Page: " + pageCounter.incrementAndGet());
                });
        pLogger.printMillis("Async-Read-Complete");
    }

    //@Test
    public void writeExcelFile_Async() {
        //Prepare Data:
        String[] headers = {"AccountName","Currency","Amount","Balance","Type","Date","Ref"};
        String[] colKeys = {"account_ref","currency","amount","balance","transaction_type","transaction_date","transaction_ref"};
        Map<Integer, List<String>> data = new HashMap<>();
        data.put(0, Arrays.asList(headers));
        List<Map<String, Object>> transactions = dummyTransactions();
        Map<Integer, List<String>> converted = AsyncWriter.convert(transactions, 1, colKeys);
        data.putAll(converted);

        //AsyncWriter:
        try (AsyncWriter writer = new AsyncWriter(true, new ByteArrayOutputStream())) {
            writer.write("data", data, false);
            writer.flush();
            pLogger.printMillis("AsyncWriter-Complete");

            //Prepare for write to file:
            InputStream ios = new ByteArrayInputStream(((ByteArrayOutputStream) writer.getOutputStream()).toByteArray());

            iFileStore<InputStream> uploadFile = new FileStore("target/");
            String reportName = String.format("Balance_Sheet_Async_%s.xlsx", Instant.now().toEpochMilli());
            uploadFile.put(reportName, ios);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pLogger.printMillis("iFileStore-Upload");
    }

    //@Test
    public void writeExcelFile_Stream() {
        //Prepare Data:
        String[] headers = {"AccountName","Currency","Amount","Balance","Type","Date","Ref"};
        String[] colKeys = {"account_ref","currency","amount","balance","transaction_type","transaction_date","transaction_ref"};
        Map<Integer, List<String>> data = new HashMap<>();
        data.put(0, Arrays.asList(headers));
        List<Map<String, Object>> transactions = dummyTransactions();
        Map<Integer, List<String>> converted = AsyncWriter.convert(transactions, 1, colKeys);
        data.putAll(converted);

        //StreamWriter:
        try (AsyncWriter writer = new StreamWriter(50, new ByteArrayOutputStream())) {
            writer.write("data", data, false);
            writer.flush();
            pLogger.printMillis("StreamWriter-Complete");

            //Prepare for write to file:
            InputStream ios = new ByteArrayInputStream(((ByteArrayOutputStream) writer.getOutputStream()).toByteArray());

            iFileStore<InputStream> uploadFile = new FileStore("target/");
            String reportName = String.format("Balance_Sheet_Stream_%s.xlsx", Instant.now().toEpochMilli());
            uploadFile.put(reportName, ios);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pLogger.printMillis("iFileStore-Upload");
    }

    @Test
    public void writeExcelFile_Stream_v2() {
        String filename = String.format("Balance_Sheet_Stream_%s.xlsx", Instant.now().toEpochMilli());
        String fileSavePath = Path.of("target", filename).toString();

        try (AsyncWriter writer = new StreamWriter(100, fileSavePath)) {
            //Prepare Data:
            String[] headers = {"AccountName","Currency","Amount","Balance","Type","Date","Ref"};
            Map<Integer, List<String>> headerRow = new HashMap<>();
            headerRow.put(0, Arrays.asList(headers));
            writer.write("data", headerRow);

            String[] colKeys = {"account_ref","currency","amount","balance","transaction_type","transaction_date","transaction_ref"};
            List<Map<String, Object>> transactions = dummyTransactions();
            Map<Integer, List<String>> reportData = AsyncWriter.convert(transactions, 1, colKeys);

            //Write to xlsx file:
            writer.write("data", reportData);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pLogger.printMillis("iFileStore-Upload");
    }

    private List<Map<String, Object>> dummyTransactions() {
        List<Map<String, Object>> data = new ArrayList<>();
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "-230.0").add("balance", "1219.9").add("transaction_type", "withdrawal").add("transaction_date", "2026-01-14T19:38:20.318").add("transaction_ref", "cc25a914-4a84-4849").keyObjectMap());
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "1290.0").add("balance", "1449.9").add("transaction_type", "deposit").add("transaction_date", "2026-01-14T19:37:20.313").add("transaction_ref", "dd54cecd-80a5-4386").keyObjectMap());
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "-340.8").add("balance", "879.1").add("transaction_type", "transfer").add("transaction_date", "2026-01-14T19:36:20.312").add("transaction_ref", "ab4c7d73-dc84-433e").keyObjectMap());
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "-120.0").add("balance", "759.1").add("transaction_type", "transfer").add("transaction_date", "2026-01-14T19:35:20.317").add("transaction_ref", "daac741d-0ea9-49bc").keyObjectMap());
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "-30.1").add("balance", "159.9").add("transaction_type", "transfer").add("transaction_date", "2026-01-14T19:34:20.319").add("transaction_ref", "1248051c-5126-4f80").keyObjectMap());
        return data;
    }

    @Test
    public void writeExcelFile_Stream_v3() {
        String filename = String.format("Balance_Sheet_Stream_%s.xlsx", Instant.now().toEpochMilli());
        String fileSavePath = Path.of("target", filename).toString();

        try (AsyncWriter writer = new StreamWriter(100, fileSavePath)) {
            //Prepare Data: (Sheet-01)
            String[] headers = {"AccountName","Currency","Amount","Balance","Type","Date","Ref"};
            Map<Integer, List<String>> headerRow = new HashMap<>();
            headerRow.put(0, Arrays.asList(headers));
            writer.write("data", headerRow);

            String[] colKeys = {"account_ref","currency","amount","balance","transaction_type","transaction_date","transaction_ref"};
            SimpleDataSource<Integer, Map<String, Object>> dataSource = dataSource();

            //Write to xlsx file:
            AtomicInteger indexCounter = new AtomicInteger(1);
            pagination(dataSource, 5, -1, (result) -> {
                int startIndex = indexCounter.get();
                Map<Integer, List<String>> reportData = AsyncWriter.convert(result, startIndex, colKeys); // startIndex need move page by page.
                writer.write("data", reportData);
                indexCounter.addAndGet(reportData.size());
            });
            //END:: Sheet-01

            //Prepare Data: (Sheet-02)
            headerRow = new HashMap<>();
            headerRow.put(0, Arrays.asList("Metric", "Sum"));
            writer.write("summary", headerRow);
            //Write summary:
            List<Map<String, Object>> summary = getDummySummary("metric", "sum");
            writer.write("summary", AsyncWriter.convert(summary, 1, "metric", "sum"));
            //END:: Sheet-02

            //Flush any leftover:
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pLogger.printMillis("iFileStore-Upload");
    }

    private List<Map<String, Object>> getDummySummary(String...headers) {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> st = new HashMap<>();
        st.put(headers[0], "SALARY");
        st.put(headers[1], "$123k");
        data.add(st);

        st = new HashMap<>();
        st.put(headers[0], "PURCHASE");
        st.put(headers[1], "$313k");
        data.add(st);

        return data;
    }

    private void pagination(SimpleDataSource<Integer, Map<String, Object>> dataSource
            , int pageSize
            , int pageCount
            , Consumer<List<Map<String, Object>>> consumer) {
        //Null Check:
        if (consumer == null) {
            consumer.accept(new ArrayList<>());
            return;
        }
        //Validation:
        pageSize = (pageSize <= 0) ? 5 : pageSize;
        int maxCount = (pageSize == dataSource.size()) ? 1 : (dataSource.size() / pageSize) + 1;
        pageCount = (pageCount <= 0) ? maxCount : pageCount;
        //Works:
        int offset = 0; //iDataSource::readAsync is 0-based;
        while (offset <= pageCount) {
            Object[] objs  = dataSource.readSync(offset, pageSize);
            List<Map<String, Object>> items = Stream.of(objs)
                    .map(ob -> (Map<String, Object>) ob)
                    .collect(Collectors.toList());
            consumer.accept(items);
            //Next page:
            offset++;
        }
    }

    private SimpleDataSource<Integer, Map<String, Object>> dataSource() {
        SimpleDataSource<Integer, Map<String, Object>> data = new SimpleDataSource<>();
        AtomicInteger indexCounter = new AtomicInteger(0);
        dummyTransactions().forEach(row -> {
            data.put(indexCounter.getAndIncrement(), row);
        });
        dummyTransactions().forEach(row -> {
            data.put(indexCounter.getAndIncrement(), row);
        });
        dummyTransactions().forEach(row -> {
            data.put(indexCounter.getAndIncrement(), row);
        });
        dummyTransactions().forEach(row -> {
            data.put(indexCounter.getAndIncrement(), row);
        });
        return data;
    }

}
