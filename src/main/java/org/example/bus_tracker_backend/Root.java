package org.example.bus_tracker_backend;

import lombok.Data;

@Data
public class Root {
    private int x;
    private double y;
    private final int STARTING_X = 4;
    private final int ENDING_X = 26;

    public double getY(double x){
        return y = 800-300*Math.pow(x/1000-0.4, 6) +
                200*Math.pow(x/1000-0.6, 4) +
                100*Math.pow(x/1000-0.5, 2);
    }
}
