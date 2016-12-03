package com.example.os1.mygeo.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OS1 on 11.08.2016.
 */
public class MyOpponent {

    private String id;

    private float latitude;
    private float longitude;

    private String  live;
    private String  experience;
    private String  level;

    private boolean isActive;
    private boolean isInBattle;

    private List<String> enemiesIdList = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public float getLatitude() {
        return latitude;
    }

    // public void setLatitude(float value) {
    public void setLatitude(String value) {

        // Log.d(MyApp.LOG_TAG, "MyOpponent: setLatitude(): value= " +value);

        if((value != null) && (!value.equals(""))) {

            try {
                latitude = Float.parseFloat(value);
            }
            catch(NumberFormatException exc) {

                exc.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public float getLongitude() {
        return longitude;
    }

    // public void setLongitude(float value) {
    public void setLongitude(String value) {

        // Log.d(MyApp.LOG_TAG, "MyOpponent: setLongitude(): value= " +value);

        // longitude = value;

        if((value != null) && (!value.equals(""))) {

            try {
                longitude = Float.parseFloat(value);
            }
            catch(NumberFormatException exc) {

                exc.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getLive() {
        return live;
    }

    public void setLive(String value) {

        // Log.d(MyApp.LOG_TAG, "MyOpponent: setLive(): value= " +value);

        this.live = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getExperience() {
        return experience;
    }

    public void setExperience(String value) {

        // Log.d(MyApp.LOG_TAG, "MyOpponent: setExperience(): value= " +value);

        this.experience = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getLevel() {
        return level;
    }

    public void setLevel(String value) {

        // Log.d(MyApp.LOG_TAG, "MyOpponent: setLevel(): value= " +value);

        this.level = value;
    }

    /////////////////////////////////////////////////////////////////////////////////

    public boolean isActive() { return isActive; }

    public void setIsActive(String value) {

        // Log.d(MyApp.LOG_TAG, "MyOpponent: setIsActive(): value= " +value);

        if(value != null){

            if(value.equals("1"))
                isActive = true;
            else
                isActive = false;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    public boolean isInBattle() { return isInBattle; }

    public void setIsInBattle(String value) {

        // Log.d(MyApp.LOG_TAG, "MyOpponent: setIsInBattle(): value= " +value);

        if(value != null){

            if(value.equals("1"))
                isInBattle = true;
            else
                isInBattle = false;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    public boolean amIHisEnemy(String userId) {

        return enemiesIdList.contains(userId);
    }

    public void setEnemiesIdList(String value) {

        // Log.d(MyApp.LOG_TAG, "MyOpponent: setEnemiesIdList(): value= " +value);

        if(value != null){

            String[] arr = value.split(",");

            for (String enemyId: arr)
                enemiesIdList.add(enemyId);
        }
    }

}