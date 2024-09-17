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
    @Getter Map<String, Double> estArrival;
    @Getter Map<String, Map<String, Double>> estArrivalsMap = new HashMap<>();
    @Getter Map<String, Long> startedTimes = new HashMap<>();

    @Getter Map<String, Double> map_1;
    @Getter Map<String, Map<String, Double>> map_2 = new HashMap<>();
    @Getter Map<String, estObject> map_3 = new HashMap<>();
    @Getter Map<String, Double> map_4;
    @Getter Map<String, Map<String, Double>> map_5 = new HashMap<>();

    @Getter Map<keyObject, Double> map_6 = new HashMap<>();

    public GpsLocation(RootRepo rootRepo, BusRepo busRepo, BusStopRepo busStopRepo) {
        busEntities = busRepo.findAll();

        if (busEntities.isEmpty()) {
            System.out.println("No BusEntities found");
            return;
        }

        rootEntities = rootRepo.findAll();
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(busEntities.size());
        taskScheduler.initialize();

        futures = new ScheduledFuture<?>[busEntities.size()];

        for (int i = 0; i < busEntities.size(); i++) {
            BusEntity busEntity = busEntities.get(i);
            speeds.put(busEntity.getSession_id(), random.nextInt(3) + 1);

            Optional<RootEntity> rootEntityOptional = rootRepo.findById(busEntity.getRoot_id());
            rootEntityOptional.ifPresentOrElse(
                    entity -> this.rootEntity = entity,
                    ()-> System.out.println("RootEntity with ID _ not found")
            );

            busStops.put(rootEntity.getRoot_id(), busStopRepo.findByIdRootId(busEntity.getRoot_id()));
            startedTimes.put(rootEntity.getRoot_id(), System.currentTimeMillis());

            int threadNumber = i;

//            taskScheduler.schedule(() -> startGpsUpdates(taskScheduler, threadNumber), new CronTrigger("0 * * * * *"));
            futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber, busEntity.getSession_id(), busEntity.getBus_id(),
                    busEntity.getRoot_id(), speeds.get(busEntity.getSession_id()),
                    rootEntity.getEnding_x(), rootEntity.getRoot_function()), Duration.ofSeconds(THREAD_SCHEDULE_INTERVAL));
            xCoordinates.put(threadNumber, rootEntity.getStarting_x());

        }

//        taskScheduler.scheduleAtFixedRate(()->System.out.println(Locations), Duration.ofSeconds(3));

    }

//    public void startGpsUpdates(TaskScheduler taskScheduler, int threadNumber, String busId) {
//        futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber, busId), Duration.ofSeconds(2));
//    }

    public void updateGpsLocation(int threadNumber, String sessionId, String busId, String rootId, int speed, int endX, String rootFunction) {
        boolean reached = false;
        int x;
        LocationObject locationObject;

        synchronized (lock) {
            x = xCoordinates.get(threadNumber) + random.nextInt(20*speed) + 10*speed;

            map_1 = new HashMap<>();
            if(!busStops.get(rootId).isEmpty()){
                for(BusStopEntity busStopEntity : busStops.get(rootId)){
                    estArrival = new HashMap<>();

                    if(busStopEntity.getId().getXCoordinate() > xCoordinates.get(threadNumber)){
                        double est = estimatedTimeToNextStop(busStopEntity.getId().getXCoordinate(), xCoordinates.get(threadNumber), x, rootFunction);

//                        System.out.printf("xCoordinate: %s, sessionId: %s, est: %s \n",
//                                busStopEntity.getId().getXCoordinate(), sessionId, est);

//                        estArrival.put(sessionId, est);
//
//                        map_1.put(busStopEntity.getId().getXCoordinate()+"", est);
//
//                        map_3.put(busStopEntity.getId().getXCoordinate()+"", new estObject(sessionId, est));
                        map_6.put(new keyObject(sessionId, busStopEntity.getId().getXCoordinate()+""), est);
                    }
                    else{
//                        estArrival.put(sessionId, 0.0);
//                        map_1.put(busStopEntity.getId().getXCoordinate()+"", 0.0);
//
//                        map_3.put(busStopEntity.getId().getXCoordinate()+"", new estObject(sessionId, 0.0));
                        map_6.put(new keyObject(sessionId, busStopEntity.getId().getXCoordinate()+""), 0.0);
                    }


//                    estArrivalsMap.put(busStopEntity.getId().getXCoordinate()+"", estArrival);

                }

//                map_2.put(sessionId, map_1);
//
//                map_2.forEach(
//                        (session_id, map1)
//                                -> {
//                            map1.forEach(
//                                    (x_coordinate, estimate)
//                                            -> {
//                                        map_4 = new HashMap<>();
//                                        map_4.put(session_id, estimate);
//                                        map_5.put(x_coordinate, map_4);
//                                        map_6.put(new keyObject(session_id, x_coordinate), estimate);
//                                    }
//                            );
//                        }
//                );

                map_6.forEach(
                        (key_object, estimate)->{

                        }
//                                System.out.printf("session_id: %s, x_coordinate: %s, estimate: %s\n",
//                                        key.getSessionId(), key.getXCoordinate(), value)
                );
                System.out.println(getEstimatedTimes("188"));
                System.out.println(getEstimatedTimes("672"));
                System.out.println();
            }



            xCoordinates.put(threadNumber, x);

            if (x >= endX) {
                reached = true;
                x = endX;
            }

            double y = getY(x, rootFunction);

            locationObject = new LocationObject(sessionId, busId, x, y);

            Locations.put(sessionId, locationObject);
            LocationsWithRoot.put(rootId, Locations);


            if (reached) {
                locationObject.setReached(true);
                System.out.println(threadNumber + " Reached ending location");
                futures[threadNumber].cancel(true);

            }
        }
    }

    public Map<String, Double> getEstimatedTimes(String xCoordinates) {
//        map_6.put(new keyObject(sessionId, busStopEntity.getId().getXCoordinate()+""), est);
        Map<String, Double> map = new HashMap<>();

        map_6.forEach(
                (key_object, estimate)->{
                    if(key_object.getXCoordinate().equals(xCoordinates)){
                        map.put(key_object.getSessionId(), estimate);
                    }
//                    System.out.printf("session_id: %s, x_coordinate: %s, estimate: %s\n",
//                            key_object.getSessionId(), key_object.getXCoordinate(), estimate);

                }
        );

        return map;
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
            futures[threadNumber] = taskScheduler.scheduleAtFixedRate(() -> updateGpsLocation(threadNumber, busEntity.getSession_id(), busEntity.getBus_id(),
                    busEntity.getRoot_id(), speeds.get(busEntity.getSession_id()),
                    rootEntity.getEnding_x(), rootEntity.getRoot_function()), Duration.ofSeconds(THREAD_SCHEDULE_INTERVAL));
            xCoordinates.put(threadNumber, rootEntity.getStarting_x());
        }
        System.out.println("GPS tracking restarted.");
    }

}

