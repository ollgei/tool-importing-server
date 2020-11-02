package com.github.ollgei.tool.importing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Bootstrap.
 * @author ollgei
 * @since 1.0
 */
@SpringBootApplication
@MapperScan(basePackages = "com.github.ollgei.tool.importing.dao.mapper")
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
    }

}
