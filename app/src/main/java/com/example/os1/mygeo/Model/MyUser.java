package com.example.os1.mygeo.Model;

import android.location.Location;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OS1 on 11.08.2016.
 */
public class MyUser {

    private String id;
    // private String login;
    private String  email;
    private String  fullLive;
    private String  currLive;
    private String  experience;
    private String  level;

    private boolean isInitialized;
    private boolean isActive;
    private boolean isInBattle;
    private boolean isNewWeaponSelected;

    private MyWeapon mySelectedWeapon;
    private MyWeapon newWeaponForSelect;

    private List<MyWeapon> myWeaponList             = new ArrayList<>();
    private List<String> myPointsList               = new ArrayList<>();
    private List<String> myCurrentShootIdList       = new ArrayList<>();

    private Map<String, MyPoint> myPointsMap        = new HashMap<>();
    private Map<String, MyOpponent> myOpponentsMap  = new HashMap<>();
    private Map<String, JSONObject> myShootsMap     = new HashMap<>();

    private final int maxSpeed = 5;

    ////////////////////////////////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

//    public String getLogin() {
//        return login;
//    }
//
//    public void setLogin(String value) {
//
//        if(value != null)
//            login = value;
//    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {

        if(value != null)
            email = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getFullLive() { return fullLive; }

