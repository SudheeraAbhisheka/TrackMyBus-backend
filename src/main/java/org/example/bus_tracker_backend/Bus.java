package org.example.bus_tracker_backend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

@Service
public class Bus {
    private final String BUS_ID = "bus_001";
    private final GpsLocation currentLocation = new GpsLocation();
    private final Root root = new Root();
    private int x = root.getSTARTING_X();

    private ScheduledFuture<?> future;

    public Bus(TaskScheduler taskScheduler) {
        future = taskScheduler.scheduleAtFixedRate(this::updateGpsLocation, Duration.ofSeconds(2));
    }

    public void updateGpsLocation() {
        Random random = new Random();

        x = x + random.nextInt(20) + 10;

        if (x > root.getENDING_X() && future != null) {
            future.cancel(true);

            return;
        }

        double y = root.getY(x);
        currentLocation.setX(x);
        currentLocation.setY(y);
        currentLocation.setTimestamp(System.currentTimeMillis());

        System.out.println("Updated Location: " + x + ", " + y);
    }

    public GpsLocation getCurrentLocation() {
        return currentLocation;
    }
}
