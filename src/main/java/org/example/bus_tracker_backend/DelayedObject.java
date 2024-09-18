package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class DelayedObject {
    private String stopCoordinate;
    private String session;
    private double expectedInNext;

    public DelayedObject(String stopCoordinate, String session, double expectedInNext) {
        this.stopCoordinate = stopCoordinate;
        this.session = session;
        this.expectedInNext = expectedInNext;
    }
}
