package com.example.os1.mygeo.Controller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.os1.mygeo.Model.MyApp;
import com.example.os1.mygeo.Model.MyUser;
import com.example.os1.mygeo.Model.MyWeapon;
import com.example.os1.mygeo.View.Main_Activity;
import com.example.os1.mygeo.View.Second_Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by OS1 on 20.08.2016.
 */
public class SendShootService extends Service {

    private Context                 context;

    // private Main_Activity           activity;
    private Second_Activity         activity;
    private MyUser                  appUser;

    private List<String>            myCurrentShootIdList;
    private Map<String, JSONObject> myShootsMap;

    private OnShootResultListener   shootResultListener;
    private OnNewLogListener        newLogListener;

    public interface OnShootResultListener {
        void onShootResult();
    }

    @Override
    public void onCreate() {

        context = this;

        ////////////////////////////////////////////////////////////////////////////////////////////

        appUser = MyApp.getUser();

        activity = MyApp.getMainActivityLink();

        if(activity != null) {

            if (activity instanceof OnShootResultListener) {
                shootResultListener = (OnShootResultListener) activity;
            } else {
                throw new ClassCastException(activity.toString() + " must implement OnShootResultListener");
            }

            if (activity instanceof OnNewLogListener) {
                newLogListener = (OnNewLogListener) activity;
            } else {
                throw new ClassCastException(activity.toString() + " must implement OnNewLogListener");
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        myCurrentShootIdList = MyApp.getUser().getMyCurrentShootIdList();
        myShootsMap = MyApp.getUser().getMyShootsMap();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        // Log.d(MyApp.LOG_TAG, "SendShootService: onStartCommand()");

        sendShootData();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        // Log.d(MyApp.LOG_TAG, "SendShootService: onDestroy");
    }

    public IBinder onBind(Intent intent) {
        // Log.d(MyApp.LOG_TAG, "SendShootService: onBind");
        return null;
    }

    private void sendShootData() {

        RequestQueue queue = Volley.newRequestQueue(context);

        JSONObject jsonObject = MyApp.getUser().getMyShootsMap().get(myCurrentShootIdList.get(0));

        ////////////////////////////////////////////////////////////////////////////////////////////

        // Log.d(LOG_TAG, "SendShootService: sendShootData(): serverUrl= " +MyApp.getServerUrl());

        Log.d(MyApp.LOG_TAG, "SendShootService: sendShootData(): JSONObject to send= " + jsonObject.toString());

        ////////////////////////////////////////////////////////////////////////////////////////////

        // newLogListener.addLog("request",jsonObject.toString(), interval);
        newLogListener.addLog("request",jsonObject.toString(), 20000);

        ////////////////////////////////////////////////////////////////////////////////////////////

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("shootJSONObj", jsonObject.toString());

        ////////////////////////////////////////////////////////////////////////////////////////////

        CustomJSONObjRequest request = new CustomJSONObjRequest(Request.Method.POST, MyApp.getServerUrl(), requestBody,

                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // Log.d(MyApp.LOG_TAG, "SendShootService: sendShootData(): onResponse: response is null: " + (response == null));

                        StringBuilder sbd = new StringBuilder("");

                        if (response != null) {

                            // Log.d(MyApp.LOG_TAG, "SendShootService: sendShootData(): onResponse: response= " + response.toString());

                            sbd.append(response.toString());

                            readMyShotDataFromJSON(response);
                        }

                        ////////////////////////////////////////////////////////////////////////////////////////////

                        // newLogListener.addLog("response", sbd.toString(), interval);
                        newLogListener.addLog("response", sbd.toString(), 20000);

                        ////////////////////////////////////////////////////////////////////////

                        if(shootResultListener != null)
                            shootResultListener.onShootResult();
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        // Log.d(MyApp.LOG_TAG, "SendShootService: sendShootData(): onErrorResponse: error= " +error.toString());

                        StringBuilder sbd = new StringBuilder("");

                        if(error != null)
                            sbd.append(error.toString());

                        ////////////////////////////////////////////////////////////////////////////////////////////

                        // newLogListener.addLog("response",sbd.toString(), interval);
                        newLogListener.addLog("response",sbd.toString(), 20000);
                    }
                });

        queue.add(request);
    }

