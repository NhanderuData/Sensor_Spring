package com.pucgoias.sensorsse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SensorSseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SensorSseApplication.class, args);
    }
}
