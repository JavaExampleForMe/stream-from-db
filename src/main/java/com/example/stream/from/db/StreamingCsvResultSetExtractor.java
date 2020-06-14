package com.example.stream.from.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.PipedOutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class StreamingCsvResultSetExtractor implements ResultSetExtractor<PipedOutputStream> {
    private final static int CHUNK_SIZE = 100000;
    private PipedOutputStream pipedOutputStream;
    private final int employeeId;
    public StreamingCsvResultSetExtractor(int employeeId) {
        this.employeeId = employeeId;
    }

    @SneakyThrows
    @Override
    public PipedOutputStream extractData(ResultSet resultSet) throws DataAccessException {
        log.info("Creating thread to extract data as csv and save to file for task {}", employeeId);
        this.pipedOutputStream = new PipedOutputStream();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            prepareCsv(resultSet);
        });

        return pipedOutputStream;
    }

    @SneakyThrows
    private Integer prepareCsv(ResultSet resultSet) {
        int interactionsSent = 1;
        log.info("starting to extract data to csv lines");
        streamHeaders(resultSet.getMetaData());
        StringBuilder csvRowBuilder = new StringBuilder();
        try {
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i < columnCount + 1; i++) {
                    if(resultSet.getString(i) != null && resultSet.getString(i).contains(",")){
                            String strToAppend = "\"" + resultSet.getString(i) + "\"";
                            csvRowBuilder.append(strToAppend);
                        } else {
                            csvRowBuilder.append(resultSet.getString(i));
                    }
                    csvRowBuilder.append(",");
                }
                int rowLength = csvRowBuilder.length();
                csvRowBuilder.replace(rowLength - 1, rowLength, "\n");

                pipedOutputStream.write(csvRowBuilder.toString().getBytes());
                interactionsSent++;
                csvRowBuilder.setLength(0);
                if (interactionsSent % CHUNK_SIZE == 0) {
                    pipedOutputStream.flush();
                }
            }
        } finally {
            pipedOutputStream.flush();
            pipedOutputStream.close();
        }

        log.debug("Created all csv lines for Task {} - total of {} rows", employeeId, interactionsSent);
        return interactionsSent;
    }

    @SneakyThrows
    private void streamHeaders(ResultSetMetaData resultSetMetaData) {
        StringBuilder headersCsvBuilder = new StringBuilder();

        for (int i = 1; i < resultSetMetaData.getColumnCount() + 1; i++) {
            headersCsvBuilder.append(resultSetMetaData.getColumnLabel(i)).append(",");
        }
        int rowLength = headersCsvBuilder.length();
        headersCsvBuilder.replace(rowLength - 1, rowLength, "\n");

        pipedOutputStream.write(headersCsvBuilder.toString().getBytes());
    }

}
