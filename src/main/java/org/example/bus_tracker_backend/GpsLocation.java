package org.example.bus_tracker_backend;

import lombok.Getter;
import org.example.bus_tracker_backend.controller.GpsController;
import org.example.bus_tracker_backend.entities.BusEntity;
import org.example.bus_tracker_backend.entities.BusStopEntity;
import org.example.bus_tracker_backend.entities.RootEntity;
import org.example.bus_tracker_backend.repo.BusRepo;
import org.example.bus_tracker_backend.repo.BusStopRepo;
import org.example.bus_tracker_backend.repo.RootRepo;
import org.mvel2.MVEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GpsLocation {
    private final RootRepo rootRepo;
    private final BusRepo busRepo;
    private final BusStopRepo busStopRepo;
    private final Object lock = new Object();
    private final GpsController gpsController;
    private RootEntity rootEntity;
    ScheduledFuture<?>[] futures;
    Random random = new Random();
    Map<Integer, Integer> xCoordinates = new HashMap<>();
    private final int THREAD_SCHEDULE_INTERVAL = 3;
    List<BusEntity> busEntities;
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    Map<String, Double> speeds = new HashMap<>();
    Map<String, LocationObject> Locations = new HashMap<>();
    List<KeyObject> markedDelayed = new ArrayList<>();
    @Getter Map<String, Map<String, LocationObject>> LocationsWithRoot = new HashMap<>();
    @Getter List<RootEntity> rootEntities;
    @Getter Map<String, List<BusStopEntity>> busStops = new HashMap<>();
    @Getter Map<String, Long> startedTimes = new HashMap<>();
    @Getter Map<KeyObject, Double> estimatedTimes = new HashMap<>();

    int delay;

    @Autowired
    public GpsLocation(RootRepo rootRepo, BusRepo busRepo, BusStopRepo busStopRepo, GpsController gpsController) {
        this.rootRepo = rootRepo;
        this.busRepo = busRepo;
        this.busStopRepo = busStopRepo;
        this.gpsController = gpsController;

        busEntities = busRepo.findAll();
        if (busEntities.isEmpty()) {
            System.out.println("No BusEntities found");
            return;
        }

        rootEntities = rootRepo.findAll();
        if (rootEntities.isEmpty()) {
            System.out.println("No RootEntities found");
            return;
        }

        taskScheduler.setPoolSize(busEntities.size());
        taskScheduler.initialize();

        futures = new ScheduledFuture<?>[busEntities.size()];

        for (int i = 0; i < busEntities.size(); i++) {
            BusEntity busEntity = busEntities.get(i);
            speeds.put(busEntity.getSession_id(), random.nextDouble(2) + 1);

            Optional<RootEntity> rootEntityOptional = rootRepo.findById(busEntity.getRoot_id());
            rootEntityOptional.ifPresentOrElse(
                    entity -> this.rootEntity = entity,
                    ()-> System.out.print("RootEntity with ID _ not found\n")
            );

            busStops.put(rootEntity.getRoot_id(), busStopRepo.findByIdRootId(busEntity.getRoot_id()));
            startedTimes.put(busEntity.getSession_id(), System.currentTimeMillis());

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

    public void updateGpsLocation(int threadNumber, String sessionId, String busId, String rootId, double speed, int endX, String rootFunction) {
        boolean reached = false;
        int x;
        LocationObject locationObject;

        synchronized (lock) {
            x = (int)Math.round(
                    xCoordinates.get(threadNumber) + random.nextDouble(20*speed) + 10*speed
            );

            if(!busStops.get(rootId).isEmpty()){
                for(BusStopEntity busStopEntity : busStops.get(rootId)){
                    String stopCoordinate = busStopEntity.getId().getXCoordinate()+"";

                    if(busStopEntity.getId().getXCoordinate() > xCoordinates.get(threadNumber)){
                        double est = estimatedTimeToNextStop(busStopEntity.getId().getXCoordinate(), xCoordinates.get(threadNumber), x, rootFunction);
                        estimatedTimes.put(new KeyObject(sessionId, stopCoordinate), est);

                        int delay = busStopEntity.getExpectedTime() - (int)TimeUnit.MILLISECONDS.toSeconds(
                                System.currentTimeMillis() - startedTimes.get(sessionId)
                        );


                        this.delay = delay;

                        if(delay <= 0 && !markedDelayed.contains(new KeyObject(sessionId, stopCoordinate))){
                            gpsController.notifyDelayedSession(
                                    new DelayedObject(sessionId, stopCoordinate, est)
                            );

                            markedDelayed.add(new KeyObject(sessionId, stopCoordinate));

                            System.out.println(new DelayedObject(sessionId, stopCoordinate, est));
                        }

                    }
                    else{
                        estimatedTimes.put(new KeyObject(sessionId, stopCoordinate), 0.0);
                    }
                }
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
                System.out.printf("Session %s reached ending location", sessionId);
                futures[threadNumber].cancel(true);

            }
        }
    }

    public Map<String, Double> getEstimatedTimes(String xCoordinates) {
        Map<String, Double> map = new HashMap<>();

        estimatedTimes.forEach(
                (key_object, estimate)->{
                    if(key_object.getXCoordinate().equals(xCoordinates)){
                        map.put(key_object.getSessionId(), estimate);
                    }
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
            if(futures!=null){
                for (ScheduledFuture<?> future : futures) {
                    if (future != null && !future.isCancelled()) {
                        future.cancel(true);
                    }
                }
            }

            System.out.println("GPS tracking stopped.");
        }
    }

    public RootEntity addRootEntity(RootEntity rootEntity) {
        RootEntity entity = rootRepo.save(rootEntity);
        rootEntities = rootRepo.findAll();

        return entity;
    }

    public boolean deleteRootEntityById(String id) {
        if (rootRepo.existsById(id)) {
            rootRepo.deleteById(id);
            rootEntities = rootRepo.findAll();
            return true;
        } else {
            return false;
        }
    }

    public void restartGpsTracking() {
        synchronized (lock) {
            stopGpsTracking();
            System.out.println("GPS tracking restarting...");

            // Re-fetch busEntities and rootEntities from the database
            busEntities = busRepo.findAll();
            rootEntities = rootRepo.findAll();

            if (busEntities.isEmpty()) {
                System.out.println("No BusEntities found");
                return;
            }

            if (rootEntities.isEmpty()) {
                System.out.println("No RootEntities found");
                return;
            }

            System.out.printf("Delay %s\n", delay);

            // Re-initialize data structures
            markedDelayed.clear();
            speeds.clear();
            xCoordinates.clear();
            busStops.clear();
            startedTimes.clear();
            estimatedTimes.clear();
            Locations.clear();
            LocationsWithRoot.clear();

            // Shut down and recreate taskScheduler
            taskScheduler.shutdown();
            taskScheduler = new ThreadPoolTaskScheduler();
            taskScheduler.setPoolSize(busEntities.size());
            taskScheduler.initialize();

            futures = new ScheduledFuture<?>[busEntities.size()];

            for (int i = 0; i < busEntities.size(); i++) {
                BusEntity busEntity = busEntities.get(i);

                speeds.put(busEntity.getSession_id(), random.nextDouble(2) + 1);

                Optional<RootEntity> rootEntityOptional = rootRepo.findById(busEntity.getRoot_id());
                RootEntity rootEntityForBus;
                if (rootEntityOptional.isPresent()) {
                    rootEntityForBus = rootEntityOptional.get();
                } else {
                    System.out.printf("RootEntity with ID %s not found\n", busEntity.getRoot_id());
                    continue; // Skip this busEntity
                }

                busStops.put(rootEntityForBus.getRoot_id(), busStopRepo.findByIdRootId(busEntity.getRoot_id()));
                startedTimes.put(busEntity.getSession_id(), System.currentTimeMillis());

                int threadNumber = i;
                RootEntity finalRootEntityForBus = rootEntityForBus; // Make it effectively final for lambda

                futures[threadNumber] = taskScheduler.scheduleAtFixedRate(
                        () -> updateGpsLocation(
                                threadNumber,
                                busEntity.getSession_id(),
                                busEntity.getBus_id(),
                                busEntity.getRoot_id(),
                                speeds.get(busEntity.getSession_id()),
                                finalRootEntityForBus.getEnding_x(),
                                finalRootEntityForBus.getRoot_function()
                        ),
                        Duration.ofSeconds(THREAD_SCHEDULE_INTERVAL)
                );
                xCoordinates.put(threadNumber, finalRootEntityForBus.getStarting_x());
            }
            System.out.println("GPS tracking restarted.");
        }
    }


}

