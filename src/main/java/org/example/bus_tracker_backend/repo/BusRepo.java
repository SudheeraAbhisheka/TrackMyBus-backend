package org.example.bus_tracker_backend.repo;

import org.example.bus_tracker_backend.entities.BusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepo extends JpaRepository<BusEntity, String> {
}
