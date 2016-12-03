package com.example.os1.mygeo.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.os1.mygeo.Controller.CustomJSONObjRequest;
import com.example.os1.mygeo.Model.MyApp;
import com.example.os1.mygeo.Model.MyOpponent;
import com.example.os1.mygeo.Model.MyUser;
import com.example.os1.mygeo.Model.MyWeapon;
import com.example.os1.mygeo.R;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by OS1 on 22.08.2016.
 */
public class Login_Activity extends Activity {

    private Context context;

    private TextView authMsgTV;
    private EditText userEmail;
    private EditText userPassword;

    private CheckBox enterAsAnonym;

    private LinearLayout enterButtonLL;

    // private Map<String, MyOpponent> myOpponentsMap;

    private Map<String, String> userData    = new HashMap<>();
    private List<MyWeapon> userWeaponList   = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        /////////////////////////////////////////////////////////////////////////////////////////

        context = this;

        ////////////////////////////////////////////////////////////////////////////////////////////

        userEmail    = (EditText) findViewById(R.id.LoginActivity_UserEmail_ET);
        userPassword = (EditText) findViewById(R.id.LoginActivity_UserPassword_ET);

        enterAsAnonym = (CheckBox) findViewById(R.id.LoginActivity_EnterAsAnonym_CHB);

        authMsgTV = (TextView) findViewById(R.id.LoginActivity_AuthMsg_TV);

        // enterButtonLL = (LinearLayout) findViewById(R.id.LoginActivity_ButtonWrap_LL);
        enterButtonLL = (LinearLayout) findViewById(R.id.LoginActivity_ButtonData_LL);
        enterButtonLL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String userEmailStr = userEmail.getText().toString();
                String userPasswordStr = userPassword.getText().toString();

                // очищаем поле от прежних сообщений
                setAuthMsgText(0);

                // если не выбран анонимный вход
                if(!enterAsAnonym.isChecked()) {

                    // если хоть одно поле осталось не заполненным, то стоп
                    if ((userEmailStr.length() == 0) || (userPasswordStr.length() == 0)) {

                        // Log.d(MyApp.LOG_TAG, "Login_Activity: не заполнено одно из полей!");

                        // выводим сообщение об ошибке авторизации
                        setAuthMsgText(MyApp.AUTH_ERROR);
                        return;
                    }
                }
                else {

                    userEmailStr    = MyApp.ANONYM_EMAIL;
                    userPasswordStr = MyApp.ANONYM_PASS;
                }

                JSONObject sendAuthJSONObj = new JSONObject();

