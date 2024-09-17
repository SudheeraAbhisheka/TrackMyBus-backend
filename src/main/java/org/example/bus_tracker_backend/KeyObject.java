package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class KeyObject {
    private String sessionId;
    private String xCoordinate;

    public KeyObject(String sessionId, String xCoordinate) {
        this.sessionId = sessionId;
        this.xCoordinate = xCoordinate;
    }
}
