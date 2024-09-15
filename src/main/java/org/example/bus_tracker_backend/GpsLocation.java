package org.example.bus_tracker_backend;

import lombok.Getter;
import org.example.bus_tracker_backend.entities.BusEntity;
import org.example.bus_tracker_backend.entities.BusStopEntity;
import org.example.bus_tracker_backend.entities.RootEntity;
import org.example.bus_tracker_backend.repo.BusRepo;
import org.example.bus_tracker_backend.repo.BusStopRepo;
import org.example.bus_tracker_backend.repo.RootRepo;
import org.mvel2.MVEL;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
public class GpsLocation {
    private final Object lock = new Object();
    private RootEntity rootEntity;
    ScheduledFuture<?>[] futures;
    Random random = new Random();
    Map<Integer, Integer> xCoordinates = new HashMap<>();
    private final int THREAD_SCHEDULE_INTERVAL = 3;
    List<BusEntity> busEntities;
    ThreadPoolTaskScheduler taskScheduler;
    Map<String, Integer> speeds = new HashMap<>();
    @Getter Map<String, LocationObject> Locations = new HashMap<>();
    @Getter Map<String, Map<String, LocationObject>> LocationsWithRoot = new HashMap<>();
    @Getter List<RootEntity> rootEntities;
    @Getter Map<String, List<BusStopEntity>> busStops = new HashMap<>();
    @Getter Map<String, Double> estArrival = new HashMap<>();
    @Getter Map<String, Long> startedTimes = new HashMap<>();

    public GpsLocation(RootRepo rootRepo, BusRepo busRepo, BusStopRepo busStopRepo) {
        busEntities = busRepo.findAll();

        if (busEntities.isEmpty()) {
            System.out.println("No BusEntities found");
            return;
        }

        rootEntities = rootRepo.findAll();
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.initialize();

        futures = new ScheduledFuture<?>[2];

        for (int i = 0; i < busEntities.size(); i++) {
            BusEntity busEntity = busEntities.get(i);
            speeds.put(busEntity.getBus_id(), random.nextInt(3) + 1);

            Optional<RootEntity> rootEntityOptional = rootRepo.findById(busEntity.getRoot_id());
            rootEntityOptional.ifPresentOrElse(
                    entity -> this.rootEntity = entity,
                    ()-> System.out.println("RootEntity with ID _ not found")
            );

            busStops.put(rootEntity.getRoot_id(), busStopRepo.findByIdRootId(busEntity.getRoot_id()));
            startedTimes.put(rootEntity.getRoot_id(), System.currentTimeMillis());

            int threadNumber = i;

//            taskScheduler.schedule(() -> startGpsUpdates(taskScheduler, threadNumber), new CronTrigger("0 * * * * *"));
            futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber, busEntity.getBus_id(),
                    busEntity.getRoot_id(), speeds.get(busEntity.getBus_id()), rootEntity.getEnding_x(), rootEntity.getRoot_function()), Duration.ofSeconds(THREAD_SCHEDULE_INTERVAL));
            xCoordinates.put(threadNumber, rootEntity.getStarting_x());

        }

//        taskScheduler.scheduleAtFixedRate(()->System.out.println(Locations), Duration.ofSeconds(3));

    }

//    public void startGpsUpdates(TaskScheduler taskScheduler, int threadNumber, String busId) {
//        futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber, busId), Duration.ofSeconds(2));
//    }

    public void updateGpsLocation(int threadNumber, String busId, String rootId, int speed, int endX, String rootFunction) {
        boolean reached = false;
        int x;
        LocationObject locationObject;

        synchronized (lock) {
            x = xCoordinates.get(threadNumber) + random.nextInt(20*speed) + 10*speed;

            if(!busStops.get(rootId).isEmpty()){
                if(busStops.get(rootId).get(0).getId().getXCoordinate() > xCoordinates.get(threadNumber)){
                    double est = estimatedTimeToNextStop(busStops.get(rootId).get(0).getId().getXCoordinate(), xCoordinates.get(threadNumber), x, rootFunction);
                    estArrival.put(busId, est);
                }
                else{
                    estArrival.put(busId, 0.0);
                }
            }

            xCoordinates.put(threadNumber, x);

            if (x >= endX) {
                reached = true;
                x = endX;
            }

            double y = getY(x, rootFunction);

            locationObject = new LocationObject(busId, x, y);

            Locations.put(busId, locationObject);
            LocationsWithRoot.put(rootId, Locations);


            if (reached) {
                locationObject.setReached(true);
                System.out.println(threadNumber + " Reached ending location");
                futures[threadNumber].cancel(true);

            }
        }
    }

    private double getY(double x, String rootFunction) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("x", x);
        variables.put("Math", Math.class);

        return (Double) MVEL.eval(rootFunction, variables);
    }

    private double estimatedTimeToNextStop(int nextStopX, int currentX, int nextX, String rootFunction) {
        double currentSpeed;
        double unitDistance;
        double distanceToNextStop;

        unitDistance = Math.sqrt(
                Math.pow(nextX - currentX, 2) +
                Math.pow(getY(nextX, rootFunction) - getY(currentX, rootFunction), 2));

        currentSpeed = unitDistance/THREAD_SCHEDULE_INTERVAL;

        distanceToNextStop = Math.sqrt(
                Math.pow(nextStopX - currentX, 2) +
                Math.pow(getY(nextStopX, rootFunction) - getY(currentX, rootFunction), 2));

        return Math.round(distanceToNextStop/currentSpeed * 100.0) / 100.0;
    }

    public void stopGpsTracking() {
        synchronized (lock) {
            for (ScheduledFuture<?> future : futures) {
                if (future != null && !future.isCancelled()) {
                    future.cancel(true);
                }
            }
            System.out.println("GPS tracking stopped.");
        }
    }

    // Add a method to restart the GPS tracking
    public void restartGpsTracking() {
        stopGpsTracking();
        System.out.println("GPS tracking restarting...");
        // Reinitialize the tasks
        for (int i = 0; i < futures.length; i++) {
            BusEntity busEntity = busEntities.get(i);
            int threadNumber = i;
            int speed = speeds.get(busEntity.getBus_id());
            futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber, busEntity.getBus_id(),
                    busEntity.getRoot_id(), speed, rootEntity.getEnding_x(), rootEntity.getRoot_function()), Duration.ofSeconds(THREAD_SCHEDULE_INTERVAL));
            xCoordinates.put(threadNumber, rootEntity.getStarting_x());
        }
        System.out.println("GPS tracking restarted.");
    }

}