                try {

                    JSONObject authJSONObj = new JSONObject();
                    authJSONObj.put("email",    userEmailStr);
                    authJSONObj.put("pass",     userPasswordStr);
//                    authJSONObj.put("auth_hash","");
//                    authJSONObj.put("is_active","");

                    ///////////////////////////////////////////////////////////

                    JSONObject userJSONObj = new JSONObject();
                    userJSONObj.put("AUTH", authJSONObj);

                    ///////////////////////////////////////////////////////////

                    JSONArray playersJSONArr = new JSONArray();
                    playersJSONArr.put(userJSONObj);

                    ///////////////////////////////////////////////////////////

                    sendAuthJSONObj.put("PLAYERS", playersJSONArr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Log.d(MyApp.LOG_TAG, "Login_Activity: connected: " +(MyApp.hasConnection(context)));

                // если есть доступ к интернету
                if(MyApp.hasConnection(context))
                    // отправляем запрос авторизации
                    sendAuthData(sendAuthJSONObj);
                // если нет доступа к интернету
                else
                    // выводим сообщение, что нет подключения к Интернету
                    setAuthMsgText(MyApp.NO_CONNECTION);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////

        userData.put("user_id",     "");
        userData.put("user_email",  "");
        userData.put("user_type",   "admin");
        userData.put("user_live",   "");
        userData.put("user_exp",    "");
        userData.put("user_level",  "");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // очищаем поле от прежних сообщений
        setAuthMsgText(0);
    }

    private void sendAuthData(JSONObject jsonObject) {

        RequestQueue queue = Volley.newRequestQueue(context);

        // Log.d(MyApp.LOG_TAG, "Login_Activity: sendAuthData(): authUrl= " +MyApp.getServerUrl());
        // Log.d(MyApp.LOG_TAG, "Login_Activity: sendAuthData(): JSONObject to send= " + jsonObject.toString());

        ////////////////////////////////////////////////////////////////////////////////////////////

        MyApp.addLogToMap("request",jsonObject.toString());

        ////////////////////////////////////////////////////////////////////////////////////////////

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("authJSONObj", jsonObject.toString());

        ////////////////////////////////////////////////////////////////////////////////////////////

        CustomJSONObjRequest request = new CustomJSONObjRequest(Request.Method.POST, MyApp.getServerUrl(), requestBody,

            new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    // Log.d(MyApp.LOG_TAG, "--> Login_Activity: sendAuthData(): onResponse: response is null: " +(response == null));

                    StringBuilder sbd = new StringBuilder("");

                    if(response != null) {

                        // Log.d(MyApp.LOG_TAG, "Login_Activity: sendAuthData(): onResponse: response= " + response.toString());

                        sbd.append(response.toString());

                        readUserDataFromJSON(response);
                    }
                    else {

                        // Log.d(MyApp.LOG_TAG, "Login_Activity: response is null");

                        // выводим сообщение об ошибке сервера
                        setAuthMsgText(MyApp.SERVER_ERROR);
                    }

                    ////////////////////////////////////////////////////////////////////////////////////////////

                    MyApp.addLogToMap("response",sbd.toString());
                }
            },
            new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    // Log.d(MyApp.LOG_TAG, "Login_Activity: sendAuthData(): onErrorResponse: error= " + error.toString());

                    StringBuilder sbd = new StringBuilder("");

                    if(error != null)
                        sbd.append(error.toString());

                    // если выбран анонимный вход
                    if(enterAsAnonym.isChecked()) {

                        setAnonymUserData();

                        init(userData);

                        moveForward();
                    }

                    // Log.d(MyApp.LOG_TAG, "Login_Activity: response error");

                    // выводим сообщение об ошибке сервера
                    setAuthMsgText(MyApp.SERVER_ERROR);

                    ////////////////////////////////////////////////////////////////////////////////////////////

                    MyApp.addLogToMap("response",sbd.toString());
                }
            });

        queue.add(request);
    }

    private void readUserDataFromJSON(JSONObject jsonObj) {

        // Log.d(MyApp.LOG_TAG, "Login_Activity: readUserDataFromJSON()");

        String userId       = "";

        boolean authError   = false;
        // boolean isActive    = true;

        // Log.d(MyApp.LOG_TAG, "Login_Activity: readUserDataFromJSON(): jsonObj.has(\"PLAYERS\"): " +jsonObj.has("PLAYERS"));

        // получаем из jsonObj "PLAYERS"
        JSONObject playersJSONObj = getJSONObj(jsonObj,"PLAYERS");

        // если playersJSONObj не получен
        if(playersJSONObj == null) {

            // Log.d(MyApp.LOG_TAG, "Login_Activity: playersJSONObj не получен");

            // выводим сообщение об ошибке сервера
            setAuthMsgText(MyApp.AUTH_ERROR);

            // стоп
            return;
        }

        ////////////////////////////////////////////////////////////////////////////////////////

        // получаем идентификатор пользователя
        userId = getUserId(playersJSONObj);

        // если идентификатор пользователя не получен либо в нем пустое/нулевое значение
        if((userId == null) || (userId.equals("")) || (userId.equals("0"))) {

            // Log.d(MyApp.LOG_TAG, "Login_Activity: идентификатор пользователя не получен либо в нем пустое/нулевое значение");

            // выводим сообщение об ошибке сервера
            setAuthMsgText(MyApp.AUTH_ERROR);

            // стоп
            return;
        }

        // сохраняем полученное значение
        userData.put("user_id", userId);

        // получаем блок данных пользователя
        JSONObject userJSONObj = getJSONObj(playersJSONObj,userId);

        // если userJSONObj не получен
        if(userJSONObj == null) {

            // Log.d(MyApp.LOG_TAG, "Login_Activity: userJSONObj не получен");

            // выводим сообщение об ошибке сервера
            setAuthMsgText(MyApp.AUTH_ERROR);

            // стоп
            return;
        }

        ////////////////////////////////////////////////////////////////////////////////////////

        JSONObject authJSONObj = getJSONObj(userJSONObj,"AUTH");

        // если authJSONObj не получен
        if(authJSONObj == null) {

            // Log.d(MyApp.LOG_TAG, "Login_Activity: authJSONObj не получен");

            // выводим сообщение об ошибке сервера
            setAuthMsgText(MyApp.AUTH_ERROR);

            // стоп
            return;
        }

        // получаем email
        String email = getStringFromJSONObj(authJSONObj,"email");

        // если email получен
        if(email != null)
            // сохраняем его в коллекцию
            userData.put("user_email", email);

        // получаем значение активен/выключен аккаунт
        String userIsActive = getStringFromJSONObj(authJSONObj,"is_active");

        // если userIsActive не получен либо получен, но содержит значение отличное от 1
        if((userIsActive == null) || (!userIsActive.equals("1"))) {

            // Log.d(MyApp.LOG_TAG, "Login_Activity: userIsActive не получен либо получен, но содержит значение отличное от 1");

            // выводим сообщение о том, что аккаунт выключен
            setAuthMsgText(MyApp.ACCOUNT_DISABLED);

            // стоп
            return;
        }

        ////////////////////////////////////////////////////////////////////////////////////////

        // получаем ответ возникла ли ошибка при авторизации
        String error = getStringFromJSONObj(userJSONObj, "ERROR");

        // если значение получено и в нем значение = 1
        if((error != null) && (error.equals("1")))
            // меняем значение флага (ошибка авторизации)
            authError = true;

        ////////////////////////////////////////////////////////////////////////////////////////

        JSONObject statusJSONObj = getJSONObj(userJSONObj,"STATUS");

        if (statusJSONObj != null) {

            // получаем уровень жизни игрока
            String live = getStringFromJSONObj(statusJSONObj,"live");

            // если live получен
            if(live != null)
                // сохраняем его в коллекцию
                userData.put("user_live", live);

            ///////////////////////////////////////////////

            // получаем опыт игрока
            String exp = getStringFromJSONObj(statusJSONObj,"exp");

            // если exp получен
            if(exp != null)
                // сохраняем его в коллекцию
                userData.put("user_exp", exp);

            ///////////////////////////////////////////////

            // получаем уровень игрока
            String level = getStringFromJSONObj(statusJSONObj,"level");

            // если level получен
            if(level != null)
                // сохраняем его в коллекцию
                userData.put("user_level", level);
        }

        ////////////////////////////////////////////////////////////////////////////////////////

        JSONObject allWeaponsJSONObj = getJSONObj(userJSONObj,"WEAPON");

        // если блок со всем оружием получен
        if (allWeaponsJSONObj != null) {

            // чистим список перед использованием (возможно повторным, после выхода из приложения)
            userWeaponList.clear();

            // проходим итератором по блоку данных
            Iterator<String> weaponIterator = allWeaponsJSONObj.keys();

            // если данные еще есть, получаем следующий элемент
            while (weaponIterator.hasNext()) {

                // получаем идентификатор оружия
                String weaponId = weaponIterator.next();

                // Log.d(MyApp.LOG_TAG, "Login_Activity: readUserDataFromJSON(): weaponId is null: " +(weaponId == null));

                // если идентификатор задан
                if((weaponId != null) && (!weaponId.equals("")) && (!weaponId.equals("0"))) {

                    JSONObject weaponJSONObj = getJSONObj(allWeaponsJSONObj,weaponId);

                    // если очередное оружие получено
                    if (weaponJSONObj != null) {

                        // создаем объект "MyWeapon" для наполнения данными
                        MyWeapon myWeapon = new MyWeapon();
                        myWeapon.setId(weaponId);

                        // получаем тип оружия
                        String weaponType = getStringFromJSONObj(weaponJSONObj,"type");

                        // если тип оружия получен
                        if(weaponType != null)
                            // сохраняем его значение
                            myWeapon.setType(weaponType);

                        //////////////////////////////////////////////////////////

                        // получаем урон от оружия
                        String weaponDamage = getStringFromJSONObj(weaponJSONObj,"damage");

                        // если урон от оружия получен
                        if(weaponDamage != null)
                            // сохраняем его значение
                            myWeapon.setDamage(weaponDamage);

                        //////////////////////////////////////////////////////////

                        // задаем по-умолчанию, что оружие не выбрано
                        boolean isSelectedBoolean = false;

                        // получаем значени, выбрано ли оружие
                        String isSelectedStr = getStringFromJSONObj(weaponJSONObj,"is_selected");

                        // если значение получено
                        if((isSelectedStr != null) && (isSelectedStr.equals("1")))
                            // меняем значение по-умолчанию
                            isSelectedBoolean = true;

                        myWeapon.setIsSelected(isSelectedBoolean);

                        //////////////////////////////////////////////////////////

                        // получаем кол-во снарядов в орудии
                        String weaponQuantity = getStringFromJSONObj(weaponJSONObj,"quantity");

                        // если кол-во снарядов в орудии получено
                        if(weaponQuantity != null)
                            // сохраняем значение
                            myWeapon.setQuantity(weaponQuantity);

                        //////////////////////////////////////////////////////////

                        // получаем скорость полета снаряда выпущенного из данного орудия
                        String weaponSpeedFly = getStringFromJSONObj(weaponJSONObj,"speed_fly");

                        // если скорость полета снаряда выпущенного из данного орудия получена
                        if(weaponSpeedFly != null)
                            // сохраняем значение
                            myWeapon.setSpeedFly(weaponSpeedFly);

                        //////////////////////////////////////////////////////////

                        // получаем прочность орудия
                        String weaponStrength = getStringFromJSONObj(weaponJSONObj,"strength");

                        // если прочность орудия получена
                        if(weaponStrength != null)
                            // сохраняем значение
                            myWeapon.setStrength(weaponStrength);

                        //////////////////////////////////////////////////////////

                        // добавляем очередное оружие в список
                        userWeaponList.add(myWeapon);

                        // если это оружие выбрано
                        if(isSelectedBoolean)
                            // запоминаем его позицию в списке оружия пользователя
                            userData.put("selected_weapon_list_pos", "" +(userWeaponList.size() - 1));
                    }
                }
            }
        }

        // Log.d(MyApp.LOG_TAG, "Login_Activity: readUserDataFromJSON(): authError: " +authError);

        // если ошибка авторизации не возникла
        if(!authError) {

            // если выбран анонимный вход
            if(enterAsAnonym.isChecked())
                setAnonymUserData();

            init(userData);

            moveForward();
        }
        else {

            // Log.d(MyApp.LOG_TAG, "Login_Activity: возникла ошибка авторизации");

            // выводим сообщение об ошибке авторизации
            setAuthMsgText(MyApp.AUTH_ERROR);
        }
    }

    private void setAnonymUserData() {

        userData.put("user_id",     "0");
        userData.put("user_email",  MyApp.ANONYM_EMAIL);
        userData.put("user_type",   "user");
    }

    // private void init(String userId, String userEmail, String userType) {
    private void init(Map<String, String> userData) {

        // Log.d(MyApp.LOG_TAG, "Login_Activity: init()");

        MyUser user = new MyUser();
        // user.setId(userId);
        user.setId(userData.get("user_id"));
        // user.setEmail(userEmail);
        user.setEmail(userData.get("user_email"));
        user.setFullLive(userData.get("user_live"));
        user.setCurrLive(userData.get("user_live"));
        user.setExperience(userData.get("user_exp"));
        user.setLevel(userData.get("user_level"));

        user.setMyWeaponList(userWeaponList);

        // получаем оружие, которое выбрано для стрельбы
        MyWeapon selectedWeapon = userWeaponList.get(Integer.parseInt(userData.get("selected_weapon_list_pos")));

        // user.setMySelectedWeapon(userData.get("user_mySelected_weapon_list_pos"));
        user.setMySelectedWeapon(selectedWeapon);

        ////////////////////////////////////////////////////////////////////////////////////////////

        // MyApp.clearBombHistoryMap();
        MyApp.clearMyBombHistoryMap();
        MyApp.clearEnemyBombHistoryMap();

        MyApp.setUser(user);
        MyApp.setBattleState(MyApp.NO_BATTLE);
        MyApp.setMapLongClickEnabled(true);

        // Log.d(MyApp.LOG_TAG, "--> Login_Activity: init(): userType.equals(\"admin\"):" +(userType.equals("admin")));

        String userType = userData.get("user_type");

        if(userType.equals("admin"))
            MyApp.setUserType(MyApp.IS_ADMIN);
        else
            MyApp.setUserType(MyApp.IS_USER);

        // myOpponentsMap = user.getMyOpponentsMap();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private String getUserId(JSONObject playersJSONObj) {

        String result = null;

        Iterator<String> keysIterator = playersJSONObj.keys();

        // получаем идентификатор пользователя
        while(keysIterator.hasNext()) {

            result = keysIterator.next();
        }

        return result;
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

    //////////////////////////////////////////////////////////////////////////////////////////////

    // private void setErrorText(int errorType) {
    private void setAuthMsgText(int msgType) {

        // Log.d(MyApp.LOG_TAG, "Login_Activity: setErrorText(): msgType= " +msgType);

        switch(msgType) {

            case MyApp.AUTH_SUCCESS:    // авторизации успешна
                                        authMsgTV.setTextColor(getResources().getColor(R.color.green));
                                        authMsgTV.setText(getResources().getString(R.string.auth_success_text));
                                        break;
            case MyApp.AUTH_ERROR:      // ошибка авторизации
                                        authMsgTV.setTextColor(getResources().getColor(R.color.red));
                                        authMsgTV.setText(getResources().getString(R.string.auth_error_text));
                                        break;
            case MyApp.SERVER_ERROR:    // ошибка сервера
                                        authMsgTV.setTextColor(getResources().getColor(R.color.red));
                                        authMsgTV.setText(getResources().getString(R.string.server_error_text));
                                        break;
            case MyApp.ACCOUNT_DISABLED:// аккаунт выключен
                                        authMsgTV.setTextColor(getResources().getColor(R.color.red));
                                        authMsgTV.setText(getResources().getString(R.string.account_disabled_text));
                                        break;

            case MyApp.NO_CONNECTION:   // нет подключения к интернету
                                        authMsgTV.setTextColor(getResources().getColor(R.color.red));
                                        authMsgTV.setText(getResources().getString(R.string.no_connection_text));
                                        break;
            default:                    // очищаем поле
                                        authMsgTV.setText("");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    private void moveForward() {

        // Log.d(MyApp.LOG_TAG, "Login_Activity: moveForward()");

        // сообщаем, что авторизация успешна
        setAuthMsgText(MyApp.AUTH_SUCCESS);

        // Intent intent = new Intent(Login_Activity.this, Main_Activity.class);
        Intent intent = new Intent(Login_Activity.this, Second_Activity.class);
        startActivity(intent);
    }
}