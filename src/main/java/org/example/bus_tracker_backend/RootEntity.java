package org.example.bus_tracker_backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class RootEntity {
    @Id
    @GeneratedValue
    private String root_id;
    private int starting_x;
    private int ending_x;
    private String root_function;
    private int bus_stop;
}