    private void readMyShotDataFromJSON(JSONObject jsonObj) {

        // Log.d(MyApp.LOG_TAG, "SendShotService: readUserDataFromJSON()");

        String userId = "";

        // Log.d(MyApp.LOG_TAG, "SendShotService: readUserDataFromJSON(): jsonObj.has(\"PLAYERS\"): " +jsonObj.has("PLAYERS"));

        // получаем из jsonObj "PLAYERS"
        JSONObject playersJSONObj = getJSONObj(jsonObj,"PLAYERS");

        // если playersJSONObj не получен
        if(playersJSONObj == null) {

            // Log.d(MyApp.LOG_TAG, "SendShotService: playersJSONObj не получен");

            // стоп
            return;
        }

        ////////////////////////////////////////////////////////////////////////////////////////

        Iterator<String> keysIterator = playersJSONObj.keys();

        // получаем идентификатор пользователя
        while (keysIterator.hasNext()) {

            userId = keysIterator.next();

            ////////////////////////////////////////////////////////////////////////////////

            // Log.d(MyApp.LOG_TAG, "SendShootService: readMyShotDataFromJSON(): userId is null: " +(userId == null));

            // если идентификатор задан
            if (userId != null) {

                // если идентификатор получен и этой мой блок данных
                if ((!userId.equals("")) && (!userId.equals("0")) && (userId.equals(appUser.getId()))) {

                    // получаем из jsonObj блок данных пользователя
                    JSONObject userJSONObj = getJSONObj(playersJSONObj,userId);

                    // если userJSONObj не получен
                    if(userJSONObj == null) {

                        // Log.d(MyApp.LOG_TAG, "SendShotService: userJSONObj не получен");

                        // стоп
                        return;
                    }

                    //////////////////////////////////////////////////////////////////////////////

                    JSONObject statusJSONObj = getJSONObj(userJSONObj,"STATUS");

                    if (statusJSONObj != null) {

                        // получаем уровень жизни игрока
                        String live = getStringFromJSONObj(statusJSONObj,"live");

                        // если live получен
                        if(live != null)
                            // сохраняем значение
                            appUser.setCurrLive(live);

                        ///////////////////////////////////////////////

                        // получаем опыт игрока
                        String exp = getStringFromJSONObj(statusJSONObj,"exp");

                        // если exp получен
                        if(exp != null)
                            // сохраняем значение
                            appUser.setExperience(exp);

                        ///////////////////////////////////////////////

                        // получаем уровень игрока
                        String level = getStringFromJSONObj(statusJSONObj,"level");

                        // если level получен
                        if(level != null)
                            // сохраняем значение
                            appUser.setLevel(level);
                    }

                    ///////////////////////////////////////////////////////////////////////////////

                    JSONObject shotJSONObj = getJSONObj(userJSONObj,"SHOT");

                    if (shotJSONObj != null) {

                        // получаем идентификатор отправленного выстрела
                        String shotId = getStringFromJSONObj(shotJSONObj,"id");

                        // если shotId получен
                        if((shotId != null) || (shotId.equals(""))) {

                            // получаем широту
                            String latitude = getStringFromJSONObj(shotJSONObj,"lat");

                            // получаем долготу
                            String longitude = getStringFromJSONObj(shotJSONObj,"lon");

                            // если не получено одно из значений, то и обновлять не надо
                            if ((longitude == null) || (longitude.equals("")) || (latitude == null) || (latitude.equals("")))
                                return;

                            // обновляем снаряд в коллекции
                            MyApp.updateMyBombInBombHistoryMap(shotId, Double.parseDouble(latitude), Double.parseDouble(longitude));
                        }
                    }

                    ///////////////////////////////////////////////////////////////////////////////

                    JSONObject allWeaponsJSONObj = getJSONObj(userJSONObj,"WEAPON");

                    // если блок со всем оружием получен
                    if (allWeaponsJSONObj != null) {

                        // получаем свое выбранное оружие
                        MyWeapon mySelectedWeapon = appUser.getSelectedWeapon();

                        // получаю идентификатор выбранного оружия
                        String mySelectedWeaponId = mySelectedWeapon.getId();

                        // проходим итератором по блоку данных
                        Iterator<String> weaponIterator = allWeaponsJSONObj.keys();

                        // если данные еще есть, получаем следующий элемент
                        while (weaponIterator.hasNext()) {

                            // получаем идентификатор оружия
                            String weaponId = weaponIterator.next();

                            // Log.d(MyApp.LOG_TAG, "Login_Activity: readUserDataFromJSON(): weaponId is null: " +(weaponId == null));

                            // если идентификатор задан и он совпадает с моим выбранным оружием
                            if((weaponId != null) && (!weaponId.equals("")) && (!weaponId.equals("0")) && (weaponId.equals(mySelectedWeaponId))) {

                                // получаю данные оружия
                                JSONObject weaponJSONObj = getJSONObj(allWeaponsJSONObj,weaponId);

                                // если данные получены
                                if (weaponJSONObj != null) {

                                    // получаем кол-во снарядов в орудии
                                    String weaponQuantity = getStringFromJSONObj(weaponJSONObj,"quantity");

                                    // если кол-во снарядов в орудии получено
                                    if(weaponQuantity != null)
                                        // сохраняем новое значение
                                        mySelectedWeapon.setQuantity(weaponQuantity);

                                    //////////////////////////////////////////////////////////

                                    // получаем прочность орудия
                                    String weaponStrength = getStringFromJSONObj(weaponJSONObj,"strength");

                                    // если прочность орудия получена
                                    if(weaponStrength != null)
                                        // сохраняем новое значение
                                        mySelectedWeapon.setStrength(weaponStrength);
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    /*private void readMyShotDataFromJSON(JSONObject jsonObj) {

        // Log.d(MyApp.LOG_TAG, "SendShootService: readMyShotDataFromJSON()");

        String userId    = "";

        String shotId    = "";
        String longitude = "";
        String latitude  = "";

        if(jsonObj.has("PLAYERS")) {

            try {

                JSONObject playersJSONObj = jsonObj.getJSONObject("PLAYERS");

                if(playersJSONObj != null) {

                    Iterator<String> keysIterator = playersJSONObj.keys();

                    // получаем идентификатор пользователя
                    while (keysIterator.hasNext()) {

                        userId = keysIterator.next();

                        ////////////////////////////////////////////////////////////////////////////////

                        // Log.d(LOG_TAG, "SendShootService: readMyShotDataFromJSON(): userId is null: " +(userId == null));

                        // если идентификатор задан
                        if (userId != null) {

                            // Log.d(MyApp.LOG_TAG, "SendShootService: readMyShotDataFromJSON(): userId= " + userId);
                            // Log.d(MyApp.LOG_TAG, "SendShootService: readMyShotDataFromJSON(): myUserId= " + appUser.getId());

                            // если идентификатор получен и этой мой блок данных
                            if ((!userId.equals("")) && (!userId.equals("0")) && (userId.equals(appUser.getId()))) {

                                JSONObject userJSONObj = playersJSONObj.getJSONObject(userId);

                                // Log.d(LOG_TAG, "SendShootService: readMyShotDataFromJSON(): userJSONObj is null: " +(userJSONObj == null));

                                // если объект с данными получен
                                if (userJSONObj != null) {

                                    // Log.d(LOG_TAG, "SendShootService: readMyShotDataFromJSON(): userJSONObj has \"STATUS\": " +(userJSONObj.has("STATUS")));

                                    // если в блоке есть объект "STATUS"
                                    if (userJSONObj.has("STATUS")) {

                                        JSONObject statusJSONObj = userJSONObj.getJSONObject("STATUS");

                                        // Log.d(LOG_TAG, "SendShootService: readMyShotDataFromJSON(): statusJSONObj is null: " +(statusJSONObj == null));

                                        if (statusJSONObj != null) {

                                            // получаем уровень жизни
                                            if (statusJSONObj.has("live")) {

                                                String currUserLive = statusJSONObj.getString("live");

                                                appUser.setLive(currUserLive);
                                            }

                                            // получаем опыт
                                            if (statusJSONObj.has("exp")) {

                                                String currUserExp = statusJSONObj.getString("exp");

                                                appUser.setExperience(currUserExp);
                                            }

                                            // получаем уровень
                                            if (statusJSONObj.has("level")) {

                                                String currUserLevel = statusJSONObj.getString("level");

                                                appUser.setLevel(currUserLevel);

                                            }
                                        }

                                    }

                                    // Log.d(MyApp.LOG_TAG, "SendShootService: readMyShotDataFromJSON(): userJSONObj has \"SHOT\": " +(userJSONObj.has("SHOT")));

                                    // если в блоке есть объект "STATUS"
                                    if (userJSONObj.has("SHOT")) {

                                        JSONObject shotJSONObj = userJSONObj.getJSONObject("SHOT");

                                        // Log.d(LOG_TAG, "SendShootService: readMyShotDataFromJSON(): shotJSONObj is null: " +(shotJSONObj == null));

                                        if((shotJSONObj == null) || (!shotJSONObj.has("id")))
                                            return;

                                        // получаем идентификатор отправленного выстрела
                                        shotId = shotJSONObj.getString("id");

                                        if((shotId == null) || (shotId.equals("")))
                                            return;

                                        // получаем широту
                                        if (shotJSONObj.has("lon"))
                                            longitude = shotJSONObj.getString("lon");

                                        // Log.d(LOG_TAG, "SendShootService: readMyShotDataFromJSON(): shotJSONObj has \"lat\": " +(shotJSONObj.has("lat")));

                                        // получаем долготу
                                        if (shotJSONObj.has("lat"))
                                            latitude = shotJSONObj.getString("lat");

                                        /////////////////////////////////////////////////////////

                                        // если не получено одно из значений, то и обновлять не надо
                                        if ((longitude == null) || (longitude.equals("")) || (latitude == null) || (latitude.equals("")))
                                            return;

                                        // обновляем снаряд в коллекции
                                        MyApp.updateMyBombInBombHistoryMap(shotId, Double.parseDouble(latitude), Double.parseDouble(longitude));
                                    }

                                    Log.d(MyApp.LOG_TAG, "SendShootService: readMyShotDataFromJSON(): userJSONObj has \"WEAPON\": " +(userJSONObj.has("WEAPON")));

                                    // если в блоке есть объект "STATUS"
                                    if (userJSONObj.has("WEAPON")) {



                                    }
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }*/

    //////////////////////////////////////////////////////////////////////////////////////////////

    private JSONObject getJSONObj(JSONObject jsonObj,String jsonObjName) {

        JSONObject resultObj = null;

        try {

            // если содержит заданный JSONObject
            if(jsonObj.has(jsonObjName))
                // читаем его
                resultObj = jsonObj.getJSONObject(jsonObjName);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultObj;
    }

    private String getStringFromJSONObj(JSONObject jsonObj,String paramName) {

        String resultStr = null;

        try {

            // если содержит заданный параметр
            if(jsonObj.has(paramName))
                // читаем его
                resultStr = jsonObj.getString(paramName);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultStr;
    }
}