package org.example.bus_tracker_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusTrackerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTrackerBackendApplication.class, args);
    }

}
