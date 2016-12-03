package com.example.os1.mygeo.Controller;

import android.content.Context;
import android.content.Intent;
import android.app.Service;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.os1.mygeo.Model.MyApp;
import com.example.os1.mygeo.Model.MyBomb;
import com.example.os1.mygeo.Model.MyOpponent;
import com.example.os1.mygeo.Model.MyPoint;
import com.example.os1.mygeo.Model.MyUser;
import com.example.os1.mygeo.Model.MyWeapon;
import com.example.os1.mygeo.View.Main_Activity;
import com.example.os1.mygeo.View.Second_Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by OS1 on 12.08.2016.
 */
public class SendPointsService extends Service {

    private Context         context;

    // private Main_Activity   activity;
    private Second_Activity activity;
    private MyUser          appUser;

    public  MyBinder        binder = new MyBinder();
    private Timer           timer;
    private TimerTask       tTask;

    private RequestQueue    queue;

    private String          appUserId;

    private long            delay    = MyApp.SEND_POINTS_LONG_INTERVAL;  // 20 секунд
    private long            interval = MyApp.SEND_POINTS_LONG_INTERVAL;  // 20 секунд

    private List<String> myPointsList;

    private Map<String, MyOpponent>     peoplesMap;

    private OnShowOpponentsListener     showOpponentsListener;
    private OnEnemyStartBattleListener  enemyStartBattleListener;

    private OnSystemErrorListener       systemErrorListener;
    private OnNoPointsListener          noPointsListener;
    private OnNewLogListener            newLogListener;
    private OnBattleStopListener        battleStopListener;

    public interface OnShowOpponentsListener {
        void onShowOpponents(Map<String, MyOpponent> opponentsMap);
    }

    public interface OnEnemyStartBattleListener {
        void onEnemyStartBattle(Map<String, MyOpponent> opponentsMap);
    }

