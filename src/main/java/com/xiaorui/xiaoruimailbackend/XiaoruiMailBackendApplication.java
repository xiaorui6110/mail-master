package com.xiaorui.xiaoruimailbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author lenovo
 */
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.xiaorui.xiaoruimailbackend.mapper")
@SpringBootApplication
public class XiaoruiMailBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaoruiMailBackendApplication.class, args);
    }

}
