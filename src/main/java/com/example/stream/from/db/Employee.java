package com.example.stream.from.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Employee {

    public StreamingResponseBody getCars(BasicDataSource dataSource, int employeeId) {
        StreamingResponseBody streamingResponseBody = new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                String sqlQuery = "SELECT [Id], [employeeId],  [type], [text1] " +
                        "FROM Cars " +
                        "WHERE EmployeeID=? ";
                PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter() {
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setInt(1, employeeId);
                    }
                };
               StreamingZipResultSetExtractor zipExtractor = new StreamingZipResultSetExtractor(outputStream, employeeId, false);
                Integer numberOfInteractionsSent = jdbcTemplate.query(sqlQuery, preparedStatementSetter, zipExtractor);

            }
        };
        return streamingResponseBody;
    }
}
