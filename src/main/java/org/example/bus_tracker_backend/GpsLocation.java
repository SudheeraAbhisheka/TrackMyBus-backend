package org.example.bus_tracker_backend;


import lombok.Getter;
import org.example.bus_tracker_backend.repo.RootRepo;
import org.mvel2.MVEL;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.Optional;

@Service
public class GpsLocation {
    @Getter
    private final LocationObject currentLocation = new LocationObject();
//    private boolean reached = false;
    private RootEntity rootEntity;
    private int x;
    ScheduledFuture<?>[] futures;
    ScheduledFuture<?> future;


    public GpsLocation(RootRepo rootRepo) {
        Optional<RootEntity> rootEntityOptional = rootRepo.findById(1);
        rootEntityOptional.ifPresentOrElse(
                entity -> this.rootEntity = entity,
                () -> System.out.println("RootEntity with ID 1 not found")
        );

        x = rootEntity.getStarting_x();

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.initialize();

        futures = new ScheduledFuture<?>[2];

        for (int i = 0; i < futures.length; i++) {
            int threadNumber = i;
            taskScheduler.schedule(() -> startGpsUpdates(taskScheduler, threadNumber), new CronTrigger("0 * * * * *"));
        }

    }

    public void startGpsUpdates(TaskScheduler taskScheduler, int threadNumber) {
        futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber), Duration.ofSeconds(2));
    }

    public void updateGpsLocation(int threadNumber) {
        Random random = new Random();
        int start = rootEntity.getStarting_x();
        int end = rootEntity.getEnding_x();
        boolean reached = false;

        x = x + random.nextInt(20) + 10;

        if (x >= end) {
            reached = true;
            x = end;
        }

        double y = getY(x);
        currentLocation.setX(x);
        currentLocation.setY(y);
        currentLocation.setTimestamp(System.currentTimeMillis());

        System.out.println( threadNumber + " " + Thread.currentThread().getId() +" " + x + ", " + y );

        if(reached){
            x = start;
            System.out.println(threadNumber + "Reached ending location");
            futures[threadNumber].cancel(true);
        }
    }

    public double getY(double x){
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", x);
        variables.put("Math", Math.class);

        return (Double) MVEL.eval(rootEntity.getRoot_function(), variables);
    }

}
