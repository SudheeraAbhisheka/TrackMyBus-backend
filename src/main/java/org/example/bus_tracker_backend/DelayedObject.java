package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class DelayedObject {
    private String stopCoordinate;
    private String session;
    private double delay;

    public DelayedObject(String stopCoordinate, String session, double delay) {
        this.stopCoordinate = stopCoordinate;
        this.session = session;
        this.delay = delay;
    }
}
