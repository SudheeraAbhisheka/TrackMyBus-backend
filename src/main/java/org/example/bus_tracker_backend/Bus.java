package org.example.bus_tracker_backend;


import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

@Service
public class Bus {
    private final String BUS_ID = "bus_001";
    private final GpsLocation currentLocation = new GpsLocation();
    private final RootEntity rootEntity = new RootEntity();
    private int x = rootEntity.getSTARTING_X();
    private boolean reached = false;

    private ScheduledFuture<?> future;

    public Bus(TaskScheduler taskScheduler) {
        taskScheduler.schedule(() -> startGpsUpdates(taskScheduler), new CronTrigger("0 * * * * *"));

    }

    public void startGpsUpdates(TaskScheduler taskScheduler) {
        future = taskScheduler.scheduleAtFixedRate(this::updateGpsLocation, Duration.ofSeconds(2));

    }


    public void updateGpsLocation() {
        Random random = new Random();

        x = x + random.nextInt(20) + 10;

        if (x >= rootEntity.getENDING_X() && future != null) {
            reached = true;
            x = rootEntity.getENDING_X();
        }

        double y = rootEntity.getY(x);
        currentLocation.setX(x);
        currentLocation.setY(y);
        currentLocation.setTimestamp(System.currentTimeMillis());

        System.out.println("Updated Location: " + x + ", " + y);

        if(reached){
            reached = false;
            x = rootEntity.getSTARTING_X();
            System.out.println("Reached ending location");
            future.cancel(true);
        }
    }

    public GpsLocation getCurrentLocation() {
        return currentLocation;
    }
}
