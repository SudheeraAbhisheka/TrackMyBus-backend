package org.example.bus_tracker_backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
public class BusEntity {
    @Id
    @GeneratedValue
    private String session_id;
    private String bus_id;
    private String root_id;
    private LocalTime starting_time;
    private String day_of_week;
}
