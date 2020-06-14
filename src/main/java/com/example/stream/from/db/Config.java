package com.example.stream.from.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Config {

    @Bean
    public Employee getEmployee() {
        return new Employee();
    }
    @Bean
    public BasicDataSource searchBasicDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName());
        String url = getSqlServerUrl();
        dataSource.setUrl(url);
        return dataSource;
    }

    private String getSqlServerUrl() {
        return String.format("jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;integratedSecurity=false",
                "localhost",
                "1433",
                "demo",
                "sa",
                "sa");
    }
}
