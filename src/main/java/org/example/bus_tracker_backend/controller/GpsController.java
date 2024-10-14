package org.example.bus_tracker_backend.controller;

import org.example.bus_tracker_backend.DelayedObject;
import org.example.bus_tracker_backend.service.RootEntityService;
import org.example.bus_tracker_backend.entities.BusStopEntity;
import org.example.bus_tracker_backend.LocationObject;
import org.example.bus_tracker_backend.GpsLocation;
import org.example.bus_tracker_backend.entities.RootEntity;
import org.example.bus_tracker_backend.repo.RootRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class GpsController {

    @Autowired
    private GpsLocation gpsLocation;
    @Autowired
    private RootRepo rootRepo;
    @Autowired
    private RootEntityService rootEntityService;

    private SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

    @GetMapping("/notify-delay")
    public SseEmitter streamGpsLocation() {
        return emitter;
    }


    public void notifyDelayedSession(DelayedObject delayedObject) {
        try {
            emitter.send(delayedObject);
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    @GetMapping("/gps-location/{root_id}")
    public Map<String, LocationObject> getGpsLocationById(@PathVariable String root_id) {
        return gpsLocation.getLocationsWithRoot().get(root_id);
    }

    @GetMapping("/select-map")
    public List<RootEntity> getListOfMaps() {
        return gpsLocation.getRootEntities();
    }

    @GetMapping("/map/{root_id}")
    public ResponseEntity<RootEntity> getMapDetails(@PathVariable String root_id) {
        Optional<RootEntity> map = rootRepo.findById(root_id);
        if (map.isPresent()) {
            return ResponseEntity.ok(map.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/bus-stops/{root_id}")
    public List<BusStopEntity> getListOfBusStops(@PathVariable String root_id) {
        return gpsLocation.getBusStops().get(root_id);
    }

    @GetMapping("/est-arrival/{xcoordinate}")
    public Map<String, Double> getEstArrival(@PathVariable String xcoordinate) {
        return gpsLocation.getEstimatedTimes(xcoordinate);
    }

    @GetMapping("/add-route/{root_id}")
    public RootEntity getRootEntity(@PathVariable String root_id) {
        List<RootEntity> rootEntities = gpsLocation.getRootEntities();
        RootEntity rootEntity = null;

        for (RootEntity entity : rootEntities) {
            if(entity.getRoot_id().equals(root_id)){
                rootEntity = entity;
            }
        }

        return rootEntity;
    }

    @PostMapping("/add-route")
    public RootEntity addRootEntity(@RequestBody RootEntity rootEntity) {
        return gpsLocation.addRootEntity(rootEntity);
    }

    @PostMapping("/restart")
    public ResponseEntity<String> restartGps() {
        gpsLocation.restartGpsTracking();
        return ResponseEntity.ok("GPS tracking restarted.");
    }

    @PostMapping("/update-default-test-data")
    public ResponseEntity<String> updateDefaultTestData() {
        rootEntityService.executeRawInsert();
        return ResponseEntity.ok("Default test data updated.");
    }

    @DeleteMapping("add-route/{id}")
    public ResponseEntity<Void> deleteRootEntity(@PathVariable String id) {
        boolean isDeleted = gpsLocation.deleteRootEntityById(id);
        if (isDeleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
