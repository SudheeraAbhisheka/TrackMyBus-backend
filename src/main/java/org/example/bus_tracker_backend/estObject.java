package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class estObject {
    private String sessionId;
    private double estimatedTime;

    public estObject(String sessionId, double estimatedTime) {
        this.sessionId = sessionId;
        this.estimatedTime = estimatedTime;
    }
}
