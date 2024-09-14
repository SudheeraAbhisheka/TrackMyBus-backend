package org.example.bus_tracker_backend.entities;

import java.io.Serializable;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class BusStopId implements Serializable {
    private String rootId;
    private int xCoordinate;

    // Getters, setters, equals, and hashCode methods
    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(int xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusStopId that = (BusStopId) o;
        return xCoordinate == that.xCoordinate && Objects.equals(rootId, that.rootId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootId, xCoordinate);
    }
}
