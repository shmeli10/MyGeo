package com.example.os1.mygeo.Model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.os1.mygeo.View.Main_Activity;
import com.example.os1.mygeo.View.Second_Activity;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by OS1 on 04.08.2016.
 */
public class MyApp {

    private static MyUser        user;
    // private static Main_Activity main_activity;
    private static Second_Activity main_activity;

    private static boolean      gpsEnabled;
    private static boolean      internetEnabled;
    private static boolean      gyroscopeEnabled;
    private static boolean      mapLongClickEnabled;

    private static float        zoomLevel                   = 13.5f;

    private static int          battleState;
    private static int          userType;
    private static int          lastBattleResult            = 0;

    private static String       serverUrl                   = "http://teach.tw1.ru/bomba/setparams";
    private static String       enemyId                     = "";

    public static final String  LOG_TAG                     = "myLogs";
    public static final String  ANONYM_EMAIL                = "no_email";
    public static final String  ANONYM_PASS                 = "no_password";

    public static final String  ATTRIBUTE_NAME_TEXT         = "text";
    public static final String  ATTRIBUTE_NAME_IMAGE        = "image";

    public static final int     NO_BATTLE                   = 1;
    public static final int     BATTLE                      = 2;

    public static final int     IS_ADMIN                    = 10;
    public static final int     IS_USER                     = 20;

    public static final int     FIRST_SHOOT_TYPE            = 1;
    public static final int     SECOND_SHOOT_TYPE           = 2;
    public static final int     THIRD_SHOOT_TYPE            = 3;

    public static final int     OPPONENT_WON                = 101;
    public static final int     NOBODY_WON                  = 102;
    public static final int     I_WON                       = 103;

    public static final int     AUTH_SUCCESS                = 200;
    public static final int     AUTH_ERROR                  = 201;
    public static final int     SERVER_ERROR                = 202;
    public static final int     ACCOUNT_DISABLED            = 203;
    public static final int     NO_CONNECTION               = 204;

    public static final int     REQUEST_PERMISSION_CODE     = 300;

    public static final long    ONE_SECOND                  = 1000;
    public static final long    SEND_POINTS_SHORT_INTERVAL  = ONE_SECOND;
    public static final long    SEND_POINTS_LONG_INTERVAL   = (20 * ONE_SECOND);

    public static final long    MINIMUM_DISTANCE_FOR_UPDATES = 0;           // в метрах
    public static final long    MINIMUM_TIME_BETWEEN_UPDATES = ONE_SECOND;

    public static final LatLng  MY_DEF_LOCATION = new LatLng(54.5168, 36.2566);

    ////////////////////////////////////////////////////

    private static List<String> logMapKeysList = new ArrayList<>();

    private static Map<String, String[]> logsMap = new HashMap<>();

    ////////////////////////////////////////////////////

    private static Map<String, MyBomb> myBombHistoryMap = new HashMap<>();
    private static Map<String, MyBomb> enemyBombHistoryMap = new HashMap<>();

    ////////////////////////////////////////////////////

    public static boolean isGPSEnabled() { return gpsEnabled; }

    public static void setGpsEnabled(boolean value) { gpsEnabled = value; }

    ////////////////////////////////////////////////////

    public static boolean isInternetEnabled() { return internetEnabled; }

    public static void setInternetEnabled(boolean value) { internetEnabled = value; }

    ////////////////////////////////////////////////////

    public static boolean isGyroscopeEnabled() { return gyroscopeEnabled; }

    public static void setGyroscopeEnabled(boolean value) { gyroscopeEnabled = value; }

    ////////////////////////////////////////////////////

    public static boolean isMapLongClickEnabled() { return mapLongClickEnabled; }

    public static void setMapLongClickEnabled(boolean value) { mapLongClickEnabled = value; }

    ////////////////////////////////////////////////////

    public static MyUser getUser() {
        return user;
    }

    public static void setUser(MyUser user) {
        MyApp.user = user;
    }

    ////////////////////////////////////////////////////

    public static String getServerUrl() { return serverUrl; }

    public static void setServerUrl(String value) { serverUrl = "" +value; }

    ////////////////////////////////////////////////////

    // public static Main_Activity getMainActivityLink() {
    public static Second_Activity getMainActivityLink() {
        return main_activity;
    }

    // public static void setMainActivityLink(Main_Activity activity) {
    public static void setMainActivityLink(Second_Activity activity) {
        main_activity = activity;
    }

