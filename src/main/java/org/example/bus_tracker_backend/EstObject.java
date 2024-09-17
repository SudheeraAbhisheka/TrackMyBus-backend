package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class EstObject {
    private String sessionId;
    private double estimatedTime;

    public EstObject(String sessionId, double estimatedTime) {
        this.sessionId = sessionId;
        this.estimatedTime = estimatedTime;
    }
}
