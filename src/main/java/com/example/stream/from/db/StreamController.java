package com.example.stream.from.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.sql.DataSource;

@RestController
@RequestMapping(value = "/stream-demo")
@Slf4j
public class StreamController {

    private Employee employee;
    private BasicDataSource dataSource;

    public StreamController(Employee employee, BasicDataSource dataSource) {
        this.employee = employee;
        this.dataSource = dataSource;
    }

    @GetMapping(value = "/employees/{employeeId}/cars")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<StreamingResponseBody> getEmployeeCars(@PathVariable  int employeeId) {
        log.info("Going to export cars for employee {}", employeeId);
        String zipFileName = "Cars Of Employee - " + employeeId;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFileName + ".zip")
                .body(
                        employee.getCars(dataSource, employeeId));
    }

}
