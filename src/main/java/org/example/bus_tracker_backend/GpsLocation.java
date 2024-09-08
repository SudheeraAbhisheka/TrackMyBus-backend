package org.example.bus_tracker_backend;


import lombok.Getter;
import org.example.bus_tracker_backend.repo.RootRepo;
import org.mvel2.MVEL;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.Optional;

@Service
public class GpsLocation {
    @Getter
    private final LocationObject currentLocation = new LocationObject();
    private boolean reached = false;
    private ScheduledFuture<?> future;
    private RootEntity rootEntity;
    private int x;

    public GpsLocation(TaskScheduler taskScheduler, RootRepo rootRepo) {
        Optional<RootEntity> rootEntityOptional = rootRepo.findById(1);
        rootEntityOptional.ifPresentOrElse(
                entity -> this.rootEntity = entity,
                () -> System.out.println("RootEntity with ID 1 not found")
        );

        x = rootEntity.getStarting_x();

        //        taskScheduler.schedule(() -> startGpsUpdates(taskScheduler), new CronTrigger("0 * * * * *"));
        future = taskScheduler.scheduleAtFixedRate(this::updateGpsLocation, Duration.ofSeconds(2));

    }

    public void startGpsUpdates(TaskScheduler taskScheduler) {
//        future = taskScheduler.scheduleAtFixedRate(this::updateGpsLocation, Duration.ofSeconds(2));

    }


    public void updateGpsLocation() {
        Random random = new Random();
        int start = rootEntity.getStarting_x();
        int end = rootEntity.getEnding_x();

        x = x + random.nextInt(20) + 10;

        if (x >= end && future != null) {
            reached = true;
            x = end;
        }

        double y = getY(x);
        currentLocation.setX(x);
        currentLocation.setY(y);
        currentLocation.setTimestamp(System.currentTimeMillis());

        System.out.println("Updated Location: " + x + ", " + y);

        if(reached){
            reached = false;
            x = start;
            System.out.println("Reached ending location");
            future.cancel(true);
        }
    }

    public double getY(double x){
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", x);
        variables.put("Math", Math.class);

        return (Double) MVEL.eval(rootEntity.getRoot_function(), variables);
    }

}
