package com.summer.manage;

import com.summer.common.initializ.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.summer.manage"})
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class ManageApplication extends SpringBootApplication {
    public static void main(String[] args) {
        start(ManageApplication.class, args);
    }

    @Override
    protected int jobs(String... args) {
        return 0;
    }
}
