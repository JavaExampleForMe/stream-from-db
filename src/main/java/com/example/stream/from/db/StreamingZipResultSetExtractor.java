package com.example.stream.from.db;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.ResultSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class StreamingZipResultSetExtractor implements ResultSetExtractor<Integer> {
    private final static int CHUNK_SIZE = 100000;
    public final static int MAX_ROWS_IN_CSV = 10;
    private OutputStream outputStream;
    private int employeeId;
    private StreamingCsvResultSetExtractor streamingCsvResultSetExtractor;
    private boolean isInteractionCountExceedsLimit;
    private int fileCount = 0;

    public StreamingZipResultSetExtractor(OutputStream outputStream, int employeeId, boolean isInteractionCountExceedsLimit) {
        this.outputStream = outputStream;
        this.employeeId = employeeId;
        this.streamingCsvResultSetExtractor = new StreamingCsvResultSetExtractor(employeeId);
        this.isInteractionCountExceedsLimit = isInteractionCountExceedsLimit;
    }

    @Override
    @SneakyThrows
    public Integer extractData(ResultSet resultSet) throws DataAccessException {
        log.info("Creating thread to extract data as zip file for employeeId {}", employeeId);
        int lineCount = 1; //+1 for header row
        try (PipedOutputStream internalOutputStream = streamingCsvResultSetExtractor.extractData(resultSet);
             PipedInputStream InputStream = new PipedInputStream(internalOutputStream);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(InputStream))) {

            String currentLine;
            String header = bufferedReader.readLine() + "\n";
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                createFile(employeeId, zipOutputStream, header);
                while ((currentLine = bufferedReader.readLine()) != null) {
                    if (lineCount % MAX_ROWS_IN_CSV == 0) {
                        zipOutputStream.closeEntry();
                        createFile(employeeId, zipOutputStream, header);
                        lineCount++;
                    }
                    lineCount++;
                    currentLine += "\n";
                    zipOutputStream.write(currentLine.getBytes());
                    if (lineCount % CHUNK_SIZE == 0) {
                        zipOutputStream.flush();
                    }
                }
            }
        } catch (IOException e) {
            log.error("Task {} could not zip search results", employeeId, e);
        }

        log.info("Finished zipping all lines to {} file\\s - total of {} lines of data for task {}", fileCount, lineCount - fileCount, employeeId);
        return lineCount;
    }

    private void createFile(int employeeId, ZipOutputStream zipOutputStream, String header) {
        String fileName = "Cars for Employee - " + employeeId;
        if (isInteractionCountExceedsLimit) {
            fileCount++;
            fileName += " Part " + fileCount;
        }
        try {
            zipOutputStream.putNextEntry(new ZipEntry(fileName + ".csv"));
            zipOutputStream.write(header.getBytes());
        } catch (IOException e) {
            log.error("Could not create new zip entry for task {} ", employeeId, e);
        }
    }

}
