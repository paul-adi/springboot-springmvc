package com.zxcv.springbootspringmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringbootSpringmvcApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringbootSpringmvcApplication.class, args);
    }

}
