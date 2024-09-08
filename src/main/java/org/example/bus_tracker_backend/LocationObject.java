package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class LocationObject {
    private int x;
    private double y;
    private String busId;
    private long timestamp;
}

