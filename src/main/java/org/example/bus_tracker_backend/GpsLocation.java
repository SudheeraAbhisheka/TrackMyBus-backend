package org.example.bus_tracker_backend;

import org.example.bus_tracker_backend.repo.RootRepo;
import org.mvel2.MVEL;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;

@Service
public class GpsLocation {

    private RootEntity rootEntity;
    private final Object lock = new Object(); // Lock object for thread safety
    ScheduledFuture<?>[] futures;
    private int start;
    private int end;
    Random random = new Random();
    Map<Integer, Integer> xCoordinates = new HashMap<>();
    Map<String, LocationObject> Locations = new HashMap<>();


    public GpsLocation(RootRepo rootRepo) {
        Optional<RootEntity> rootEntityOptional = rootRepo.findById(1);
        rootEntityOptional.ifPresentOrElse(
                entity -> this.rootEntity = entity,
                () -> System.out.println("RootEntity with ID 1 not found")
        );

        start = rootEntity.getStarting_x();
        end = rootEntity.getEnding_x();

        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(3);
        taskScheduler.initialize();

        futures = new ScheduledFuture<?>[2];

        for (int i = 0; i < futures.length; i++) {
            int threadNumber = i;
            String busId = i+"";
//            taskScheduler.schedule(() -> startGpsUpdates(taskScheduler, threadNumber), new CronTrigger("0 * * * * *"));
            futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber, busId), Duration.ofSeconds(1));
            xCoordinates.put(threadNumber, start);
        }

        taskScheduler.scheduleAtFixedRate(()->System.out.println(Locations), Duration.ofSeconds(3));

    }

    public void startGpsUpdates(TaskScheduler taskScheduler, int threadNumber, String busId) {
        futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber, busId), Duration.ofSeconds(2));
    }

    public void updateGpsLocation(int threadNumber, String busId) {
        boolean reached = false;
        int x;
        LocationObject locationObject;

        synchronized (lock) {
            x = xCoordinates.get(threadNumber) + random.nextInt(20) + 10;

            xCoordinates.put(threadNumber, x);

            if (x >= end) {
                reached = true;
                x = end;
            }

            double y = getY(x);

            System.out.println(busId + " " + x + ", " + y);

            locationObject = new LocationObject(busId, x, y);

            Locations.put(busId, locationObject);


            if (reached) {
                locationObject.setReached(true);
                System.out.println(threadNumber + " Reached ending location");
                futures[threadNumber].cancel(true);

            }
        }
    }

    public double getY(double x) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", x);
        variables.put("Math", Math.class);

        return (Double) MVEL.eval(rootEntity.getRoot_function(), variables);
    }
}
