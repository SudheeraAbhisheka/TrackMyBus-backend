package org.example.bus_tracker_backend;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class Bus {
    private final String BUS_ID = "bus_001";
    private final GpsLocation currentLocation;
    private final Root root = new Root();

    public Bus() {
//        this.currentLocation = new GpsLocation(root.getSTARTING_X(), root.getY(root.getSTARTING_X()), BUS_ID, System.currentTimeMillis());
        this.currentLocation = new GpsLocation();
    }

    @Scheduled(fixedRate = 2000)
    public void updateGpsLocation() {
        Random random = new Random();

        int x = root.getSTARTING_X() + random.nextInt(20) + 10;
        double y = root.getY(x);

        currentLocation.setX(x);
        currentLocation.setY(y);
        currentLocation.setTimestamp(System.currentTimeMillis());

        System.out.println("Updated Location: " + x + ", " + y);
    }

    public GpsLocation getCurrentLocation() {
        return currentLocation;
    }
}
