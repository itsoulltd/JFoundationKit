package com.infoworks.utils.exceltests;

import com.infoworks.PLogger;
import com.infoworks.orm.Row;
import com.infoworks.utils.excel.ExcelReadingService;
import com.infoworks.utils.excel.writer.AsyncWriter;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class ExcelReadWriteTest {

    private static Logger LOG = Logger.getLogger(ExcelReadWriteTest.class.getSimpleName());
    private PLogger pLogger;

    @Before
    public void before() {
        pLogger = new PLogger(LOG);
    }

    private List<Map> dummyTransactions() {
        List<Map> data = new ArrayList<>();
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "-230.0").add("balance", "1219.9").add("transaction_type", "withdrawal").add("transaction_date", "2026-01-14T19:38:20.318").add("transaction_ref", "cc25a914-4a84-4849").keyObjectMap());
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "1290.0").add("balance", "1449.9").add("transaction_type", "deposit").add("transaction_date", "2026-01-14T19:37:20.313").add("transaction_ref", "dd54cecd-80a5-4386").keyObjectMap());
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "-340.8").add("balance", "879.1").add("transaction_type", "transfer").add("transaction_date", "2026-01-14T19:36:20.312").add("transaction_ref", "ab4c7d73-dc84-433e").keyObjectMap());
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "-120.0").add("balance", "759.1").add("transaction_type", "transfer").add("transaction_date", "2026-01-14T19:35:20.317").add("transaction_ref", "daac741d-0ea9-49bc").keyObjectMap());
        data.add(new Row().add("account_ref", "CASH@admin").add("currency", "BDT").add("amount", "-30.1").add("balance", "159.9").add("transaction_type", "transfer").add("transaction_date", "2026-01-14T19:34:20.319").add("transaction_ref", "1248051c-5126-4f80").keyObjectMap());
        return data;
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
        try (InputStream inputStream = createInputStream(filename, iResources.create())) {
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
        iResources resources = iResources.create();
        try (InputStream ios = createInputStream("data/Balance_Sheet_1787924075343.xlsx", resources)) {
            ExcelReadingService.readAsync(ios, 50, 0, 1, 55, 12
                    , (rows) -> {
                        //Print rows:
                        rows.forEach((idx, row) -> {
                            LOG.info(String.join(" | ", row));
                        });
                        pLogger.printMillis("Page: " + pageCounter.incrementAndGet());
                    });
        }
        pLogger.printMillis("Async-Read-Complete");
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
    public void writeExcelFile() {
        //Prepare Data:
        String[] headers = {"AccountName","Currency","Amount","Balance","Type","Date","Ref"};
        String[] colKeys = {"account_ref","currency","amount","balance","transaction_type","transaction_date","transaction_ref"};
        Map<Integer, List<String>> data = new HashMap<>();
        data.put(0, Arrays.asList(headers));
        List<Map> transactions = dummyTransactions();
        Map<Integer, List<String>> converted = AsyncWriter.convert(transactions, 1, colKeys);
        data.putAll(converted);

        //AsyncWriter:
        try (AsyncWriter writer = new AsyncWriter(true, new ByteArrayOutputStream())) {
            writer.write("data", data, false);
            writer.flush();
            pLogger.printMillis("AsyncWriter-Complete");

            //Prepare for write to file:
            InputStream ios = new ByteArrayInputStream(((ByteArrayOutputStream) writer.getOutfile()).toByteArray());

            iFileStore<InputStream> uploadFile = new FileStore("target/");
            String reportName = String.format("Balance_Sheet_%s.xlsx", Instant.now().toEpochMilli());
            uploadFile.put(reportName, ios);
            pLogger.printMillis("iFileStore-Upload");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
