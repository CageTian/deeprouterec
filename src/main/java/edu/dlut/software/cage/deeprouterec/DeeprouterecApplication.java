package edu.dlut.software.cage.deeprouterec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class DeeprouterecApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeeprouterecApplication.class, args);
    }

}

