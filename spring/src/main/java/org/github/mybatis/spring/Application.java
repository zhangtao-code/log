package org.github.mybatis.spring;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("org.github.mybatis.spring.mapper")
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
