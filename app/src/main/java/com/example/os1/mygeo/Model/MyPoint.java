package com.example.os1.mygeo.Model;

import android.location.Location;

/**
 * Created by OS1 on 09.08.2016.
 */
public class MyPoint {

    private Location location;

    private long  time;

    private int speed;

    ///////////////////////////////////////////////////////////////////

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    ///////////////////////////////////////////////////////////////////

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    ///////////////////////////////////////////////////////////////////

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}