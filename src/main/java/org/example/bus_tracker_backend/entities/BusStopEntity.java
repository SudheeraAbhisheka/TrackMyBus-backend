package org.example.bus_tracker_backend.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;

@Data
@Entity
public class BusStopEntity {
    @EmbeddedId
    private BusStopId id;
    private int secondsFromStart;
}
