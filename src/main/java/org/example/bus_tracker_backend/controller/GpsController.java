package org.example.bus_tracker_backend.controller;

import org.example.bus_tracker_backend.entities.BusStopEntity;
import org.example.bus_tracker_backend.LocationObject;
import org.example.bus_tracker_backend.GpsLocation;
import org.example.bus_tracker_backend.entities.RootEntity;
import org.example.bus_tracker_backend.repo.RootRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/gps-location")
    public Map<String, LocationObject> getGpsLocation() {
        return gpsLocation.getLocations();
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

    @GetMapping("/started-time/{root_id}")
    public long getStartedTime(@PathVariable String root_id) {
        return gpsLocation.getStartedTimes().get(root_id);
    }

    @GetMapping("/bus-stops/{root_id}")
    public BusStopEntity getListOfBusStops(@PathVariable String root_id) {
        return gpsLocation.getBusStops().get(root_id).get(0);
    }

    @GetMapping("/est-arrival/{root_id}")
    public double getEstArrival(@PathVariable String root_id) {
        return gpsLocation.getEstArrival().get(root_id);
    }

    @PostMapping("/restart")
    public ResponseEntity<String> restartGps() {
        gpsLocation.restartGpsTracking();
        return ResponseEntity.ok("GPS tracking restarted.");
    }
}