    public void setFullLive(String value) {

        // Log.d(MyApp.LOG_TAG, "MyUser: setFullLive(): value= " +value);

        this.fullLive = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getCurrLive() {
        return currLive;
    }

    public void setCurrLive(String value) {

        // Log.d(MyApp.LOG_TAG, "MyUser: setCurrLive(): value= " +value);

        this.currLive = value;

        // если здоровье пользователя восстановилось до максимума
        if((fullLive != null) && (!fullLive.equals("") && (!fullLive.equals("0")) && (fullLive.equals(currLive))))
          // сообщаем, что он готов к бою
          setIsActive("1");
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getExperience() {
        return experience;
    }

    public void setExperience(String value) {

        // Log.d(MyApp.LOG_TAG, "MyUser: setExperience(): value= " +value);

        experience = value;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public String getLevel() {
        return level;
    }

    public void setLevel(String value) {

        // Log.d(MyApp.LOG_TAG, "MyUser: setLevel(): value= " +value);

        level = value;
    }

    /////////////////////////////////////////////////////////////////////////////////

    public List<MyWeapon> getMyWeaponList() {
        return myWeaponList;
    }

    public void setMyWeaponList(List<MyWeapon> weaponList) {

        if((weaponList != null) && (!weaponList.isEmpty())) {

            myWeaponList.addAll(weaponList);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    public MyWeapon getSelectedWeapon() {
        return mySelectedWeapon;
    }

    // public void setMySelectedWeapon(String value) {
    public void setMySelectedWeapon(MyWeapon myWeapon) {

        // если выбранное оружие еще не задано
        if(mySelectedWeapon == null)
            // запоминаем ссылку на то, что указано
            mySelectedWeapon = myWeapon;
        // если выбранное оружие уже указано
        else {

            // если выбрано другое оружие
            if (!mySelectedWeapon.getId().equals(myWeapon.getId())) {
                // выставляем прежнему оружие значение, что оно уже не выбрано
                mySelectedWeapon.setIsSelected(false);

                // запоминаем ссылку на новое оружие
                mySelectedWeapon = myWeapon;
                // выставляем новому оружию значение, что оно теперь выбрано
                mySelectedWeapon.setIsSelected(true);
            }
        }

        /*
        int intValue = -1;

        // если значение задано
        if((value != null) && (!value.equals("")))
            // получаем значение
            intValue = Integer.parseInt(value);

        // если значение получено
        if(intValue >= 0) {
            // получаем оружие из списка
            mySelectedWeapon = myWeaponList.get(intValue);

            // Log.d(MyApp.LOG_TAG, "MyUser: setMySelectedWeapon(): mySelectedWeaponId= " +mySelectedWeapon.getId());
        }
        */
    }

    ////////////////////////////////////////////////////////////////////////////////

    public MyWeapon getNewWeaponForSelect() {
        return newWeaponForSelect;
    }

    public void setNewWeaponForSelect(MyWeapon myNewWeapon) {

        this.newWeaponForSelect = myNewWeapon;
    }

    ////////////////////////////////////////////////////////////////////////////////

    public List<String> getPointsList() { return myPointsList; }

    public void addPoint(Location location) {

        // Log.d(MyApp.LOG_TAG, "MyUser: addPoint(): lat= " +location.getLatitude()+ ", lon= " +location.getLongitude());

        int speed = 0;

        long dateTime = System.currentTimeMillis();

        MyPoint myPoint = new MyPoint();
        myPoint.setLocation(location);
        myPoint.setTime(dateTime);
        myPoint.setSpeed(speed);

        if(myPointsList.size() > 0) {

            MyPoint myLastPoint = myPointsMap.get(myPointsList.get(0));

            float myDistance = location.distanceTo(myLastPoint.getLocation());

            int secondsLeft = (int) ((dateTime - myLastPoint.getTime()) / 1000);

            if(myDistance != 0 && secondsLeft != 0) {

                speed = (int) (myDistance / secondsLeft);
                myPoint.setSpeed(speed);
            }

            // Log.d(LOG_TAG, "MyUser: addPoint(): myDistance= " +myDistance);
            // Log.d(LOG_TAG, "MyUser: addPoint(): speed= " +speed);

            if(myDistance != 0 && speed <= maxSpeed) {

                String key = "" +dateTime;

                myPointsList.add(0, key);
                myPointsMap.put(key, myPoint);
            }
        }
        else {

            String key = "" +dateTime;

            myPointsList.add(key);
            myPointsMap.put(key, myPoint);
        }
    }

    public MyPoint getMyPointFromMap(String key) {

        if(myPointsMap.containsKey(key))
            return myPointsMap.get(key);
        else
            return null;
    }

    public void removePoint(String key) {

        if(myPointsList.contains(key)) {

            int position = myPointsList.indexOf(key);

            myPointsList.remove(position);
        }

        if(myPointsMap.containsKey(key))
            myPointsMap.remove(key);
    }

    /////////////////////////////////////////////////////////////////////////////////

    public Map<String, MyOpponent> getMyOpponentsMap() {
        return myOpponentsMap;
    }

    public void removeOpponentFromMap(String key) {

        if(key != null) {

            if(myOpponentsMap.containsKey(key))
                myOpponentsMap.remove(key);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    public List<String> getMyCurrentShootIdList() {
        return myCurrentShootIdList;
    }

    public Map<String, JSONObject> getMyShootsMap() {
        return myShootsMap;
    }

    /////////////////////////////////////////////////////////////////////////////////

    public boolean isInitialized() { return isInitialized; }

    public void setIsInitialized(boolean value) {

        // Log.d(MyApp.LOG_TAG, "MyUser: setIsInitialized(): value= " +value);

        isInitialized = value;
    }

    /////////////////////////////////////////////////////////////////////////////////

    public boolean isActive() { return isActive; }

    public void setIsActive(String value) {

        // Log.d(MyApp.LOG_TAG, "MyUser: setIsActive(): value= " +value);

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

        // Log.d(MyApp.LOG_TAG, "MyUser: setIsInBattle(): value= " +value);

        if(value != null){

            if(value.equals("1"))
                isInBattle = true;
            else
                isInBattle = false;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    public boolean isNewWeaponSelected() { return isNewWeaponSelected; }

    public void setIsNewWeaponSelected(boolean value) {

        // Log.d(MyApp.LOG_TAG, "MyUser: setIsNewWeaponSelected(): value= " +value);

        isNewWeaponSelected = value;
    }
}