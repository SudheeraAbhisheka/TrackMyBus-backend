package org.example.bus_tracker_backend.repo;

import org.example.bus_tracker_backend.entities.RootEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RootRepo extends JpaRepository<RootEntity, String> {
}