    @Override
    public void onCreate() {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: onCreate()");

        context = this;

        ////////////////////////////////////////////////////////////////////////////////////////////

        activity = MyApp.getMainActivityLink();

        if(activity != null) {

            if (activity instanceof OnShowOpponentsListener) {
                showOpponentsListener = (OnShowOpponentsListener) activity;
            } else {
                throw new ClassCastException(activity.toString() + " must implement OnShowOpponentsListener");
            }

            if (activity instanceof OnEnemyStartBattleListener) {
                enemyStartBattleListener = (OnEnemyStartBattleListener) activity;
            } else {
                throw new ClassCastException(activity.toString() + " must implement OnEnemyStartBattleListener");
            }

            if (activity instanceof OnNewLogListener) {
                newLogListener = (OnNewLogListener) activity;
            } else {
                throw new ClassCastException(activity.toString() + " must implement OnNewLogListener");
            }

            if (activity instanceof OnBattleStopListener) {
                battleStopListener = (OnBattleStopListener) activity;
            } else {
                throw new ClassCastException(activity.toString() + " must implement OnBattleStopListener");
            }

            if (activity instanceof OnSystemErrorListener) {
                systemErrorListener = (OnSystemErrorListener) activity;
            } else {
                throw new ClassCastException(activity.toString() + " must implement OnSystemErrorListener");
            }

            if (activity instanceof OnNoPointsListener) {
                noPointsListener = (OnNoPointsListener) activity;
            } else {
                throw new ClassCastException(activity.toString() + " must implement OnNoPointsListener");
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        // если пользователь создан
        if((MyApp.getUser() != null)) {

            // получаем ссылку на пользователя
            appUser = MyApp.getUser();

            // если идентификатор пользователя задан
            if(appUser.getId() != null) {

                // получаем значение
                appUserId = appUser.getId();

                // если список координат получен
                if (appUser.getPointsList() != null) {

                    // если получен список с координатами местоположения пользовтеля
                    myPointsList = appUser.getPointsList();

                    ////////////////////////////////////////////////////////////////////////////////////////////

                    queue = Volley.newRequestQueue(context);

                    ////////////////////////////////////////////////////////////////////////////////////////////

                    timer = new Timer();
                }
            }
            // если идентификатор пользователя не задан
            else
                // показать ошибку приложения
                systemErrorListener.showSystemError();
        }
        // если пользователь не создан
        else
            // показать ошибку приложения
            systemErrorListener.showSystemError();
    }

    public void schedule() {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: schedule()");

        cancelTask();

        ///////////////////////////////////////////////////////////////////////

        tTask = new TimerTask() {
            public void run() {

                // Log.d(MyApp.LOG_TAG, "--> SendPointsService: schedule(): new TimerTask() with interval= " +interval);

                // задаем фиктивное местоположение
                Location location = new Location("");
                location.setLatitude(MyApp.MY_DEF_LOCATION.latitude);
                location.setLongitude(MyApp.MY_DEF_LOCATION.longitude);

                // добавить координату
                appUser.addPoint(location);

                /////////////////////////////////////////////////////////////////////////

                int pointsSum = myPointsList.size();

                // Log.d(MyApp.LOG_TAG, "SendPointsService: schedule(): pointsSum= " +pointsSum);

                if(pointsSum > 0)
                    sendPoints();
                else if(pointsSum == 0)
                    // если точки не получены, сообщаем об ошибке с определением местоположения
                    noPointsListener.showLocationError();
            }
        };

        timer.schedule(tTask, delay, interval);
    }

    public void setInterval(long value) {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: setInterval()");

        // Log.d(MyApp.LOG_TAG, "--> SendPointsService: setInterval(): to= " +value);

        interval = value;

        schedule();
    }

    public IBinder onBind(Intent arg0) {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: onBind()");

        return binder;
    }

    public void cancelTask() {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: cancelTask()");

        // Log.d(MyApp.LOG_TAG, "--> SendPointsService: cancelTask(): tTask is null " +(tTask == null));

        if (tTask != null)
            tTask.cancel();
    }

    public class MyBinder extends Binder {

        public SendPointsService getService() {

            // Log.d(MyApp.LOG_TAG, "SendPointsService: MyBinder: getService()");

            return SendPointsService.this;
        }
    }

    private void sendPoints() {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: sendPoints()");

        // если идентификатор пользовтаеля задан
        if((appUserId != null) && (!appUserId.equals(""))) {

            // получаем список с точками местоположения пользовтеля
            List<String> sendPointsList = new ArrayList<>();
            sendPointsList.addAll(myPointsList);

            // если данные получены
            if (sendPointsList != null) {

                // формируем блок данных для отправки на сервер

                JSONObject sendPointsJSONObj = new JSONObject();
                JSONObject userJSONObj       = new JSONObject();

                try {

                    /////////////////////////// POSITION ////////////////////////////////

                    // формируем блок POSITION

                    JSONArray positionJSONArr = new JSONArray();

                    for (int i = (sendPointsList.size() - 1); i >= 0; i--) {

                        MyPoint sendPoint = appUser.getMyPointFromMap(sendPointsList.get(i));

                        JSONObject pointJSONObj = new JSONObject();
                        pointJSONObj.put("lat",  "" + sendPoint.getLocation().getLatitude());
                        pointJSONObj.put("lon",  "" + sendPoint.getLocation().getLongitude());
                        pointJSONObj.put("time", "" + sendPoint.getTime());

                        positionJSONArr.put(pointJSONObj);

                        // если это НЕ самая "свежая" точка
                        if (i > 0)
                            // удаляем ее за ненадобностью
                            MyApp.getUser().removePoint(sendPointsList.get(i));
                    }

                    // вкладываем блок POSITION в блок данных пользователя
                    userJSONObj.put("POSITION", positionJSONArr);

                    /////////////////////////// GAME_STATUS ////////////////////////////////

                    // если пользователь уже получил данные для инициализации
                    if (appUser.isInitialized()) {

                        // формируем блок GAME_STATUS

                        JSONObject gameStatusJSONObj = new JSONObject();

                        int isActive = 0;
                        int isWar = 0;

                        // если готов к бою
                        if (appUser.isActive())
                            // меняем значение
                            isActive = 1;

                        // если уже в бою
                        if (appUser.isInBattle())
                            // меняем значение
                            isWar = 1;

                        gameStatusJSONObj.put("is_active",      "" + isActive);
                        gameStatusJSONObj.put("is_war",         "" + isWar);
                        gameStatusJSONObj.put("id_opponents",   MyApp.getEnemyId());
                        gameStatusJSONObj.put("time_to_ping",   "");

                        // вкладываем блок GAME_STATUS в данные пользователя
                        userJSONObj.put("GAME_STATUS", gameStatusJSONObj);
                    }

                    /////////////////////////// WEAPON ////////////////////////////////

                    // если пользователь выбрал другое оружие
                    if(appUser.isNewWeaponSelected()) {

                        // формируем блок WEAPON

                        JSONObject allWeaponJSONObj = new JSONObject();

                        MyWeapon newWeaponForSelect = appUser.getNewWeaponForSelect();

                        // получаем список оружия пользователя
                        List<MyWeapon> userWeaponList = appUser.getMyWeaponList();

                        for(int i=0; i<userWeaponList.size(); i++) {

                            JSONObject weaponJSONObj = new JSONObject();

                            MyWeapon weapon = userWeaponList.get(i);

                            int is_selected = 0;

                            // если это оружие выбрано
                            if (weapon.getId().equals(newWeaponForSelect.getId()))
                                // меняем значение
                                is_selected = 1;

                            weaponJSONObj.put("type",           weapon.getType());
                            weaponJSONObj.put("damage",         weapon.getDamage());
                            weaponJSONObj.put("is_selected",    "" +is_selected);
                            weaponJSONObj.put("quantity",       weapon.getQuantity());
                            weaponJSONObj.put("speed_fly",      weapon.getSpeedFly());
                            weaponJSONObj.put("strength",       weapon.getStrength());
                            weaponJSONObj.put("attack_radius",  weapon.getAttackRange());

                            // добавляем блок данных очередного оружия в блок WEAPON
                            allWeaponJSONObj.put(weapon.getId(), weaponJSONObj);
                        }

                        // вкладываем блок WEAPON в блок данных пользователя
                        userJSONObj.put("WEAPON", allWeaponJSONObj);
                    }

                    ///////////////////////////////////////////////////////////

                    // вкладываем блок с данными пользователя в блок PLAYERS
                    JSONObject playersJSONObj = new JSONObject();
                    playersJSONObj.put(appUserId, userJSONObj);

                    ////////////////////////////// PLAYERS /////////////////////////////////////

                    // вкладываем блок PLAYERS в блок данных для отправки
                    sendPointsJSONObj.put("PLAYERS", playersJSONObj);

                    // отправляем данные на сервер
                    sendPointsData(sendPointsJSONObj);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendPointsData(JSONObject jsonObject) {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData()");

        // Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData(): serverUrl= " + MyApp.getServerUrl());
        Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData(): JSONObject to send= " + jsonObject.toString());

        ////////////////////////////////////////////////////////////////////////////////////////////

        newLogListener.addLog("request",jsonObject.toString(), interval);

        ////////////////////////////////////////////////////////////////////////////////////////////

        // формируем параметры запроса к серверу
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("pointsJSONObj", jsonObject.toString());

        ////////////////////////////////////////////////////////////////////////////////////////////

        CustomJSONObjRequest request = new CustomJSONObjRequest(Request.Method.POST, MyApp.getServerUrl(), requestBody,

                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(MyApp.LOG_TAG, "--> SendPointsService: sendPointsData(): onResponse: response is null: " +(response == null));

                        StringBuilder sbd = new StringBuilder("");

                        if(response != null) {

                            Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData(): onResponse: response= " + response.toString());

                            sbd.append(response.toString());
                        }

                        ////////////////////////////////////////////////////////////////////////////////////////////

                        newLogListener.addLog("response",sbd.toString(), interval);

                        ////////////////////////////////////////////////////////////////////////

                        // Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData(): battleState is NO_BATTLE: " +(MyApp.getBattleState() == MyApp.NO_BATTLE));

                        // если обычный режим
                        if(MyApp.getBattleState() == MyApp.NO_BATTLE) {

                            // Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData(): battleState is NO_BATTLE");

                            // если среди оппонентов не найден тот, кто выстрелил в меня первым
                            // if(!enemyFoundInJSONData(response)) {
                            if(!readData(response)) {

                                // Log.d(MyApp.LOG_TAG, "SendPointsService: (0) sendPointsData(): peoplesMap.size= " +peoplesMap.size());

                                // если слушатель задан
                                if(showOpponentsListener != null) {

                                    // отображаем оппонентов на карте
                                    showOpponentsListener.onShowOpponents(peoplesMap);
                                }
                                // если слушатель не задан
                                else
                                    // показать ошибку приложения
                                    systemErrorListener.showSystemError();
                            }
                            // если среди оппонентов найден тот, кто выстрелил в меня первым
                            else {

                                // Log.d(MyApp.LOG_TAG, "SendPointsService: (1) sendPointsData(): peoplesMap.size= " +peoplesMap.size());

                                // если слушатель задан
                                if(enemyStartBattleListener != null) {

                                    // начинаем бой с определившимся врагом
                                    enemyStartBattleListener.onEnemyStartBattle(peoplesMap);
                                }
                                // если слушатель не задан
                                else
                                    // показать ошибку приложения
                                    systemErrorListener.showSystemError();
                            }
                        }
                        // если это боевой режим
                        else {

                            // Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData(): battleState is BATTLE");

                            // readEnemyDataFromJSON(response);

                            // если бой продолжается
                            if(battleContinue(response)) {

                                // Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData(): battleState continue");

                                // если слушатель задан
                                if(showOpponentsListener != null) {

                                    // отображаем врага на карте
                                    showOpponentsListener.onShowOpponents(peoplesMap);
                                }
                                // если слушатель не задан
                                else
                                    // показать ошибку приложения
                                    systemErrorListener.showSystemError();
                            }
                            // если соперник уже не в бою
                            else {

                                // Log.d(MyApp.LOG_TAG, "SendPointsService: sendPointsData(): battleState stop");

                                // если слушатель задан
                                if(battleStopListener != null) {

                                    // завершаем бой
                                    battleStopListener.onBattleStop();
                                }
                                // если слушатель не задан
                                else
                                    // показать ошибку приложения
                                    systemErrorListener.showSystemError();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d(MyApp.LOG_TAG, "--> SendPointsService: sendPointsData(): onErrorResponse: error= " + error.toString());

                        StringBuilder sbd = new StringBuilder("");

                        if(error != null)
                            sbd.append(error.toString());

                        ////////////////////////////////////////////////////////////////////////////////////////////

                        newLogListener.addLog("response",sbd.toString(), interval);
                    }
                });

        queue.add(request);
    }

    private boolean readData(JSONObject jsonObj) {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: readData()");

        boolean enemyFound = false;

        peoplesMap = new HashMap<>();

        // получаем из jsonObj "PLAYERS"
        JSONObject playersJSONObj = getJSONObj(jsonObj,"PLAYERS");

        // если PLAYERS получен
        if(playersJSONObj != null) {

            // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): PLAYERS получен");

            // получаем очередного игрока
            Iterator<String> keysIterator = playersJSONObj.keys();

            while (keysIterator.hasNext()) {

                // получаем идентификатор игрока
                String userId = keysIterator.next();

                // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): userId= " +userId);

                // если идентификатор пользователя получен
                if ((userId != null) && (!userId.equals("")) && (!userId.equals("0"))) {

                    // получаем блок данных пользователя
                    JSONObject userJSONObj = getJSONObj(playersJSONObj, userId);

                    // если USERS получен
                    if (userJSONObj != null) {

                        // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): USERS получен");

                        // если это мой блок данных
                        if (userId.equals(appUser.getId())) {

                            // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): это мой блок данных");

                            // обновляем мои данные
                            updateMyUserData(userJSONObj);
                        }
                        // если это не мой блок данных
                        else {

                            // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): это блок данных оппонента");

                            // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): враг уже найден= " + enemyFound);

                            // если враг еще на найден
                            if (!enemyFound) {

                                // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): формируем оппонента");

                                // получаю очередного оппонента
                                MyOpponent myOpponent = getMyOpponent(userJSONObj, userId);

                                // если оппонент получен
                                if (myOpponent != null) {

                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): оппонент сформирован");

                                    // определяю стрелял ли он в меня первым
                                    enemyFound = findEnemy(myOpponent);

                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): это и есть мой враг= " +enemyFound);
                                }
                                // если оппонент не сформирован
                                // else
                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): оппонент не сформирован");
                            }
                            // если враг уже найден
                            // else
                                // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): враг уже найден");
                        }
                    }
                    // если USERS не получен
                    // else
                        // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): USERS не получен");
                }
                // идентификатор пользователя не получен
                // else
                    // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): идентификатор пользователя не получен");
            }
        }
        // если PLAYERS не получен
        // else
            // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): PLAYERS не получен");

        return enemyFound;
    }

    // private boolean findEnemy(JSONObject userJSONObj, MyOpponent myOpponent) {
    private boolean findEnemy(MyOpponent myOpponent) {

        String enemyId = myOpponent.getId();

        // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): opponentId= " +enemyId);

        // peoplesMap = new HashMap<>();

        // если найдется тот, кто по мне выстрелил первым, будет выставлено true
        boolean enemyFound = false;

        // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): isActive: " +myOpponent.isActive());
        // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): isInBattle: " +myOpponent.isInBattle());
        // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): amIHisEnemy: " +myOpponent.amIHisEnemy(appUser.getId()));

        // если оппонент активен, находится в состоянии боя и я указан среди его врагов
        if(myOpponent.isActive() && myOpponent.isInBattle() && myOpponent.amIHisEnemy(appUser.getId())) {

            // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): да это мой враг");

            // сообщаю что враг, который выстрелил в меня первым определен
            enemyFound = true;

            // запоминаю его идентификатор, как идентификатор моего врага
            MyApp.setEnemyId(enemyId);

            // удаляю из колллекции всех кого раньше туда добавил
            peoplesMap.clear();

            // кладу врага в коллекцию
            peoplesMap.put(enemyId, myOpponent);

            // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): чищу коллекцию оппонентов и кладу туда только врага");
        }
        // если оппонент не мой враг
        else {

            // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): нет он не мой враг");

            // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): isActive: " +myOpponent.isActive());
            // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): isInBattle: " +myOpponent.isInBattle());

            // если оппонент готов к бою и ни с кем не воюет
            if(myOpponent.isActive() && (!myOpponent.isInBattle())) {

                // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): да с ним можно сразиться");

                // добавляем его в коллекцию
                peoplesMap.put(enemyId, myOpponent);

                // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): добавляю в коллекцию оппонента= " +enemyId);
            }
            // если оппонент к бою не готов
            // else
                // Log.d(MyApp.LOG_TAG, "SendPointsService: findEnemy(): нет он не готов к бою");
        }

        return enemyFound;
    }

    private boolean battleContinue(JSONObject jsonObj) {

        boolean battleContinue = true;

        /////////////////////////////////////////////////////////////////////////////

        // получаем сохраненный идентификатор врага
        String enemyId = MyApp.getEnemyId();

        // Log.d(MyApp.LOG_TAG, "SendPointsService: battleContinue(): enemyId= " +enemyId);

        // если идентификатор врага получен
        if((enemyId != null) && (!enemyId.equals(""))) {

            // получаем из jsonObj "PLAYERS"
            JSONObject playersJSONObj = getJSONObj(jsonObj,"PLAYERS");

            // если playersJSONObj получен
            if(playersJSONObj != null) {

                // получаем очередного игрока
                Iterator<String> keysIterator = playersJSONObj.keys();

                while (keysIterator.hasNext()) {

                    // получаем идентификатор игрока
                    String userId = keysIterator.next();

                    // если идентификатор пользователя получен и это мой враг
                    if ((userId != null) && (userId.equals(enemyId))) {

                        // читаем блок с его данными
                        JSONObject userJSONObj = getJSONObj(playersJSONObj,userId);

                        // если блок с данными получен
                        if(userJSONObj != null) {

                            // получаем оппонента с заполненными данными
                            MyOpponent myOpponent = getMyOpponent(userJSONObj, userId);

                            // если оппонент получен
                            if(myOpponent != null) {

                                // Log.d(MyApp.LOG_TAG, "SendPointsService: battleContinue(): enemy is active: " +myOpponent.isActive());

                                // если враг активен
                                if(myOpponent.isActive()) {

                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: battleContinue(): enemy is in battle: " +myOpponent.isInBattle());
                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: battleContinue(): he is my enemy: " +myOpponent.amIHisEnemy(appUser.getId()));

                                    // если враг в бою со мной
                                    if(myOpponent.isInBattle() && myOpponent.amIHisEnemy(appUser.getId())) {

                                        // запоминаем его идентификатор, как идентификатор врага
                                        MyApp.setEnemyId(userId);

                                        // чистим колллекцию от прежних оппонентов
                                        peoplesMap.clear();

                                        // добавляем оппонента в коллекцию
                                        peoplesMap.put(userId, myOpponent);
                                    }
                                    // если враг уже не в бою со мной
                                    else
                                        battleContinue = false;
                                }
                                // если оппонент уже не активен
                                else
                                    battleContinue = false;
                            }
                        }
                    }
                }
            }
            // если блок "PLAYERS" не получен
//            else
//                Log.d(MyApp.LOG_TAG, "SendPointsService: battleContinue(): playersJSONObj не получен");
        }

        return battleContinue;
    }

//    private boolean enemyFoundInJSONData(JSONObject jsonObj) {


        // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData()");

//        peoplesMap = new HashMap<>();

        // если найдется тот, кто по мне выстрелил первым, будет выставлено true
//        boolean enemyFound = false;

//        if(jsonObj.has("PLAYERS")) {

//            try {

//                JSONObject playersJSONObj = jsonObj.getJSONObject("PLAYERS");

//                if(playersJSONObj != null) {

                    // получаем очередного игрока
//                    Iterator<String> keysIterator = playersJSONObj.keys();

//                    while (keysIterator.hasNext()) {

                        // получаем идентификатор игрока
//                        String userId = keysIterator.next();

                        // если значение получено
//                        if(userId != null) {

                            // получаем блок с его данными
//                            JSONObject userJSONObj = playersJSONObj.getJSONObject(userId);

                            // если блок данных получен
//                            if (userJSONObj != null) {

//                                // если это мой блок данных
//                                if(userId.equals(appUser.getId()))
//                                    // обновляем мои данные
//                                    updateMyUserData();
//                                else
//
//                            }
//
//                        }



                        // если враг еще не найден
//                        if(!enemyFound) {

//                            String latitude     = "";
//                            String longitude    = "";
//                            String isActiveStr  = "";
                            // String enemyId = "";

                            // если идентификатор задан
//                            if (opponentId != null) {

                                // если идентификатор получен
//                                if ((!opponentId.equals("")) && (!opponentId.equals("0"))) {

//                                    MyOpponent myOpponent = new MyOpponent();
//                                    myOpponent.setId(opponentId);

                                    ////////////////////////////////////////////////////////////////////////////

//                                    JSONObject userJSONObj = playersJSONObj.getJSONObject(opponentId);

//                                    if (userJSONObj != null) {

//                                        JSONArray positionJSONArr = userJSONObj.getJSONArray("POSITION");

//                                        if (positionJSONArr != null) {

//                                            JSONObject opponentPositionJSONObj = (JSONObject) positionJSONArr.get(0);

//                                            if (opponentPositionJSONObj != null) {
//
//                                                if (opponentPositionJSONObj.has("lat")) {
//                                                    latitude = opponentPositionJSONObj.getString("lat");
//
//                                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): (" +i+") latitude= " +latitude);
//                                                }
//
//                                                // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): opponentJSONObj.has(\"lon\"): " +(opponentJSONObj.has("lon")));
//
//                                                if (opponentPositionJSONObj.has("lon")) {
//                                                    longitude = opponentPositionJSONObj.getString("lon");
//
//                                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): (" + i + ") longitude= " + longitude);
//                                                }
//
//                                                ////////////////////////////////////////////////////////////////////
//
//                                                myOpponent.setLatitude(latitude);
//                                                myOpponent.setLongitude(longitude);
//                                            }
//                                        }

//                                        // если в блоке есть объект "STATUS"
//                                        if (userJSONObj.has("STATUS")) {
//
//                                            JSONObject statusJSONObj = userJSONObj.getJSONObject("STATUS");
//
//                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): statusJSONObj is null: " +(statusJSONObj == null));
//
//                                            if (statusJSONObj != null) {
//
//                                                // если параметр получен
//                                                if (statusJSONObj.has("live"))
//                                                    // получаем уровень жизни оппонента
//                                                    myOpponent.setCurrLive(statusJSONObj.getString("live"));
//
//                                                // получаем опыт
//                                                if (statusJSONObj.has("exp")) {
//
////                                                String currUserExp = statusJSONObj.getString("exp");
////
////                                                appUser.setExperience(currUserExp);
//                                                }
//
//                                                // получаем уровень
//                                                if (statusJSONObj.has("level")) {
//
////                                                String currUserLevel = statusJSONObj.getString("level");
////
////                                                appUser.setLevel(currUserLevel);
//                                                }
//                                            }
//                                        }

//                                        // если в блоке есть объект "GAME_STATUS"
//                                        if (userJSONObj.has("GAME_STATUS")) {
//
//                                            JSONObject gameStatusJSONObj = userJSONObj.getJSONObject("GAME_STATUS");
//
//                                            if (gameStatusJSONObj != null) {
//
//                                                // String isActiveStr      = "";
////                                                String isWarStr         = "";
////                                                String idOpponentsStr   = "";
////                                                String timeToPingStr    = "";
////
////                                                // получаем состояние игрока (0-не в игре, 1-в игре)
////                                                if (gameStatusJSONObj.has("is_active"))
////                                                    isActiveStr = gameStatusJSONObj.getString("is_active");
////
////                                                // получаем готовность игрока к бою (0-готов, 1-нет)
////                                                if (gameStatusJSONObj.has("is_war"))
////                                                    isWarStr = gameStatusJSONObj.getString("is_war");
////
////                                                // получаем идентификаторы врагов игрока (содержит данные, если он в режиме боя)
////                                                if (gameStatusJSONObj.has("id_opponents"))
////                                                    idOpponentsStr = gameStatusJSONObj.getString("id_opponents");
////
////                                                // получаем частоту отправки данных на сервер
////                                                if (gameStatusJSONObj.has("time_to_ping"))
////                                                    timeToPingStr = gameStatusJSONObj.getString("time_to_ping");
//
//                                                // значение получено
//                                                // if((isActiveStr != null) && (isActiveStr.equals("1"))) {
//                                                if(isActiveStr != null) {
//
//                                                    // игрок активен
//                                                    if(isActiveStr.equals("1")) {
//
//                                                        // игрок в состоянии боя
//                                                        if ((isWarStr != null) && (isWarStr.equals("1"))) {
//
//                                                            // если мой идентификатор указан среди его врагов
//                                                            if ((idOpponentsStr != null) && (idOpponentsStr.equals(appUser.getId()))) {
//
//                                                                // запоминаю его идентификатор, как идентификатор моего врага
//                                                                MyApp.setEnemyId(opponentId);
//
//                                                                // чистим колллекцию от прежних оппонентов
//                                                                peoplesMap.clear();
//
//                                                                // добавляем оппонента в коллекцию
//                                                                peoplesMap.put(opponentId, myOpponent);
//
//                                                                // сигнализируем, что найден игрок выстреливший в меня первым
//                                                                enemyFound = true;
//                                                            }
//                                                        }
//
//                                                        if()
//                                                    }
//                                                    // игрок не активен
//                                                    else {
//
//
//                                                    }
//                                                }
//                                            }
//                                        }

                                        // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): userJSONObj has \"SHOT\": " +(userJSONObj.has("SHOT")));

//                                        // если в блоке есть объект "SHOT"
//                                        if (userJSONObj.has("SHOT")) {
//
//                                            JSONObject shotJSONObj = userJSONObj.getJSONObject("SHOT");
//
//                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): shotJSONObj is null: " +(shotJSONObj == null));
//
//                                            if (shotJSONObj != null) {

                                                // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): shotJSONObj has \"lon\": " +(shotJSONObj.has("lon")));

                                                // if(shotJSONObj.has("enemy_id")) {

                                                //     enemyId = shotJSONObj.getString("enemy_id");

                                                    // выстрелил в меня
                                                    // if((enemyId != null) && (!enemyId.equals("")) && (enemyId.equals(appUser.getId()))) {

//                                                        // получаем широту
//                                                        if (shotJSONObj.has("lon"))
//                                                            longitude = shotJSONObj.getString("lon");
//
//                                                        // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): shotJSONObj has \"lat\": " +(shotJSONObj.has("lat")));
//
//                                                        // получаем долготу
//                                                        if (shotJSONObj.has("lat"))
//                                                            latitude = shotJSONObj.getString("lat");

                                                        /////////////////////////////////////////////////////////

//                                                        if((longitude != null) && (!longitude.equals("")) && (latitude != null) && (!latitude.equals(""))) {
//
//                                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): longitude= " +longitude);
//                                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): latitude= " +latitude);
//
//                                                            MyBomb bomb = new MyBomb();
//                                                            bomb.setId("" + System.currentTimeMillis());
//                                                            // bomb.setOwnerId(opponentId);
//                                                            bomb.setLatitude(Double.parseDouble(latitude));
//                                                            bomb.setLongitude(Double.parseDouble(longitude));
//
//                                                            // добавляем снаряд в коллекцию
//                                                            MyApp.addEnemyBombToBombHistoryMap(bomb);
//
//                                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): addBombToBombHistoryMap");
//                                                            // Log.d(MyApp.LOG_TAG, "(" +MyApp.getEnemyBombHistoryMap().size() +") add bomb to EnemyBombHistoryMap: lat= " +bomb.getLatitude()+ ", lon= " +bomb.getLongitude());
//                                                        }

                                                        /*
                                                        //////////////////////////////////////////////////////////////////////////////////

                                                        // чистим колллекцию от прежних оппонентов
                                                        peoplesMap.clear();

                                                        // добавляем оппонента в коллекцию
                                                        peoplesMap.put(opponentId, myOpponent);

                                                        // указывает идентификатор врага
                                                        MyApp.setEnemyId(opponentId);

                                                        // сигнализируем, что найден игрок выстреливший в меня первым
                                                        enemyFound = true;*/

                                                        // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): enemyFound with id=" +opponentId);
                                                    // }
                                                // }
//                                            }
//                                        }
//                                    }

                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): enemyFound: " +enemyFound);

                                    // если пока никто не выстрелил в меня первым
                                    // if (!enemyFound) {

                                    // если пока никто не выстрелил в меня первым и оппонент в активном состоянии
//                                    if((!enemyFound) && (isActiveStr != null) && (isActiveStr.equals("1"))) {
//
//                                        // Log.d(MyApp.LOG_TAG, "SendPointsService: enemyFoundInJSONData(): add opponent: " +opponentId);
//
//                                        // добавляем очередного оппонента в коллекцию
//                                        peoplesMap.put(opponentId, myOpponent);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }

//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

//        return enemyFound;
//    }

//    private void readEnemyDataFromJSON(JSONObject jsonObj) {
//
//        // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON()");
//
//        peoplesMap = new HashMap<>();
//
//        boolean battleContinue = true;
//
//        if(jsonObj.has("PLAYERS")) {
//
//            try {
//
//                JSONObject playersJSONObj = jsonObj.getJSONObject("PLAYERS");
//
//                if (playersJSONObj != null) {
//
//                    Iterator<String> keysIterator = playersJSONObj.keys();
//
//                    // получаем идентификатор врага
//                    String enemyId = MyApp.getEnemyId();
//
//                    // если идентификатор получен
//                    if((enemyId != null) && (!enemyId.equals(""))) {
//
//                        // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): enemyId= " +enemyId);
//
//                        while (keysIterator.hasNext()) {
//
//                            // получаем идентификатор очередного оппонента
//                            String opponentId = keysIterator.next();
//
//                            // если идентификатор задан
//                            if ((opponentId != null) && (!opponentId.equals(""))) {
//
//                                // если оппонент и есть искомый враг
//                                if (opponentId.equals(enemyId)) {
//
//                                    MyOpponent myOpponent = new MyOpponent();
//                                    myOpponent.setId(opponentId);
//
//                                    ////////////////////////////////////////////////////////////////////////////
//
//                                    JSONObject userJSONObj = playersJSONObj.getJSONObject(opponentId);
//
//                                    if(userJSONObj != null) {
//
//                                        JSONArray positionJSONArr = userJSONObj.getJSONArray("POSITION");
//
//                                        String latitude     = "";
//                                        String longitude    = "";
//
//                                        if (positionJSONArr != null) {
//
//                                            JSONObject opponentPositionJSONObj = (JSONObject) positionJSONArr.get(0);
//
//                                            if (opponentPositionJSONObj != null) {
//
//                                                if (opponentPositionJSONObj.has("lat")) {
//                                                    latitude = opponentPositionJSONObj.getString("lat");
//
//                                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): (" +i+") latitude= " +latitude);
//                                                }
//
//                                                // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): opponentJSONObj.has(\"lon\"): " +(opponentJSONObj.has("lon")));
//
//                                                if (opponentPositionJSONObj.has("lon")) {
//                                                    longitude = opponentPositionJSONObj.getString("lon");
//
//                                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): (" + i + ") longitude= " + longitude);
//                                                }
//
//                                                ////////////////////////////////////////////////////////////////////
//
//                                                myOpponent.setLatitude(latitude);
//                                                myOpponent.setLongitude(longitude);
//                                            }
//                                        }
//
//                                        // если в блоке есть объект "STATUS"
//                                        if (userJSONObj.has("STATUS")) {
//
//                                            JSONObject statusJSONObj = userJSONObj.getJSONObject("STATUS");
//
//                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): statusJSONObj is null: " +(statusJSONObj == null));
//
//                                            if (statusJSONObj != null) {
//
//                                                // если параметр получен
//                                                if (statusJSONObj.has("live"))
//                                                    // получаем уровень жизни оппонента
//                                                    myOpponent.setCurrLive(statusJSONObj.getString("live"));
//
//                                                // получаем опыт
//                                                if (statusJSONObj.has("exp")) {
//
////                                                String currUserExp = statusJSONObj.getString("exp");
////
////                                                appUser.setExperience(currUserExp);
//                                                }
//
//                                                // получаем уровень
//                                                if (statusJSONObj.has("level")) {
//
////                                                String currUserLevel = statusJSONObj.getString("level");
////
////                                                appUser.setLevel(currUserLevel);
//                                                }
//                                            }
//                                        }
//
//                                        // Log.d(MyApp.LOG_TAG, "--> SendPointsService: readEnemyDataFromJSON(): userJSONObj has \"GAME_STATUS\": " +(userJSONObj.has("GAME_STATUS")));
//
//                                        // если в блоке есть объект "GAME_STATUS"
//                                        if (userJSONObj.has("GAME_STATUS")) {
//
//                                            JSONObject gameStatusJSONObj = userJSONObj.getJSONObject("GAME_STATUS");
//
//                                            if (gameStatusJSONObj != null) {
//
//                                                String isActiveStr      = "";
//                                                String isWarStr         = "";
//                                                String idOpponentsStr   = "";
//                                                String timeToPingStr    = "";
//
//                                                // получаем состояние игрока (0-не в игре, 1-в игре)
//                                                if (gameStatusJSONObj.has("is_active"))
//                                                    isActiveStr = gameStatusJSONObj.getString("is_active");
//
//                                                // получаем готовность игрока к бою (0-готов, 1-нет)
//                                                if (gameStatusJSONObj.has("is_war"))
//                                                    isWarStr = gameStatusJSONObj.getString("is_war");
//
//                                                // получаем идентификаторы врагов игрока (содержит данные, если он в режиме боя)
//                                                if (gameStatusJSONObj.has("id_opponents"))
//                                                    idOpponentsStr = gameStatusJSONObj.getString("id_opponents");
//
//                                                // получаем частоту отправки данных на сервер
//                                                if (gameStatusJSONObj.has("time_to_ping"))
//                                                    timeToPingStr = gameStatusJSONObj.getString("time_to_ping");
//
//                                                // оппонент активен
//                                                if((isActiveStr != null) && (isActiveStr.equals("1"))) {
//
//                                                    // получен флаг - в бою ли он
//                                                    if(isWarStr != null) {
//
//                                                        // если он в бою
//                                                        if(isWarStr.equals("1")) {
//
//                                                            // Log.d(MyApp.LOG_TAG, "--> SendPointsService: readEnemyDataFromJSON(): opponent is in war");
//
//                                                            // если мой идентификатор указан среди его врагов
//                                                            if ((idOpponentsStr != null) && (idOpponentsStr.equals(appUser.getId()))) {
//
//                                                                // запоминаем его идентификатор, как идентификатор врага
//                                                                MyApp.setEnemyId(opponentId);
//
//                                                                // чистим колллекцию от прежних оппонентов
//                                                                peoplesMap.clear();
//
//                                                                // добавляем оппонента в коллекцию
//                                                                peoplesMap.put(opponentId, myOpponent);
//                                                            }
//                                                        }
//                                                        // если он уже НЕ в бою
//                                                        else {
//
//                                                            // Log.d(MyApp.LOG_TAG, "--> SendPointsService: readEnemyDataFromJSON(): opponent is NOT in war");
//
//                                                            // выставляем флаг окончания боя
//                                                            MyApp.setBattleState(MyApp.NO_BATTLE);
//
//                                                            // получаем идентификатор победителя
//                                                            if (gameStatusJSONObj.has("winner_id")) {
//
//                                                                // получаем идентификатор победителя
//                                                                String winnerIdStr = gameStatusJSONObj.getString("winner_id");
//
//                                                                // если идентификатор получен
//                                                                if(winnerIdStr != null)
//                                                                    // сохраняем значение
//                                                                    MyApp.setLastBattleResult(winnerIdStr);
//
//                                                            }
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                        // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): userJSONObj has \"SHOT\": " +(userJSONObj.has("SHOT")));
//
//                                        // если в блоке есть объект "SHOT"
//                                        if (userJSONObj.has("SHOT")) {
//
//                                            JSONObject shotJSONObj = userJSONObj.getJSONObject("SHOT");
//
//                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): shotJSONObj is null: " +(shotJSONObj == null));
//
//                                            if (shotJSONObj != null) {
//
//                                                // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): shotJSONObj has \"lon\": " +(shotJSONObj.has("lon")));
//
//                                                // if (shotJSONObj.has("enemy_id")) {
//
//                                                    String isRealShoot = "";
//
//                                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): shotJSONObj.has(\"is_active\"): " +(shotJSONObj.has("is_active")));
//
//                                                    // получаем широту
//                                                    if (shotJSONObj.has("is_active"))
//                                                        isRealShoot = shotJSONObj.getString("is_active");
//
//                                                    // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): isRealShoot= " +isRealShoot);
//
//                                                    // данные выстрела нужно получить и отобразить маркером на карте
//                                                    if((isRealShoot != null) && (isRealShoot.equals("1"))) {
//
//                                                        // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): isRealShoot!");
//
//                                                        // enemyId = shotJSONObj.getString("enemy_id");
//
//                                                        // выстрелил в меня
//                                                        // if ((enemyId != null) && (!enemyId.equals("")) && (enemyId.equals(appUser.getId()))) {
//
//                                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): shoot on me");
//
//                                                            // получаем широту
//                                                            if (shotJSONObj.has("lon"))
//                                                                longitude = shotJSONObj.getString("lon");
//
//                                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): shotJSONObj has \"lat\": " +(shotJSONObj.has("lat")));
//
//                                                            // получаем долготу
//                                                            if (shotJSONObj.has("lat"))
//                                                                latitude = shotJSONObj.getString("lat");
//
//                                                            /////////////////////////////////////////////////////////
//
//                                                            if ((longitude != null) && (!longitude.equals("")) && (latitude != null) && (!latitude.equals(""))) {
//
//                                                                // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): longitude= " +longitude);
//                                                                // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): latitude= " +latitude);
//
//                                                                MyBomb bomb = new MyBomb();
//                                                                bomb.setId("" + System.currentTimeMillis());
//                                                                // bomb.setOwnerId(opponentId);
//                                                                bomb.setLatitude(Double.parseDouble(latitude));
//                                                                bomb.setLongitude(Double.parseDouble(longitude));
//
//                                                                // добавляем снаряд в коллекцию
//                                                                // MyApp.addBombToBombHistoryMap(bomb);
//                                                                MyApp.addEnemyBombToBombHistoryMap(bomb);
//
//                                                                // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): addBombToBombHistoryMap");
//                                                                // Log.d(MyApp.LOG_TAG, "(" +MyApp.getEnemyBombHistoryMap().size() +") add bomb to EnemyBombHistoryMap: lat= " +bomb.getLatitude()+ ", lon= " +bomb.getLongitude());
//                                                            }
//
//                                                            //////////////////////////////////////////////////////////////////////////////////
//
////                                                        // чистим колллекцию от прежних оппонентов
////                                                        peoplesMap.clear();
////
////                                                            // обновляем оппонента в коллекции
////                                                            peoplesMap.put(opponentId, myOpponent);
////
////                                                        // указывает идентификатор врага
////                                                        MyApp.setEnemyId(opponentId);
////
////                                                        // сигнализируем, что найден игрок выстреливший в меня первым
////                                                        enemyFound = true;
//
//                                                            // Log.d(MyApp.LOG_TAG, "SendPointsService: readEnemyDataFromJSON(): enemyFound with id=" + opponentId);
//                                                        // }
//                                                    }
//                                                // }
//                                            }
//                                        }
//
//                                        // обновляем оппонента в коллекции
//                                        peoplesMap.put(opponentId, myOpponent);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            // если продолжается бой
//            if(MyApp.getBattleState() == MyApp.BATTLE) {
//
//                // просто отображаем оппонента на карте
//                if (showOpponentsListener != null)
//                    showOpponentsListener.onShowOpponents(peoplesMap);
//            }
//            // если соперник уже не в бою
//            else {
//
//                // завершаем бой
//                if (battleStopListener != null)
//                    battleStopListener.onBattleStop();
//            }
//        }
//    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private void updateMyUserData(JSONObject userJSONObj) {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: readData(): updateMyUserData()");

        ///////////////////////////////////////// STATUS /////////////////////////////////////////

        // получаю блок STATUS
        JSONObject statusJSONObj = getJSONObj(userJSONObj,"STATUS");

        // если блок данных полуен
        if (statusJSONObj != null) {

            // получаем уровень жизни
            String live = getStringFromJSONObj(statusJSONObj,"live");

            // если live получен
            if(live != null)
                // сохраняем значение
                appUser.setCurrLive(live);

            ////////////////////////////////////////////////////////////////////////

            // получаем опыт
            String exp = getStringFromJSONObj(statusJSONObj,"exp");

            // если exp получен
            if(exp != null)
                // сохраняем значение
                appUser.setExperience(exp);

            ////////////////////////////////////////////////////////////////////////

            // получаем уровень
            String level = getStringFromJSONObj(statusJSONObj,"level");

            // если level получен
            if(level != null)
                // сохраняем значение
                appUser.setLevel(level);
        }

        /////////////////////////////////////// GAME_STATUS ///////////////////////////////////////

        // получаю блок GAME_STATUS
        JSONObject gameStatusJSONObj = getJSONObj(userJSONObj,"GAME_STATUS");

        // если блок данных полуен
        if (gameStatusJSONObj != null) {

            // если пользователь еще не получал данные для инициализации
            if(!appUser.isInitialized())
                // меняем флаг
                appUser.setIsInitialized(true);

            ////////////////////////////////////////////////////////////////////////

            // получаем состояние игрока (0-не готов к бою, 1-готов к бою)
            String isActive = getStringFromJSONObj(gameStatusJSONObj,"is_active");

            // если isActive получен
            if(isActive != null)
                // сохраняем значение
                appUser.setIsActive(isActive);

            ////////////////////////////////////////////////////////////////////////

            // получаем состояние игрока (0- уже не в бою, 1- еще в бою)
            String isWar = getStringFromJSONObj(gameStatusJSONObj,"is_war");

            // если isWar получен
            if(isWar != null)
                // сохраняем значение
                appUser.setIsInBattle(isWar);

            ////////////////////////////////////////////////////////////////////////

            // получаем идентификаторы врагов игрока (содержит данные, только если он еще в режиме боя)
            String enemiesIds = getStringFromJSONObj(gameStatusJSONObj,"id_opponents");

            ////////////////////////////////////////////////////////////////////////

            // получаем частоту отправки данных на сервер
            String timeToPing = getStringFromJSONObj(gameStatusJSONObj,"time_to_ping");

            ////////////////////////////////////////////////////////////////////////

            // получаем идентификатор победителя боя
            String winnerId = getStringFromJSONObj(gameStatusJSONObj,"winner_id");

            // если winnerId получен
            if((winnerId != null) && (!winnerId.equals("")))
                // запоминаем идентификатор игрока, который победил
                MyApp.setLastBattleResult(winnerId);
        }

        /////////////////////////////////////// WEAPON //////////////////////////////////////////

        JSONObject allWeaponsJSONObj = getJSONObj(userJSONObj,"WEAPON");

        // если блок со всем оружием получен
        if (allWeaponsJSONObj != null) {

            // будем хранить кол-во полученных орудий
            int weaponsSum = 0;

            // проходим итератором по блоку данных
            Iterator<String> weaponIterator = allWeaponsJSONObj.keys();

            // если данные еще есть, получаем следующий элемент
            while (weaponIterator.hasNext()) {

                // читаем идентификатор
                String weaponId = weaponIterator.next();

                // считаем кол-во полученных элементов
                weaponsSum++;
            }

            // если получено хоть одно оружие
            if(weaponsSum > 0) {

                MyWeapon newWeaponForSelect = appUser.getNewWeaponForSelect();

                // меняем выбранное оружие
                appUser.setMySelectedWeapon(newWeaponForSelect);

                // сообщаем, что выбранное оружие изменено
                appUser.setIsNewWeaponSelected(false);

                // затираем прежнее значение нового оружия для выбора
                appUser.setNewWeaponForSelect(null);
            }
        }
    }

    private MyOpponent getMyOpponent(JSONObject userJSONObj, String userId) {

        // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): userId= " +userId);

        MyOpponent myOpponent = new MyOpponent();
        myOpponent.setId(userId);

        /////////////////////////////////////// POSITION //////////////////////////////////////////

        // получаю блок POSITION
        JSONArray positionJSONArr = getJSONArr(userJSONObj,"POSITION");

        // если POSITION получен
        if(positionJSONArr != null) {

            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): POSITION получен");

            try {
                // получаем блок с координатами оппонента
                JSONObject opponentPositionJSONObj = (JSONObject) positionJSONArr.get(0);

                // если блок с данными получен
                if (opponentPositionJSONObj != null) {

                    // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): opponentPositionJSONObj получен");

                    // получаем широту
                    String latitude = getStringFromJSONObj(opponentPositionJSONObj,"lat");

                    // если latitude получен
                    if(latitude != null)
                        // сохраняем значение
                        myOpponent.setLatitude(latitude);

                    ////////////////////////////////////////////////////////////////////////

                    // получаем долготу
                    String longitude = getStringFromJSONObj(opponentPositionJSONObj,"lon");

                    // если longitude получен
                    if(longitude != null)
                        // сохраняем значение
                        myOpponent.setLongitude(longitude);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // если POSITION не получен
        // else
            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): POSITION не получен");

        ///////////////////////////////////////// STATUS /////////////////////////////////////////

        // получаю блок STATUS
        JSONObject statusJSONObj = getJSONObj(userJSONObj,"STATUS");

        // если блок данных полуен
        if (statusJSONObj != null) {

            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): STATUS получен");

            // получаем уровень жизни
            String live = getStringFromJSONObj(statusJSONObj,"live");

            // если live получен
            if(live != null)
                // сохраняем значение
                myOpponent.setLive(live);

            ////////////////////////////////////////////////////////////////////////

            // получаем опыт
            String exp = getStringFromJSONObj(statusJSONObj,"exp");

            // если exp получен
            if(exp != null)
                // сохраняем значение
                myOpponent.setExperience(exp);

            ////////////////////////////////////////////////////////////////////////

            // получаем уровень
            String level = getStringFromJSONObj(statusJSONObj,"level");

            // если level получен
            if(level != null)
                // сохраняем значение
                myOpponent.setLevel(level);
        }
        // если STATUS не получен
        // else
            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): STATUS не получен");

        /////////////////////////////////////// GAME_STATUS ///////////////////////////////////////

        // получаю блок GAME_STATUS
        JSONObject gameStatusJSONObj = getJSONObj(userJSONObj,"GAME_STATUS");

        // если блок данных полуен
        if (gameStatusJSONObj != null) {

            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): GAME_STATUS получен");

            // получаем состояние игрока (0-не готов к бою, 1-готов к бою)
            String isActive = getStringFromJSONObj(gameStatusJSONObj,"is_active");

            // если isActive получен
            if(isActive != null)
                // сохраняем значение
                myOpponent.setIsActive(isActive);

            ////////////////////////////////////////////////////////////////////////

            // получаем состояние игрока (0- уже не в бою, 1- еще в бою)
            String isWar = getStringFromJSONObj(gameStatusJSONObj,"is_war");

            // если isWar получен
            if(isWar != null)
                // сохраняем значение
                myOpponent.setIsInBattle(isWar);

            ////////////////////////////////////////////////////////////////////////

            // получаем идентификаторы врагов игрока (содержит данные, только если он еще в режиме боя)
            String enemiesIds = getStringFromJSONObj(gameStatusJSONObj,"id_opponents");

            // если enemiesIds получен
            if(enemiesIds != null)
                // заполняем список идентификаторами
                myOpponent.setEnemiesIdList(enemiesIds);

            ////////////////////////////////////////////////////////////////////////

            // получаем частоту отправки данных на сервер
            String timeToPing = getStringFromJSONObj(gameStatusJSONObj,"time_to_ping");

            ////////////////////////////////////////////////////////////////////////

            // если это мой враг
            if(myOpponent.getId().equals(MyApp.getEnemyId())) {

                // получаем идентификатор победителя боя
                String winnerId = getStringFromJSONObj(gameStatusJSONObj, "winner_id");

                // если winnerId получен
                if ((winnerId != null) && (!winnerId.equals("")))
                    // запоминаем идентификатор игрока, который победил
                    MyApp.setLastBattleResult(winnerId);
            }
        }
        // если GAME_STATUS не получен
        // else
            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): GAME_STATUS не получен");

        //////////////////////////////////////// SHOT ////////////////////////////////////////////

        // получаю блок SHOT
        JSONObject shotJSONObj = getJSONObj(userJSONObj,"SHOT");

        // если блок данных полуен
        if (shotJSONObj != null) {

            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): SHOT получен");

            // получаем идентификатор снаряда врага
            String shotId = getStringFromJSONObj(shotJSONObj,"lat");

            ////////////////////////////////////////////////////////////////////////

            // получаем статус выстрела (0 - не надо учитывать, 1 - надо учитывать)
            String shotIsActive = getStringFromJSONObj(shotJSONObj,"is_active");

            ////////////////////////////////////////////////////////////////////////

            // получаем время выстрела
            String shotTime = getStringFromJSONObj(shotJSONObj,"time");

            ////////////////////////////////////////////////////////////////////////

            // получаем широту точки падения снаряда врага
            String latitude = getStringFromJSONObj(shotJSONObj,"lat");

            // получаем долготу точки падения снаряда врага
            String longitude = getStringFromJSONObj(shotJSONObj,"lon");

            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): shotId= " +shotId+ " is active= " +(shotIsActive.equals("1")));

            // если выстрел нужно учесть
            if(shotIsActive.equals("1")) {

                // если обе координаты получены
                if ((latitude != null) && (longitude != null) && (!latitude.equals("")) && (!longitude.equals(""))) {

                    // создаем объект "снаряд"
                    MyBomb enemyBomb = new MyBomb();

                    // если shotId получен и значение задано
                    if ((shotId != null) && (!shotId.equals("")))
                        // сохраняем значение
                        enemyBomb.setId(shotId);
                        // если shotId не получен или значение не задано
                    else
                        // задаем свое значение
                        enemyBomb.setId("" + System.currentTimeMillis());

                    ////////////////////////////////////////////////////////////

                    // bomb.setOwnerId(opponentId);
                    enemyBomb.setLatitude(Double.parseDouble(latitude));
                    enemyBomb.setLongitude(Double.parseDouble(longitude));

                    // добавляем "снаряд" в коллекцию
                    MyApp.addEnemyBombToBombHistoryMap(enemyBomb);

                    // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): addEnemyBombToBombHistoryMap shotId= " +enemyBomb.getId());
                }
            }
        }
        // если SHOT не получен
        // else
            // Log.d(MyApp.LOG_TAG, "SendPointsService: getMyOpponent(): SHOT не получен");

        return myOpponent;
    }

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


    private JSONArray getJSONArr(JSONObject jsonObj,String jsonObjName) {

        JSONArray resultArr = null;

        try {

            // если содержит заданный JSONObject
            if(jsonObj.has(jsonObjName))
                // читаем его
                resultArr = jsonObj.getJSONArray(jsonObjName);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultArr;
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