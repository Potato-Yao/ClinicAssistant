package com.potato.desktop.Utils;

import com.potato.kernel.Config;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CSVUtil {
    private Path filePath;
    private boolean flushEachTime;
    private BufferedWriter writer;
    private CSVPrinter printer;

    public CSVUtil(String fileName, boolean overview, boolean flushEachTime, String... headers) throws IOException {
        Path projectRoot = Path.of(System.getProperty("user.dir")).toAbsolutePath().resolve("..");
        Path outputPath = projectRoot.resolve(Config.CAFILE);

        this.filePath = outputPath.resolve(fileName);
        this.flushEachTime = flushEachTime;

        OpenOption[] option = overview
                ? new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING}
                : new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};
        this.writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, option);
        this.printer = new CSVPrinter(writer, CSVFormat.DEFAULT);

        if (overview) {
            printer.printRecord((Object[]) headers);;
        }
    }

    public void write(String... values) throws IOException {
        printer.printRecord((Object[]) values);
        if (flushEachTime) {
            flush();
        }
    }

    public void flush() throws IOException {
        printer.flush();
    }

    public void close() throws IOException {
        printer.close();
        writer.close();
    }
}
