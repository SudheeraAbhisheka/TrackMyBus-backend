package org.example.bus_tracker_backend.repo;

import org.example.bus_tracker_backend.entities.BusStopEntity;
import org.example.bus_tracker_backend.entities.BusStopId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusStopRepo extends JpaRepository<BusStopEntity, BusStopId> {
    List<BusStopEntity> findByIdRootId(String rootId);
}
