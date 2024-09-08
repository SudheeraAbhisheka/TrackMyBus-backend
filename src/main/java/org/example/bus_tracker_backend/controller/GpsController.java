package org.example.bus_tracker_backend.controller;

import org.example.bus_tracker_backend.LocationObject;
import org.example.bus_tracker_backend.GpsLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GpsController {

    @Autowired
    private GpsLocation gpsLocation;

    @GetMapping("/gps-location")
    public LocationObject getGpsLocation() {
        return gpsLocation.getCurrentLocation();
    }
}
