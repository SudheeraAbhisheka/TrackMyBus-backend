package org.example.bus_tracker_backend.repo;

import org.example.bus_tracker_backend.BusEntity;
import org.example.bus_tracker_backend.RootEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepo extends JpaRepository<BusEntity, String> {
}
