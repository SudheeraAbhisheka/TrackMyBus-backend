package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class LocationObject {
    private String sessionId;
    private int x;
    private double y;
    private String busId;
    private boolean reached = false;

    public LocationObject(String sessionId, String busId, int x, double y) {
        this.busId = busId;
        this.x = x;
        this.y = y;
    }
}

