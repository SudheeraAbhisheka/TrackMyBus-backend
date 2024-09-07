package org.example.bus_tracker_backend.controller;

import org.example.bus_tracker_backend.GpsLocation;
import org.example.bus_tracker_backend.Bus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GpsController {

    @Autowired
    private Bus bus;

    @GetMapping("/gps-location")
    public GpsLocation getGpsLocation() {
        return bus.getCurrentLocation();
    }
}
