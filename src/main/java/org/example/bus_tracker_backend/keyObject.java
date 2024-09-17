package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class keyObject {
    private String sessionId;
    private String xCoordinate;

    public keyObject(String sessionId, String xCoordinate) {
        this.sessionId = sessionId;
        this.xCoordinate = xCoordinate;
    }
}