    ////////////////////////////////////////////////////

    public static int getBattleState() {
        return battleState;
    }

    public static void setBattleState(int value) {
        battleState = value;
    }

    ////////////////////////////////////////////////////

    public static int getUserType() {
        return userType;
    }

    public static void setUserType(int value) {
        userType = value;
    }

    ////////////////////////////////////////////////////

    public static float getZoomLevel() {
        return zoomLevel;
    }

    public static void setZoomLevel(float value) {
        zoomLevel = value;
    }

    ////////////////////////////////////////////////////

    public static List<String> getLogsMapKeysList() { return logMapKeysList; }

    private static void addLogsMapKeyListItem(String key) {

        if(key != null)
            logMapKeysList.add(key);
    }

    public static Map<String, String[]> getLogsMap() { return logsMap; }

    public static void addLogToMap(String dataType, String dataValue) {

        if((dataType != null) && (dataValue != null)) {

            int mapSize = logsMap.size();

            String key = "" +(mapSize + 1);

            //////////////////////////////////////////////////////////////

            addLogsMapKeyListItem(key);

            logsMap.put(key, new String[] {dataType, dataValue});
        }
    }

    ////////////////////////////////////////////////////

    public static void clearMyBombHistoryMap() {

        if(myBombHistoryMap != null)
            myBombHistoryMap.clear();
    }

    public static Map<String, MyBomb> getMyBombHistoryMap() { return myBombHistoryMap; }

    public static void addMyBombToBombHistoryMap(MyBomb bomb) {

        // Log.d(LOG_TAG, "MyApp: addMyBombToBombHistoryMap(): bomb is null: " +(bomb == null));

        if(bomb != null) {

            // задаем тип выстрела как предварительный
            bomb.setShootType(MyApp.SECOND_SHOOT_TYPE);

            // задаем ключ для коллекции
            String key = bomb.getId();

            // добавляем снаряд в коллекцию
            myBombHistoryMap.put(key, bomb);
        }
    }

    // public static void updateMyBombInBombHistoryMap(String key, String lat, String lon) {
    public static void updateMyBombInBombHistoryMap(String key, double lat, double lon) {

        // Log.d(LOG_TAG, "MyApp: updateMyBombInBombHistoryMap(): bombId: " +key);

        // если коллекция содержит заданный снаряд
        if(myBombHistoryMap.containsKey(key)) {

            // получаем его и обновляем его данные
            MyBomb bomb = myBombHistoryMap.get(key);
            // bomb.setLatitude(Double.parseDouble(lat));
            bomb.setLatitude(lat);
            // bomb.setLongitude(Double.parseDouble(lon));
            bomb.setLongitude(lon);
            bomb.setShootType(MyApp.THIRD_SHOOT_TYPE);
        }
    }

    ////////////////////////////////////////////////////

    public static void clearEnemyBombHistoryMap() {

        if(enemyBombHistoryMap != null)
            enemyBombHistoryMap.clear();
    }

    public static Map<String, MyBomb> getEnemyBombHistoryMap() {

        return enemyBombHistoryMap;
    }

    public static void addEnemyBombToBombHistoryMap(MyBomb bomb) {

        // Log.d(LOG_TAG, "MyApp: addEnemyBombToBombHistoryMap(): bomb is null: " +(bomb == null));

        // если снаряд задан
        if(bomb != null) {

            // задаем ключ для коллекции
            String key = bomb.getId();

            enemyBombHistoryMap.put(key, bomb);
        }
    }

    ////////////////////////////////////////////////////

    public static String getEnemyId() {
        return enemyId;
    }

    public static void setEnemyId(String value) {

        // Log.d(MyApp.LOG_TAG, "MyApp: setEnemyId(): enemyId= " +value);

        // если значение задано
        if(value != null)
            // сохраняем его
            enemyId = value;
    }

    ////////////////////////////////////////////////////

    public static int getLastBattleResult() { return lastBattleResult; }

    public static void setLastBattleResult(String value) {

        // Log.d(MyApp.LOG_TAG, "MyApp: setLastBattleResult(): winner= " +value);

        // если я победил
        if(value.equals(user.getId()))
            lastBattleResult = I_WON;
        // если никто не победил
        else if(value.equals(""))
            lastBattleResult = NOBODY_WON;
        // если победил соперник
        else
            lastBattleResult = OPPONENT_WON;
    }

    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }

        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }

        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }

        return false;
    }
}