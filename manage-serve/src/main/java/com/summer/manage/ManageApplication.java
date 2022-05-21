package com.summer.manage;

import com.summer.manage.core.alipay.AliPayCore;
import com.summer.common.helper.SpringHelper;
import com.summer.common.initializ.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@EnableWebSocket
@ComponentScan({"com.thinker.education"})
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class ManageApplication extends SpringBootApplication {
    public static void main(String[] args) {
        start(ManageApplication.class, args);
    }
    @Override
    protected int jobs(String... args) {
        AliPayCore.initClient();
        SpringHelper.getBean(OrderSchedule.class).starting();
        return 1;
    }
}
