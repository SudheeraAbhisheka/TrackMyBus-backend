package org.example.bus_tracker_backend;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
public class BusEntity {
    @Id
    private String bus_id;
    private String root_id;
    private LocalTime starting_time;
    private String day_of_week;
}
