package com.example.os1.mygeo.Model;

/**
 * Created by OS1 on 17.08.2016.
 */
public class MyBomb {

    private String id;

    private int attackRange = 150; // радиус поражения

    private double latitude;
    private double longitude;

    private int shootType = 0;

    /////////////////////////////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int value) { this.attackRange = value; }

    /////////////////////////////////////////////////////////////////////////////////////

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    /////////////////////////////////////////////////////////////////////////////////////

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    /////////////////////////////////////////////////////////////////////////////////////

    public int getShootType() { return shootType; }

    public void setShootType(int value) {

        this.shootType = value;
    }
}