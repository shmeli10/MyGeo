package com.example.os1.mygeo.View;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.os1.mygeo.Model.MyApp;
import com.example.os1.mygeo.Controller.OnBattleStopListener;
import com.example.os1.mygeo.Controller.OnNewLogListener;
import com.example.os1.mygeo.Controller.OnNoPointsListener;
import com.example.os1.mygeo.Controller.OnSystemErrorListener;
import com.example.os1.mygeo.Controller.SendPointsService;
import com.example.os1.mygeo.Controller.SendShootService;
import com.example.os1.mygeo.Model.MyApp;
import com.example.os1.mygeo.Model.MyBomb;
import com.example.os1.mygeo.Model.MyOpponent;
import com.example.os1.mygeo.Model.MyUser;
import com.example.os1.mygeo.Model.MyWeapon;
import com.example.os1.mygeo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by OS1 on 11.11.2016.
 */
public class Second_Activity    extends     AppCompatActivity
                                implements  View.OnClickListener,
                                            OnMapReadyCallback,
                                            Animation.AnimationListener,
                                            SoundPool.OnLoadCompleteListener,
                                            OnBattleStopListener,
                                            OnNewLogListener,
                                            OnSystemErrorListener,
                                            OnNoPointsListener,
                                            SendShootService.OnShootResultListener,
                                            SendPointsService.OnShowOpponentsListener,
                                            SendPointsService.OnEnemyStartBattleListener {

    private Context             context;
    private SharedPreferences   shPref;

    private MyUser  appUser;

    // интерфейс экрана
    private TextView        lifeTV;
    private TextView        experienceTV;
    private TextView        shotNumTV;
    private EditText        sendUrlET;
    private TabHost         tabHost;
    private LinearLayout    showMenuLL;
    private LinearLayout    showMeOnMapLL;
    private ProgressDialog  progressDialog;

    // карта
    private GoogleMap       googleMap;
    private UiSettings      UISettings;
    private Location        myLocation;
    private MyBomb          myBomb;
    private Polyline        polyline;
    private Marker          bombMarker;
    private Circle          bombRangeCircle;
    private MarkerOptions   bombMarkerOptions;
    private CircleOptions   bombRangeCircleOptions;
    private boolean         mapInit        = false;
    private boolean         autoCenterMap  = false;
    private boolean         bombMarkerShow = false;
    private boolean         markerDraggable= false;

    // координаты
    private LocationManager     mLocationManager;
    private LocationListener    mLocationListener;
    private String              provider;

    // логи
    private SimpleAdapter logsListAdapter;
    private boolean tabHostIsClean = true;
    private ArrayList<Map<String, Object>>  logListData;

    // анимация
    private Animation    mShowPanelAnimation;
    private Animation    mHidePanelAnimation;
    private LinearLayout mPanelLL;

    // меню
    private LinearLayout menuLL;
    private LinearLayout menuArrowsLL;
    private LinearLayout subMenuLL;
    private LinearLayout closeMenuLL;
    private LinearLayout.LayoutParams menu_WW_LP;
    private LinearLayout.LayoutParams menuArrowLL_WW_LP;
    private LinearLayout.LayoutParams menuArrowTV_WW_LP;
    private LinearLayout.LayoutParams menu_MM_LP;
    private List<String> menuItemsList = new ArrayList<>();

    // вибрация
    private Vibrator vibrator;
    private long[] vibratePattern = {0, MyApp.ONE_SECOND, 0};

    // звук
    private SoundPool mSoundPool;
    private int shootSound;
    private boolean shootSoundIsLoaded;

    // сервис отправки координат
    private Intent sendPointsServiceIntent;
    private SendPointsService sendPointsService;
    private ServiceConnection sendPointsServiceConnection;
    private Snackbar reloadSnackBar;
    private boolean sendPointsServiceIsBound = false;

    // сервис отправки выстрела
    private boolean shootServiceStarted;
    private boolean bombIsFlying = false;

    private Map<String, MyOpponent> myOpponentsMap;
    private Map<String, JSONObject> myShootsMap;

    private Map<String, MyBomb> myBombHistoryMap;
    private Map<String, MyBomb> enemyBombHistoryMap;

    private List<MyOpponent> enemiesList = new ArrayList<>();
    private List<LatLng> routePointsList = new ArrayList<>();

    private MyWeapon mySelectedWeapon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onCreate()");

        setContentView(R.layout.activity_second);

        ////////////////////////////////////////////////////////////////////////////////////////////

        context = this;

        ////////////////////////////////////////////////////////////////////////////////////////////

        shPref = getSharedPreferences("user_data", MODE_PRIVATE);

        loadTextFromPreferences();

        ////////////////////////////////////////////////////////////////////////////////

        appUser = MyApp.getUser();

        MyApp.setMainActivityLink(this);

        myOpponentsMap      = appUser.getMyOpponentsMap();
        myShootsMap         = appUser.getMyShootsMap();

        myBombHistoryMap    = MyApp.getMyBombHistoryMap();
        enemyBombHistoryMap = MyApp.getEnemyBombHistoryMap();

        mySelectedWeapon    = appUser.getSelectedWeapon();

        ////////////////////////////////////////////////////////////////////////////////////////////

        lifeTV          = (TextView) findViewById(R.id.SecondActivity_LifeTV);
        experienceTV    = (TextView) findViewById(R.id.SecondActivity_ExperienceTV);
        shotNumTV       = (TextView) findViewById(R.id.SecondActivity_ShotTV);

        setLifeValue();

        setExperienceValue();

        setShotNumValue();

        ////////////////////////////////////////////////////////////////////////////////////////////

        showMeOnMapLL = (LinearLayout) findViewById(R.id.SecondActivity_ShowMe_LL);
        showMeOnMapLL.setOnClickListener(this);

        ////////////////////////////////////////////////////////////////////////////////////////////

        mLocationManager  = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = getLocationListener();

        ////////////////////////////////////////////////////////////////////////////////

        mShowPanelAnimation = AnimationUtils.loadAnimation(this, R.anim.show_panel);
        mShowPanelAnimation.setAnimationListener(this);

        mHidePanelAnimation = AnimationUtils.loadAnimation(this, R.anim.hide_panel);
        mHidePanelAnimation.setAnimationListener(this);

        // панель
        mPanelLL = (LinearLayout) findViewById(R.id.Panel_LL);
        mPanelLL.setVisibility(View.INVISIBLE); // сразу скрываем

        // меню
        menuLL       = (LinearLayout) findViewById(R.id.Panel_Menu_LL);

        // стрелки показывающиеся при выборе одного из пунктов меню
        menuArrowsLL = (LinearLayout) findViewById(R.id.Panel_MenuArrows_LL);

        // подменю
        subMenuLL    = (LinearLayout) findViewById(R.id.Panel_SubMenu_LL);

        ////////////////////////////////////////////////////////////////////////////////////////////

        showMenuLL = (LinearLayout) findViewById(R.id.SecondActivity_ShowMenu_LL);
        showMenuLL.setBackgroundResource(R.drawable.menu_selector);
        showMenuLL.setOnClickListener(this);

        // закрытие меню
        closeMenuLL = (LinearLayout) findViewById(R.id.Panel_CloseMenu_LL);
        closeMenuLL.setBackgroundResource(R.drawable.menu_selector);
        closeMenuLL.setOnClickListener(this);

        // пункты меню
        menuItemsList.add("Броня");
        menuItemsList.add("Оружие");
        menuItemsList.add("Настройки");
        menuItemsList.add("Выход");

        setMenu();

        ////////////////////////////////////////////////////////////////////////////////////////////

        sendPointsServiceIntent = new Intent(this, SendPointsService.class);

        sendPointsServiceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {

                // Log.d(MyApp.LOG_TAG, "Main_Activity: onCreate(): onSendPointsServiceConnected");

                sendPointsService = ((SendPointsService.MyBinder) binder).getService();
                sendPointsServiceIsBound = true;

                sendPointsService.schedule();
            }

            public void onServiceDisconnected(ComponentName name) {

                // Log.d(MyApp.LOG_TAG, "MainActivity: onCreate(): onSendPointsServiceDisconnected");

                sendPointsServiceIsBound = false;
            }
        };

        bindService(sendPointsServiceIntent, sendPointsServiceConnection, 0);

        ////////////////////////////////////////////////////////////////////////////////////////////

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        ////////////////////////////////////////////////////////////////////////////////////////////

        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener(this);

        shootSound = mSoundPool.load(this, R.raw.shoot, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onResume()");

        MyApp.setGpsEnabled(true);
        MyApp.setInternetEnabled(true);

        setTabs();

        // если это вкладка "Карта"
        if (tabHost.getCurrentTab() == 0) {

            // если GPS выключен
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                // предложить пользователю его включить
                showEnableGPSDialog();

            // включить определение местоположения
            startSendPoints();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onStop()");

        stopSendPointsService();
        stopSendShootService();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onPause()");

        removeLocUpdates();

        ////////////////////////////////////////////////////////////////////////////////////////////

        if (MyApp.getBattleState() == MyApp.BATTLE) {

            // Log.d(MyApp.LOG_TAG, "Second_Activity: onPause(): battle is running. stop send shoot");

            stopSendShootService();
        }
    }

    /**
     * обработка внезапного закрытия окна или приложения
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Log.d(MyApp.LOG_TAG, "Main_Activity: onDestroy()");

        stopSendPointsService();
        unBindSendPointsService();

        // если сервис отправки выстрела запущен
        if (shootServiceStarted)
            // остановить его
            stopSendShootService();

        stopVibrate();
    }

    @Override
    public void onClick(View view) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onClick()");

        switch (view.getId()) {

            case R.id.MapSettings_SendUrl_ET:       // сохраняем новый url сервера
                                                    String currUrlStr = MyApp.getServerUrl();
                                                    String newUrlStr = sendUrlET.getText().toString();

                                                    if ((newUrlStr != null) && (newUrlStr.length() > 0)) {

                                                        if (!newUrlStr.equals(currUrlStr)) {

                                                            MyApp.setServerUrl(newUrlStr);

                                                            saveTextInPreferences("server_url", newUrlStr);

                                                            showAlertDialog(R.string.new_send_url_saved_title_text, R.string.new_send_url_saved_message_text);
                                                        } else
                                                            showAlertDialog(R.string.new_send_url_saved_title_text, R.string.not_new_send_url_message_text);
                                                    } else
                                                        showAlertDialog(R.string.new_send_url_saved_title_text, R.string.new_send_url_empty_message_text);

                                                    break;
            case R.id.SecondActivity_ShowMe_LL:     // центрируем карту на маркере пользователя
                                                    autoCenterMap = true;

                                                    setMarker();
                                                    break;

            case R.id.SecondActivity_ShowMenu_LL:   // показываем панель
                                                    mPanelLL.startAnimation(mShowPanelAnimation);
                                                    break;

            case R.id.Panel_CloseMenu_LL:           // скрываем панель
                                                    mPanelLL.startAnimation(mHidePanelAnimation);
                                                    break;
        }
    }

    /////////////////////////////////////////// SET TABS //////////////////////////////////////////

    private void setTabs() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setTabs()");

        final String TABS_TAG_1 = getResources().getString(R.string.map_text);
        final String TABS_TAG_2 = getResources().getString(R.string.settings_text);
        final String TABS_TAG_3 = getResources().getString(R.string.logs_text);

        tabHost = (TabHost) findViewById(android.R.id.tabhost);

        if (tabHost != null) {

            tabHost.setup();

            TabHost.TabSpec tabSpec;

            TabHost.TabContentFactory TabFactory = new TabHost.TabContentFactory() {

                @Override
                public View createTabContent(String tag) {

                    // выбрана вкладка "Карта"
                    if (tag == TABS_TAG_1) {

                        LinearLayout mapLL = (LinearLayout) getLayoutInflater().inflate(R.layout.tab_map, null);

                        return mapLL;
                    }
                    // выбрана вкладка "Настройки"
                    else if (tag == TABS_TAG_2) {

                        LinearLayout settingsLL = (LinearLayout) getLayoutInflater().inflate(R.layout.tab_settings, null);

                        sendUrlET = (EditText) settingsLL.findViewById(R.id.MapSettings_SendUrl_ET);
                        sendUrlET.setOnClickListener(Second_Activity.this);

                        /////////////////////////////////////////////////////////////////////////////////////////////////////////

                        removeLocUpdates();

                        /////////////////////////////////////////////////////////////////////////////////////////////////////////

                        CheckBox gpsCHB = (CheckBox) settingsLL.findViewById(R.id.MapSettings_GPS_CHB);

                        gpsCHB.setChecked(MyApp.isGPSEnabled());

                        gpsCHB.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                MyApp.setGpsEnabled(!MyApp.isGPSEnabled());

                                // Log.d(MyApp.LOG_TAG, "Main_Activity: GPS checkbox click, enabled: " + (MyApp.isGPSEnabled()));
                            }
                        });

                        /////////////////////////////////////////////////////////////////////////////////////////////////////////

                        CheckBox internetCHB = (CheckBox) settingsLL.findViewById(R.id.MapSettings_Internet_CHB);

                        internetCHB.setChecked(MyApp.isInternetEnabled());

                        internetCHB.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                MyApp.setInternetEnabled(!MyApp.isInternetEnabled());

                                // Log.d(MyApp.LOG_TAG, "Main_Activity: Internet checkbox click, enabled: " + (MyApp.isInternetEnabled()));
                            }
                        });

                        /////////////////////////////////////////////////////////////////////////////////////////////////////////

                        CheckBox gyroscopeCHB = (CheckBox) settingsLL.findViewById(R.id.MapSettings_Gyroscope_CHB);

                        gyroscopeCHB.setChecked(MyApp.isGyroscopeEnabled());

                        gyroscopeCHB.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                MyApp.setGyroscopeEnabled(!MyApp.isGyroscopeEnabled());

                                // Log.d(MyApp.LOG_TAG, "Main_Activity: Hiroskope checkbox click, enabled: " + (MyApp.isGyroscopeEnabled()));
                            }
                        });

                        /////////////////////////////////////////////////////////////////////////////////////////////////////////

                        // Log.d(MyApp.LOG_TAG, "Second_Activity: userType= " +MyApp.getUserType());

                        if (MyApp.getUserType() == MyApp.IS_ADMIN) {

                            RelativeLayout sendUrl_RL = (RelativeLayout) settingsLL.findViewById(R.id.MapSettings_SendUrl_RL);
                            sendUrl_RL.setVisibility(View.VISIBLE);

                            EditText sendUrl_ET = (EditText) settingsLL.findViewById(R.id.MapSettings_SendUrl_ET);
                            sendUrl_ET.setText(MyApp.getServerUrl());

                            Button saveButton = (Button) settingsLL.findViewById(R.id.MapSettings_Save_BTN);
                            saveButton.setOnClickListener(Second_Activity.this);
                        }

                        return settingsLL;

                    } else if (tag == TABS_TAG_3) {

                        LinearLayout logsLL = (LinearLayout) getLayoutInflater().inflate(R.layout.tab_logs, null);

                        // упаковываем данные в понятную для адаптера структуру
                        logListData = new ArrayList<Map<String, Object>>();

                        // массив имен атрибутов, из которых будут читаться данные
                        String[] from = {MyApp.ATTRIBUTE_NAME_TEXT, MyApp.ATTRIBUTE_NAME_IMAGE};

                        // массив ID View-компонентов, в которые будут вставлять данные
                        int[] to = {R.id.LogsRow_TV, R.id.LogsRow_IV};

                        // создаем адаптер
                        logsListAdapter = new SimpleAdapter(context, logListData, R.layout.list_logs_row, from, to);

                        // определяем список и присваиваем ему адаптер
                        ListView logsListView = (ListView) logsLL.findViewById(R.id.Logs_Data_LV);
                        logsListView.setAdapter(logsListAdapter);

                        ////////////////////////////////////////////////////////////////////////////////

                        setLogsListItems();

                        ////////////////////////////////////////////////////////////////////////////////

                        return logsLL;
                    }

                    return null;
                }
            };

            /////////////////////////////////////////////////////////////////

            View tabView = null;
            TabHost.TabSpec setContent = null;

            if (tabHostIsClean) {

                // Вкладка "Карта"
                tabView = createTabView(tabHost.getContext(), TABS_TAG_1);

                setContent = tabHost.newTabSpec(TABS_TAG_1).setIndicator(tabView).setContent(TabFactory);

                tabHost.addTab(setContent);

                /////////////////////////////////////////////////////////////////

                // Вкладка "Настройки"
                tabView = createTabView(tabHost.getContext(), TABS_TAG_2);

                setContent = tabHost.newTabSpec(TABS_TAG_2).setIndicator(tabView).setContent(TabFactory);

                tabHost.addTab(setContent);

                /////////////////////////////////////////////////////////////////

                // Вкладка "Логи"
                tabView = createTabView(tabHost.getContext(), TABS_TAG_3);

                setContent = tabHost.newTabSpec(TABS_TAG_3).setIndicator(tabView).setContent(TabFactory);

                tabHost.addTab(setContent);

                /////////////////////////////////////////////////////////////////

                tabHostIsClean = false;

                /////////////////////////////////////////////////////////////////

                tabHost.setCurrentTab(0);

                /////////////////////////////////////////////////////////////////

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.TabMap_MapFragment);
                mapFragment.getMapAsync(this);
            }
        }
    }

    private static View createTabView(final Context context, final String text) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: createTabView()");

        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);

        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);

        return view;
    }

    ////////////////////////////////////// LOGS //////////////////////////////////////////////////

    @Override
    public void addLog(String dataType, String dataValue, long interval) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: addLog()");

        // если задан обычный интервал отправки координат на сервер
        if (interval != MyApp.SEND_POINTS_SHORT_INTERVAL)
            // добавляем лог в коллекцию
            MyApp.addLogToMap(dataType, dataValue);
    }

    private void setLogsListItems() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setLogsListItems()");

        List<String> logsMapKeysList = MyApp.getLogsMapKeysList();

        if (logsMapKeysList != null) {

            Map<String, String[]> logsMap = MyApp.getLogsMap();

            if (logsMap != null) {

                logListData.clear();

                for (int i = 0; i < logsMapKeysList.size(); i++) {

                    String[] dataArr = logsMap.get(logsMapKeysList.get(i));

                    Map<String, Object> listRow = new HashMap<String, Object>();

                    if (dataArr[0] != null) {

                        if (dataArr[0].equals("request"))
                            listRow.put(MyApp.ATTRIBUTE_NAME_IMAGE, R.drawable.request);
                        else if (dataArr[0].equals("response"))
                            listRow.put(MyApp.ATTRIBUTE_NAME_IMAGE, R.drawable.response);
                    }

                    if (dataArr[1] != null)
                        listRow.put(MyApp.ATTRIBUTE_NAME_TEXT, dataArr[1]);

                    logListData.add(0, listRow);
                }

                logsListAdapter.notifyDataSetChanged();
            }
        }
    }

    ////////////////////////////////// ANIMATION /////////////////////////////////////////////

    @Override
    public void onAnimationStart(Animation animation) {

        if (animation.equals(mShowPanelAnimation)) {
            mPanelLL.setVisibility(View.VISIBLE);
        } else if (animation.equals(mHidePanelAnimation)) {
            mPanelLL.setVisibility(View.GONE);

            // делаем все пункты меню не выбранными
            changeMenuItemSelection(-1);

            // убираем все подпункты ранее выбранного пункта меню
            subMenuLL.removeAllViews();

            // если было задано новое оружие для выбора
            if(appUser.getNewWeaponForSelect() != null) {

                // если новое и прежнее оружие не совпадают
                if(!appUser.getNewWeaponForSelect().getId().equals(appUser.getSelectedWeapon().getId())) {

                    // сообщаем, что выбрано новое оружие
                    appUser.setIsNewWeaponSelected(true);
                }
            }
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    //////////////////////////////// MENU /////////////////////////////////////////////

    private void setMenu() {

        menu_WW_LP  = (LinearLayout.LayoutParams) menuLL.getLayoutParams();

        menu_MM_LP  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        menu_MM_LP.setMargins(5,5,5,5);

        menuArrowLL_WW_LP  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menuArrowLL_WW_LP.gravity = Gravity.CENTER_VERTICAL;
        menuArrowLL_WW_LP.setMargins(5,5,5,5);

        menuArrowTV_WW_LP  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);

        ///////////////////////////////////////////////////////////////////////////

        menuLL.removeAllViews();
        menuArrowsLL.removeAllViews();

        ///////////////////////////////////////////////////////////////////////////

        // наполняем меню
        for(int i=0; i<menuItemsList.size(); i++) {

            final int menuItemPos = i;

            LinearLayout menuItemWrapLL = new LinearLayout(context);
            menuItemWrapLL.setLayoutParams(menu_MM_LP);
            // menuItemWrapLL.setBackgroundResource(R.drawable.menu_item_selector);
            menuItemWrapLL.setGravity(Gravity.CENTER_VERTICAL);

            // текст пункта меню
            TextView menuItemTV = new TextView(context);
            menuItemTV.setLayoutParams(menu_WW_LP);
            menuItemTV.setTextColor(Color.WHITE);
            menuItemTV.setText(menuItemsList.get(menuItemPos));
            menuItemTV.setPadding(5,10,5,10);

            menuItemWrapLL.addView(menuItemTV);

            // если это не пункт меню "Выход"
            if(i < (menuItemsList.size()-1)) {

                menuItemWrapLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        changeMenuItemSelection(menuItemPos);

                        showSubMenu(menuItemPos);
                    }
                });
            }
            // если это пункт меню "Выход"
            else {

                menuItemWrapLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // выходим из приложения
                        appExit();
                    }
                });
            }

            menuLL.addView(menuItemWrapLL);

            ///////////////////////////////////////////////////////////////////////////

            LinearLayout menuArrowWrapLL = new LinearLayout(context);
            menuArrowWrapLL.setLayoutParams(menuArrowLL_WW_LP);
            menuArrowWrapLL.setVisibility(View.INVISIBLE);

            // стрелка вправо
            TextView arrowRightItemTV = new TextView(context);
            arrowRightItemTV.setLayoutParams(menuArrowTV_WW_LP);
            arrowRightItemTV.setPadding(5,10,5,10);
            arrowRightItemTV.setTextColor(Color.WHITE);
            arrowRightItemTV.setText(" > ");

            // добавляем стрелку в контейнер
            menuArrowWrapLL.addView(arrowRightItemTV);

            // добавляем контейнер в меню
            menuArrowsLL.addView(menuArrowWrapLL);
        }
    }

    private void showSubMenu(int menuItemSelected) {

        // Toast.makeText(context, "Выбран пункт: " +menuItemsList.get(menuItemSelected), Toast.LENGTH_SHORT).show();

        subMenuLL.removeAllViews();

        switch(menuItemSelected) {

            case 1:     // Оружие

                        // получаю список оружия
                        List<MyWeapon> myWeaponList = appUser.getMyWeaponList();

                        // наполняю подменю оружием
                        for(int i=0; i<myWeaponList.size(); i++) {

                            // получаю очередное оружие
                            final MyWeapon myWeapon = myWeaponList.get(i);

                            // если оружие получено
                            if(myWeapon != null) {

                                final int subMenuItemPos = i;

                                LinearLayout weaponWrapLL = new LinearLayout(context);
                                weaponWrapLL.setLayoutParams(menu_MM_LP);

                                if(myWeapon.isSelected())
                                    weaponWrapLL.setBackgroundResource(R.drawable.rounded_rect_with_white_stroke);
                                else
                                    weaponWrapLL.setBackgroundResource(R.drawable.rounded_rect_with_brown_stroke);

                                // наименование оружия
                                final TextView weaponTV = new TextView(context);
                                weaponTV.setLayoutParams(menu_WW_LP);
                                weaponTV.setTextColor(Color.WHITE);
                                weaponTV.setText(myWeapon.getType() + "(" +myWeapon.getQuantity()+ ")");
                                weaponTV.setPadding(10,10,10,10);

                                // добавляем наименование оружия в контейнер
                                weaponWrapLL.addView(weaponTV);

                                // создаем обработчик выбора оружия
                                weaponWrapLL.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        // если это оружие мной еще не выбрано
                                        if(!myWeapon.getId().equals(appUser.getSelectedWeapon().getId())) {

//                                            // если снаряды есть
//                                            if (myWeapon.getQuantity() > 0) {

                                                // сообщаем, что будет выбрано другое оружие
                                                appUser.setNewWeaponForSelect(myWeapon);

                                                // выделяем выбранное оружие рамкой
                                                changeSubMenuItemSelection(subMenuItemPos);

//                                            } else
//                                                Toast.makeText(context, getResources().getString(R.string.weapon_select_error_text), Toast.LENGTH_SHORT).show();
                                        }
                                        // если это оружие уже выбрано
//                                        else
//                                            Log.d(MyApp.LOG_TAG, "SecondActivity: showSubMenu(): Это оружие уже выбрано!");
                                    }
                                });

                                // добавляем контейнер в подменю
                                subMenuLL.addView(weaponWrapLL);
                            }
                        }

                        break;
            default:    // любой другой пункт
                        LinearLayout noDataWrapLL = new LinearLayout(context);
                        noDataWrapLL.setLayoutParams(menu_MM_LP);

                        // текст об отсутствии подпунктов в выбранном меню
                        final TextView noDataTV = new TextView(context);
                        noDataTV.setLayoutParams(menu_WW_LP);
                        noDataTV.setTextColor(Color.WHITE);
                        noDataTV.setText(getResources().getString(R.string.no_data_text));
                        noDataTV.setPadding(5,10,5,10);

                        // добавляем текстовое представление в контейнер
                        noDataWrapLL.addView(noDataTV);

                        // добавляем контейнер в подменю
                        subMenuLL.addView(noDataWrapLL);
        }
    }

    private void changeMenuItemSelection(int selectedMenuItem) {

        int menuArrowsSum = menuArrowsLL.getChildCount();

        for(int i=0; i<menuArrowsSum; i++) {

            LinearLayout menuItemArrowLL = (LinearLayout) menuArrowsLL.getChildAt(i);

            if(i != selectedMenuItem)
                menuItemArrowLL.setVisibility(View.INVISIBLE);
            else
                menuItemArrowLL.setVisibility(View.VISIBLE);
        }
    }

    private void changeSubMenuItemSelection(int selectedSubMenuItem) {

        int subMenuItemsSum = subMenuLL.getChildCount();

        if(subMenuItemsSum > 1) {

            for(int i=0; i<subMenuItemsSum; i++) {

                LinearLayout subMenuItem = (LinearLayout) subMenuLL.getChildAt(i);

                // если это не выбранный подпункт меню
                if(i != selectedSubMenuItem)
                    // рамка коричневая
                    subMenuItem.setBackgroundResource(R.drawable.rounded_rect_with_brown_stroke);
                // если это выбранный подпункт меню
                else
                    // рамка белая
                    subMenuItem.setBackgroundResource(R.drawable.rounded_rect_with_white_stroke);
            }
        }
    }

    private void appExit() {

        AlertDialog.Builder acceptDialog = new AlertDialog.Builder(context);

        acceptDialog.setTitle(context.getResources().getString(R.string.exit_text));     // заголовок
        acceptDialog.setMessage(context.getResources().getString(R.string.accept_exit_text));  // сообщение

        acceptDialog.setPositiveButton(context.getResources().getString(R.string.yes_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                Second_Activity.this.finish();
            }
        });

        acceptDialog.setNegativeButton(context.getResources().getString(R.string.no_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });

        acceptDialog.setCancelable(true);

        acceptDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });

        acceptDialog.show();
    }

    ///////////////////////////////////////// MAP /////////////////////////////////////////////////

    @Override
    public void onMapReady(GoogleMap google_map) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onMapReady()");

        googleMap = google_map;

        googleMap.setPadding(10, 10, 10, 50);

        /////////////////////////////////////////////////////////////////

        // инициализация карты
        mapInit = true;

        // автоматически отцентрировать карту на пользователе
        autoCenterMap = true;

        // назначаем слушателя изменения позиции или наезда камеры
        googleMap.setOnCameraChangeListener(getCameraChangeListener());

        /////////////////////////////////////////////////////////////////

        // назначаем карте слушателя длинного нажатия по ней
        googleMap.setOnMapLongClickListener(getMapLongClickListener());

        /////////////////////////////////////////////////////////////////

        // назначаем карте слушателя щелчка по маркеру
        googleMap.setOnMarkerClickListener(getMarkerClickListener());

        /////////////////////////////////////////////////////////////////

        // назначаем слушателя перемещения маркера
        googleMap.setOnMarkerDragListener(getMarkerDragListener());

        /////////////////////////////////////////////////////////////////

        try {

            // задаем новый стиль карты
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.dark_style_json));

        } catch (Resources.NotFoundException e) {
            // Log.e(MyApp.LOG_TAG, "Second_Activity: onMapReady(): Can't find style.", e);
        }

        /////////////////////////////////////////////////////////////////

        // получаем ссылку к пользовательским настройкам карты
        UISettings = this.googleMap.getUiSettings();

        // зум включить
        UISettings.setZoomControlsEnabled(true);

        // вращение карты выключить
        UISettings.setRotateGesturesEnabled(false);
    }

    private void setMarker() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker()");

        if (googleMap != null) {

            googleMap.clear();

            List<String> myPointsList = appUser.getPointsList();

            if (myPointsList != null) {

                // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): show me!");

                // получаем самую раннюю координату, она последняя в массиве
                myLocation = appUser.getMyPointFromMap(myPointsList.get((myPointsList.size() - 1))).getLocation();

                LatLng centerLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                // формируем маркер в заданной точке
                Marker myMarker = googleMap.addMarker(new MarkerOptions().position(centerLatLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).draggable(false));
                myMarker.setTitle("" + appUser.getId());

                ////////////////////////////////////////////////////////////////////////////////////

                // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): routePointsList.isEmpty(): " +(routePointsList.isEmpty()));

                if(bombMarkerShow) {

                    // если маршрут полета снаряда не пустой
                    if (!routePointsList.isEmpty()) {

                        LatLng routePoint = routePointsList.get(0);

                        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): set route point marker: lat= " +routePoint.latitude+ ", lon= " +routePoint.longitude);

                        // отображаем летящий снаряд в виде окружности
//                        CircleOptions circleOptions = new CircleOptions();
//                        circleOptions.strokeWidth(1).strokeColor(Color.BLACK).fillColor(0xc805f431).radius(30).center(routePoint);
//                        googleMap.addCircle(circleOptions);

                        googleMap.addCircle(new CircleOptions().center(routePoint).strokeWidth(1).strokeColor(Color.RED).fillColor(0xc805f431).radius(30));

                        // отображаем маршрут падения полета снаряда
                        polyline = googleMap.addPolyline(new PolylineOptions().add(centerLatLng, routePoint).width(3).color(Color.RED));

                        // удаляем точку маршрута, через которую маркер уже прошел
                        routePointsList.remove(0);
                    }

                    ////////////////////////////////////////////////////////////////////////////////////

                    // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker: myBomb is null: " +(myBomb == null));

                    // если маркер выставлен, но еще не отправлен на сервер
                    if ((myBomb != null) && (!myBombHistoryMap.containsKey(myBomb.getId()))) {

                        // отображаем его
                        bombMarker = googleMap.addMarker(bombMarkerOptions);
                        bombRangeCircle = googleMap.addCircle(bombRangeCircleOptions);
                    }

                    ////////////////////////////////////////////////////////////////////////////////////

                    if ((myBombHistoryMap != null) && (!myBombHistoryMap.isEmpty())) {

                        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): myBombHistoryMap size= " +myBombHistoryMap.size());

                        // изображение для маркера места падения снаряда

                        BitmapDescriptor myDroppedBombIcon = getBombMarkerPicture("my_bomb");

                        for (Map.Entry<String, MyBomb> myBombHistory : myBombHistoryMap.entrySet()) {

                            MyBomb myDroppedBomb = myBombHistory.getValue();

                            LatLng myDroppedBombLatLng = new LatLng(myDroppedBomb.getLatitude(), myDroppedBomb.getLongitude());

                            switch (myDroppedBomb.getShootType()) {

                                case MyApp.SECOND_SHOOT_TYPE:
                                    myDroppedBombIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
                                    bombRangeCircle = googleMap.addCircle(bombRangeCircleOptions);
                                    break;
                                case MyApp.THIRD_SHOOT_TYPE:
                                    myDroppedBombIcon = getBombMarkerPicture("my_bomb");
                                    break;

                            }

                            bombMarker = googleMap.addMarker(new MarkerOptions().position(myDroppedBombLatLng).icon(myDroppedBombIcon).draggable(false));
                            bombMarker.setTitle("" + myBombHistory.getKey());
                        }
                    }

                    if ((enemyBombHistoryMap != null) && (!enemyBombHistoryMap.isEmpty())) {

                        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): enemyBombHistoryMap size= " +enemyBombHistoryMap.size());

                        BitmapDescriptor enemyDroppedBombIcon = getBombMarkerPicture("enemy_bomb");

                        for (Map.Entry<String, MyBomb> enemyBombHistory : enemyBombHistoryMap.entrySet()) {

                            MyBomb enemyDroppedBomb = enemyBombHistory.getValue();

                            LatLng enemyDroppedBombLatLng = new LatLng(enemyDroppedBomb.getLatitude(), enemyDroppedBomb.getLongitude());

                            if (enemyDroppedBombIcon != null) {

                                // формируем маркер в заданной точке
                                Marker enemyDroppedBombMarker = googleMap.addMarker(new MarkerOptions().position(enemyDroppedBombLatLng).icon(enemyDroppedBombIcon).draggable(false));
                                enemyDroppedBombMarker.setTitle("" + enemyBombHistory.getKey());
                            }
                        }
                    }
                }

                ////////////////////////////////////////////////////////////////////////////////////

                if ((myOpponentsMap != null) && (!myOpponentsMap.isEmpty())) {

                    for (Map.Entry<String, MyOpponent> opponent : myOpponentsMap.entrySet()) {

                        MyOpponent myOpponent = opponent.getValue();

                        LatLng myOppMarkerLatLng = new LatLng(myOpponent.getLatitude(), myOpponent.getLongitude());

                        float myOppMarkerColor = BitmapDescriptorFactory.HUE_AZURE;

                        // if (MyApp.isMarkerRed()) {
                        if (MyApp.getBattleState() == MyApp.BATTLE) {

                            myOppMarkerColor = BitmapDescriptorFactory.HUE_RED;

                            // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker: set my opponent marker red color");
                        }

                        ///////////////////////////////////////////////////////////////////////////

                        if (myBomb != null) {

                            LatLng bombPosition = bombMarkerOptions.getPosition();

                            // double distance = (getDistance(myOppMarkerLatLng, centerLatLng) * 1000);
                            double distance = (getDistance(myOppMarkerLatLng, bombPosition) * 1000);

                            // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): distance= " +distance);
                            // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): radius= " +RADIUS);

                            if (MyApp.getBattleState() == MyApp.NO_BATTLE) {

                                if (distance < myBomb.getAttackRange()) {
                                    enemiesList.add(myOpponent);

                                    // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): add enemy: " +myOpponent.getId());
                                }
                            }
                        }

                        // формируем маркер в заданной точке
                        Marker myOppMarker = googleMap.addMarker(new MarkerOptions().position(myOppMarkerLatLng).icon(BitmapDescriptorFactory.defaultMarker(myOppMarkerColor)).draggable(false));
                        myOppMarker.setTitle("" + myOpponent.getId());
                        // myOppMarker.setTitle("" + myOpponent.getId() + "Current Life=" +myOpponent.getCurrLive());
                    }
                }

                CameraPosition newPoint = new CameraPosition.Builder()
                        .target(centerLatLng)
                        .zoom(MyApp.getZoomLevel())
                        .bearing(0)
                        .tilt(0)
                        .build();

                // Log.d(MyApp.LOG_TAG, "Second_Activity: setMarker(): autoCenterMap: " +autoCenterMap);

                if (autoCenterMap) {
                    // центрируем карту на заданной точке
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(newPoint));

                    autoCenterMap = false;
                }
            }
        }
    }

    private BitmapDescriptor getBombMarkerPicture(String resource) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: getBombMarkerPicture()");

        StringBuilder sb = new StringBuilder("@drawable/");
        sb.append(resource);

        int imageId = getResources().getIdentifier(sb.toString(), null, context.getPackageName());

        return BitmapDescriptorFactory.fromResource(imageId);
    }

    private double getDistance(LatLng point1, LatLng point2) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: getDistance()");

        // радиус Земли в км
        int R = 6371;

        // получаем значения
        float dLat = Float.parseFloat("" + getRadValue(point2.latitude - point1.latitude));
        float dLong = Float.parseFloat("" + getRadValue(point2.longitude - point1.longitude));

        // подставляем значения в формулу
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(getRadValue(point1.latitude)) * Math.cos(getRadValue(point2.latitude)) * Math.sin(dLong / 2) * Math.sin(dLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // получаем расстояние от точки до центра круга
        return (R * c);
    }

    private double getRadValue(double value) {
        return value * Math.PI / 180;
    }

    // private void initBombMarker(LatLng targetPoint, int bombMarkerType) {
    private void initBombMarker(LatLng targetPoint) {

        // Log.d(MyApp.LOG_TAG, "Main_Activity: setBombMarker(): myBomb is null: " +(myBomb == null));

        // if (myBomb == null)
        myBomb = new MyBomb();

        myBomb.setId("" + System.currentTimeMillis());
        myBomb.setShootType(MyApp.FIRST_SHOOT_TYPE);
        myBomb.setLatitude(targetPoint.latitude);
        myBomb.setLongitude(targetPoint.longitude);

        markerDraggable = true;

        bombMarkerShow = true;

        ////////////////////////////////////////////////////////////////////////////////////

        bombMarkerOptions = new MarkerOptions().icon(getBombMarkerPicture("target")).draggable(markerDraggable);
        bombMarkerOptions.position(targetPoint);

        ////////////////////////////////////////////////////////////////////////////////////

        if (bombRangeCircleOptions == null)
            // bombRangeCircleOptions = new CircleOptions().strokeWidth(3).strokeColor(Color.RED).fillColor(0x23ff0000);
            bombRangeCircleOptions = new CircleOptions();

        bombRangeCircleOptions.strokeWidth(3).strokeColor(Color.RED).fillColor(0x23ff0000);

        // задаем центр окружности и радиус
        bombRangeCircleOptions.radius(myBomb.getAttackRange());

        bombRangeCircleOptions.center(targetPoint);
    }

    private void forceUpdateMarker() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: forceUpdateMarker(): myBomb is null: " +(myBomb == null));

        // если снаряд не был затерт системой
        if(myBomb != null) {

            // принудительно обновляем его
            MyApp.updateMyBombInBombHistoryMap(myBomb.getId(), myBomb.getLatitude(), myBomb.getLongitude());

            // затираем снаряд, чтобы выстрелить новым
            myBomb = null;
        }

        // обновляем кол-во сделанных выстрелов
        setShotNumValue();

        // затираем окружность
        // bombRangeCircle = null;
        bombRangeCircle.setVisible(false);

        // если окружность задана
        if(bombRangeCircle != null)
            // удаляем ее с карты
            bombRangeCircle.remove();
    }

    private void showFlyingBomb() {

        bombIsFlying = true;

        new CountDownTimer(((appUser.getSelectedWeapon().getSpeedFly() + 1) * MyApp.ONE_SECOND), MyApp.ONE_SECOND) {

            // перерисовываем карту каждую секунду
            public void onTick(long millisUntilFinished) {

                setMarker();
            }

            // Задаем действия после завершения отсчета (высвечиваем надпись "Бабах!"):
            public void onFinish() {

                bombIsFlying = false;

                if(polyline != null)
                    polyline.remove();

                // принудительно меняем маркер места падения снаряда
                forceUpdateMarker();

                setMarker();
            }
        }.start();
    }

    ////////////////////////////////// LISTENERS //////////////////////////////////////////////////

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {

        return new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition position) {

                // Log.d(MyApp.LOG_TAG, "Main_Activity: onCameraChange()");

                float newZoomLevel = position.zoom;

                // карта уже инициализирована
                if (!mapInit) {

                    // если новый уровень зума отличается от прежнего
                    if (newZoomLevel != MyApp.getZoomLevel()) {

                        // сохраняем новое значение
                        MyApp.setZoomLevel(newZoomLevel);
                        saveTextInPreferences("zoom_level", "" + newZoomLevel);
                    }
                } else
                    mapInit = false;
            }
        };
    }

    public GoogleMap.OnMapLongClickListener getMapLongClickListener() {

        return new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng targetPoint) {

                // Log.d(MyApp.LOG_TAG, "Second_Activity: onMapLongClick()");

                if (MyApp.isMapLongClickEnabled()) {

                    // задаем маркер снаряда
                    // initBombMarker(targetPoint, MyApp.FIRST_SHOOT_TYPE);
                    initBombMarker(targetPoint);

                    // отображаем маркеры на карте
                    setMarker();

                    // если пользователь может воевать (достаточно здоровья)
                    if(appUser.isActive()) {

                        // если в выбранном оружии есть патроны
                        if (appUser.getSelectedWeapon().getQuantity() != 0)
                            // готовимся к началу боя
                            changeBattleState();
                            // если в выбранном оружии нет патронов
                        else
                            // выводим сообщение, о том что оружие без снарядов
                            showAlertDialog(R.string.weapon_is_empty_title_text, R.string.weapon_is_empty_message_text);
                    }
                    // если пользователь не может воевать
                    else
                        // выводим сообщение, о том что ему нужно дождаться воостановления здоровья
                        showAlertDialog(R.string.not_ready_to_fight_title_text, R.string.not_ready_to_fight_message_text);
                }
            }
        };
    }

    public GoogleMap.OnMarkerClickListener getMarkerClickListener() {

        return new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker marker) {

                // Log.d(MyApp.LOG_TAG, "Main_Activity: onMarkerClick()");

                String opponentId = marker.getTitle();

                // Log.d(MyApp.LOG_TAG, "Main_Activity: onMarkerClick(): opponentId is null: " +(opponentId == null));

                if (opponentId != null) {

                    // Log.d(MyApp.LOG_TAG, "Main_Activity: onMarkerClick(): opponentId= " +opponentId);

                    // Log.d(MyApp.LOG_TAG, "Main_Activity: onMarkerClick(): opponentId= " + title);
                    // LatLng markerPosition = marker.getPosition();

                    int position = getSelectedEnemyId(opponentId);

                    // Log.d(MyApp.LOG_TAG, "Main_Activity: onMarkerClick(): position= " +position);

                    if (position >= 0) {

                        // Log.d(MyApp.LOG_TAG, "Main_Activity: onMarkerClick(): show accept selected enemy dialog");

                        // showAcceptSelectEnemyDialog(opponentId);
                        showAcceptSelectEnemyDialog(position);
                    } else {

                        // Log.d(MyApp.LOG_TAG, "Main_Activity: onMarkerClick(): show info window of opponent");
                    }
                }
                // else
                // Log.d(MyApp.LOG_TAG, "Main_Activity: onMarkerClick(): myMarker");

                return false;
            }
        };
    }

    public GoogleMap.OnMarkerDragListener getMarkerDragListener() {

        return new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {

                // Log.d(MyApp.LOG_TAG, "Second_Activity: onMarkerDragStart()");

                LatLng newPosition = marker.getPosition();

                if (bombMarker != null)
                    bombMarker.setPosition(newPosition);

                if (bombRangeCircle != null)
                    bombRangeCircle.setCenter(newPosition);
            }

            @Override
            public void onMarkerDrag(Marker marker) {

                // Log.d(MyApp.LOG_TAG, "Second_Activity: onMarkerDrag()");

                LatLng newPosition = marker.getPosition();

                if (bombMarker != null)
                    bombMarker.setPosition(newPosition);

                if (bombRangeCircle != null)
                    bombRangeCircle.setCenter(newPosition);
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                // Log.d(MyApp.LOG_TAG, "Second_Activity: onMarkerDragEnd()");

                LatLng newPosition = marker.getPosition();

                if (bombMarker != null)
                    bombMarker.setPosition(newPosition);

                if (bombRangeCircle != null)
                    bombRangeCircle.setCenter(newPosition);

                /////////////////////////////////////////////////////////////

                bombMarkerOptions.position(newPosition);

                bombRangeCircleOptions.center(newPosition);

                ////////////////////////////////////////////////////////////////////////////////////

                if (MyApp.getBattleState() == MyApp.NO_BATTLE)
                    // enemiesList.clear();
                    clearEnemiesList();

                setMarker();

                changeBattleState();
            }
        };
    }

    private LocationListener getLocationListener() {

        return new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

//                Log.d(MyApp.LOG_TAG, "Second_Activity: onLocationChanged(): location is null: " +(location == null));

                // если определен провайдер и определено местоположение пользователя
//                if ((provider != null) && (location != null))
//                    // добавить координату
//                    appUser.addPoint(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                // Log.d(MyApp.LOG_TAG, "Main_Activity: onProviderDisabled(): MyApp.isGPSEnabled: " + MyApp.isGPSEnabled());

                if (MyApp.isGPSEnabled()) {

                    showEnableGPSDialog();
                }
            }
        };
    }

    ////////////////////////////////////// LOCATION ////////////////////////////////////////////////

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Log.d("myLogs", "Second_Activity: onRequestPermissionsResult()");

        switch (requestCode) {

            case MyApp.REQUEST_PERMISSION_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    requestLocationUpdates();

                return;
        }
    }

    private void requestLocationUpdates() {

        // Log.d("myLogs", "Second_Activity: requestLocationUpdates()");
        // Log.d("myLogs", "Second_Activity: requestLocationUpdates(): provider is null: " +(provider == null));

        if (provider != null) {

            // Log.d(MyApp.LOG_TAG, "Main_Activity: requestLocationUpdates(): provider is \"" +provider+ "\"");

            if (provider.equals("gps+network")) {

                // Log.d("myLogs", "Second_Activity: requestLocationUpdates(): provider is \"gps+network\"");

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;

                // Log.d("myLogs", "Second_Activity: requestLocationUpdates(): permission granted");

                // mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,       MyApp.MINIMUM_TIME_BETWEEN_UPDATES, MyApp.MINIMUM_DISTANCE_FOR_UPDATES, mLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,   MyApp.MINIMUM_TIME_BETWEEN_UPDATES, MyApp.MINIMUM_DISTANCE_FOR_UPDATES, mLocationListener);
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MyApp.MINIMUM_TIME_BETWEEN_UPDATES, MyApp.MINIMUM_DISTANCE_FOR_UPDATES, mLocationListener);

                // Log.d("myLogs", "Second_Activity: requestLocationUpdates(): provider is \"" +provider+ "\"");
            }

            showPD();

            startSendPointsService();
        }
    }

    private void removeLocUpdates() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: removeLocUpdates()");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        mLocationManager.removeUpdates(mLocationListener);
    }

    private void setProvider() {

        // Log.d("myLogs", "Second_Activity: setProvider()");

        StringBuilder sb = new StringBuilder();

        if (MyApp.isGPSEnabled())
            sb.append("gps");

        if (MyApp.isInternetEnabled()) {

            if (MyApp.isGPSEnabled())
                sb.append("+");

            sb.append("network");
        }

        if (sb.length() > 0)
            provider = sb.toString();

        if ((!MyApp.isGPSEnabled()) && (!MyApp.isInternetEnabled()))
            provider = null;
    }

    private void startSendPoints() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: startSendPoints()");

        setProvider();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, MyApp.REQUEST_PERMISSION_CODE);

                return;
            }
        } else {

            // Log.d(MyApp.LOG_TAG, "Second_Activity: startSendPoints(): MyApp.isGPSEnabled(): " +MyApp.isGPSEnabled());
            // Log.d(MyApp.LOG_TAG, "Second_Activity: startSendPoints(): MyApp.isInternetEnabled(): " +MyApp.isInternetEnabled());

            if (MyApp.isGPSEnabled() || MyApp.isInternetEnabled())
                requestLocationUpdates();
        }
    }

    ////////////////////////////////////////// USER VALUES ////////////////////////////////////////

    private void setLifeValue() {

        // Log.d(MyApp.LOG_TAG, "Main_Activity: setLifeValue()");

        String enemyLifeValue   = "-";

        ///////////////////////////////////////////////////////

        // если это режим боя
        if(MyApp.getBattleState() == MyApp.BATTLE) {

            // если враг определен
            if(!enemiesList.isEmpty()) {

                // получаем его из списка
                MyOpponent enemy = enemiesList.get(0);

                // если враг получен
                if(enemy != null)
                    // получаем его уровень жизни
                    enemyLifeValue = enemy.getLive();
            }
        }

        ///////////////////////////////////////////////////////

        String myLifeValue = appUser.getCurrLive();

        StringBuilder lifeSB = new StringBuilder("");
        lifeSB.append(enemyLifeValue);
        lifeSB.append("/");
        lifeSB.append(myLifeValue);

        lifeTV.setText(lifeSB.toString());
    }

    private void setExperienceValue() {

        // Log.d(MyApp.LOG_TAG, "Main_Activity: setExperienceValue()");

        String nextLevelExpValue = "1500";
        String currExpValue = appUser.getExperience();

        StringBuilder expSB = new StringBuilder("");
        expSB.append(nextLevelExpValue);
        expSB.append("(");
        expSB.append(currExpValue);
        expSB.append(")");

        experienceTV.setText(expSB.toString());
    }

    // private void setShotNumValue(int value) {
    private void setShotNumValue() {

        // Log.d(MyApp.LOG_TAG, "Main_Activity: setShotNumValue()");

        StringBuilder shotNumSB = new StringBuilder("-");

        // если это боевой режим
        if(MyApp.getBattleState() == MyApp.BATTLE) {

            // если я уже стрелял во врага
            if((myBombHistoryMap != null) && (!myBombHistoryMap.isEmpty())) {

                // очищаем от прежнего значения
                shotNumSB.delete(0, shotNumSB.length());

                // добавляем новое значение
                shotNumSB.append(myBombHistoryMap.size());
            }
        }

        // показываем значение на экране
        shotNumTV.setText("" + shotNumSB.toString());
    }

    ///////////////////////////////// SHOW DIALOGS/////////////////////////////////////////////////

    private void showEnableGPSDialog() {

        // Просим пользователя включить GPS
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.gps_settings_text));
        builder.setMessage(getResources().getString(R.string.gps_is_disabled_text));

        builder.setPositiveButton(getResources().getString(R.string.yes_text),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.create().show();
    }

    private void showAlertDialog(int titleMsgId, int contentMsgId) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: showAlertDialog()");

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // заголовок
        alertDialog.setTitle(context.getResources().getString(titleMsgId));

        // сообщение
        alertDialog.setMessage(context.getResources().getString(contentMsgId));

        alertDialog.setNegativeButton("Ok",

                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    private void showReloadSnackBar(final int reloadTime) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: showSnackBar()");

        final StringBuilder snackBarTextSB = new StringBuilder("");

        // Создаем таймер обратного отсчета на reloadTime секунд с шагом отсчета
        // в 1 секунду (задаем значения в миллисекундах):
        new CountDownTimer((reloadTime * MyApp.ONE_SECOND), MyApp.ONE_SECOND) {

            // Обновляем текст счетчика
            public void onTick(long millisUntilFinished) {

                snackBarTextSB.append("Перезазарядка оружия. ");
                snackBarTextSB.append("Осталось (сек.): ");
                snackBarTextSB.append("" + (millisUntilFinished / MyApp.ONE_SECOND));

                if (reloadSnackBar == null)
                    // reloadSnackBar = Snackbar.make(findViewById(R.id.coordinatorLayout), (CharSequence) snackBarTextSB.toString(), Snackbar.LENGTH_INDEFINITE);
                    reloadSnackBar = Snackbar.make(findViewById(R.id.FirstLayer_LL), (CharSequence) snackBarTextSB.toString(), Snackbar.LENGTH_INDEFINITE);
                else
                    reloadSnackBar.setText((CharSequence) snackBarTextSB.toString());

                reloadSnackBar.setActionTextColor(Color.WHITE);
                reloadSnackBar.show();

                snackBarTextSB.delete(0, snackBarTextSB.length());
            }

            // Задаем действия после завершения отсчета (высвечиваем надпись "Бабах!"):
            public void onFinish() {

                // forceUpdateMarker();

                reloadSnackBar.dismiss();

                // если сервис отправки выстрела запущен
                if (shootServiceStarted)
                    // остановить его
                    stopSendShootService();

                // отображаем полет снаряда
                showFlyingBomb();
            }
        }.start();
    }

    private void showAcceptShootDialog() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: showAcceptShootDialog()");

        AlertDialog.Builder acceptDialog = new AlertDialog.Builder(context);

        acceptDialog.setTitle(context.getResources().getString(R.string.accept_shoot_text));     // заголовок
        acceptDialog.setMessage(context.getResources().getString(R.string.accept_answer_text));  // сообщение

        acceptDialog.setPositiveButton(context.getResources().getString(R.string.yes_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                // startBattle(enemiesList.get(0));

                if(!enemiesList.isEmpty())
                    shootOnEnemy(enemiesList.get(0));
                else
                    shootOnEnemy(null);

                // shootOnEnemy();
            }
        });

        acceptDialog.setNegativeButton(context.getResources().getString(R.string.cancel_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                if (MyApp.getBattleState() == MyApp.NO_BATTLE)
                    clearEnemiesList();
            }
        });

        acceptDialog.setCancelable(true);

        acceptDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });

        acceptDialog.show();
    }

    private void showSelectEnemyInfoDialog() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: showSelectEnemyInfoDialog()");

        AlertDialog.Builder selectEnemyDialog = new AlertDialog.Builder(context);

        selectEnemyDialog.setTitle(context.getResources().getString(R.string.select_enemy_title_text));     // заголовок
        selectEnemyDialog.setMessage(context.getResources().getString(R.string.select_enemy_message_text)); // сообщение

        selectEnemyDialog.setNegativeButton("Ok",

                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        selectEnemyDialog.show();
    }

    private void showAcceptSelectEnemyDialog(final int enemyListPosition) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: showAcceptSelectEnemyDialog()");

        AlertDialog.Builder acceptDialog = new AlertDialog.Builder(context);

        acceptDialog.setTitle(context.getResources().getString(R.string.accept_select_enemy_title_text));      // заголовок
        acceptDialog.setMessage(context.getResources().getString(R.string.accept_select_enemy_message_text));  // сообщение

        acceptDialog.setPositiveButton(context.getResources().getString(R.string.yes_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

                markerDraggable = false;

                MyOpponent enemy = enemiesList.get(enemyListPosition);

                clearEnemiesList();

                enemiesList.add(enemy);

                shootOnEnemy(enemy);
            }
        });

        acceptDialog.setNegativeButton(context.getResources().getString(R.string.no_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {

            }
        });

        acceptDialog.setCancelable(true);

        acceptDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });

        acceptDialog.show();
    }

    @Override
    public void showSystemError() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // скрываем инициализацию
                hidePD();

                stopSendPointsService();

                showAlertDialog(R.string.system_error_title_text, R.string.system_error_content_text);
            }
        });
    }

    @Override
    public void showLocationError() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: showLocationError()");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // скрываем инициализацию
                hidePD();

                stopSendPointsService();

                showAlertDialog(R.string.location_error_title_text, R.string.location_error_content_text);
            }
        });
    }

    /////////////////////////////////////////// SOUND /////////////////////////////////////////////

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

        // Log.d(MyApp.LOG_TAG, "Main_Activity: onLoadComplete()");
        // Log.d(MyApp.LOG_TAG, "Main_Activity: onLoadComplete(): sampleId= " +sampleId+ ", status= " +status);

        shootSoundIsLoaded = true;
    }

    private void playShootSound() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: playShootSound()");

        if (shootSoundIsLoaded)
            mSoundPool.play(shootSound, 1, 1, 0, 0, 1);
    }

    //////////////////////////////////////// VIBRATION ////////////////////////////////////////////

    private void startVibrate() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: startVibrate() ");

        if (vibrator != null)
            vibrator.vibrate(vibratePattern, -1);
    }

    private void stopVibrate() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: stopVibrate() ");

        if (vibrator != null)
            vibrator.cancel();
    }

    ////////////////////////////////////// SEND POINTS SERVICE ////////////////////////////////////

    private void startSendPointsService() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: startSendPointsService() ");

        sendPointsServiceIntent = new Intent(this, SendPointsService.class);

        startService(sendPointsServiceIntent);
    }

    private void stopSendPointsService() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: stopSendPointsService() ");

        if (sendPointsService != null)
            sendPointsService.cancelTask();

        stopService(sendPointsServiceIntent);
    }

    private void unBindSendPointsService() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: unBindSendPointsService()");

        if (!sendPointsServiceIsBound)
            return;

        sendPointsService.cancelTask();

        unbindService(sendPointsServiceConnection);
        sendPointsServiceIsBound = false;
    }

    ////////////////////////////////////// SEND SHOOT SERVICE /////////////////////////////////////

    private void startSendShootService() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: startSendShootService()");

        MyApp.setMapLongClickEnabled(false);

        startService(new Intent(this, SendShootService.class));

        shootServiceStarted = true;
    }

    private void stopSendShootService() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: stopSendShootService()");

        MyApp.setMapLongClickEnabled(true);

        stopService(new Intent(this, SendShootService.class));

        shootServiceStarted = false;
    }

    ////////////////////////////////////// BATTLE /////////////////////////////////////////////////

    @Override
    public void onEnemyStartBattle(Map<String, MyOpponent> opponentsMap) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onEnemyStartBattle()");

        hidePD();

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onEnemyStartBattle(): battle state= " +MyApp.getBattleState());

        if (MyApp.getBattleState() == MyApp.NO_BATTLE) {

            // Log.d(MyApp.LOG_TAG, "Second_Activity: onEnemyStartBattle(): set myBomb to null");

            // myBomb = null;

            ///////////////////////////////////////////////////////////////////////////

            clearEnemiesList();
            enemiesList.add(opponentsMap.get(0));

            reachMyOpponentsMap(opponentsMap);

            ///////////////////////////////////////////////////////////////////////////

            playShootSound();

            ///////////////////////////////////////////////////////////////////////////

            setStartBattle();
        }
    }

    @Override
    public void onShowOpponents(Map<String, MyOpponent> opponentsMap) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onShowOpponents()");

        reachMyOpponentsMap(opponentsMap);

        hidePD();

        // если не идет отображение летящего снаряда
        if(!bombIsFlying)
            // рисуем маркеры на карте
            setMarker();
    }

    @Override
    public void onShootResult() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onShootResult()");

        setReadyToShootMode();

        // Log.d(MyApp.LOG_TAG, "Second_Activity: onShootResult(): myBombHistoryMap is null: " +(myBombHistoryMap == null));

        if (myBombHistoryMap != null) {

            // Log.d(MyApp.LOG_TAG, "Second_Activity: onShootResult(): myBombHistoryMap is empty: " +(myBombHistoryMap.isEmpty()));

            if (!myBombHistoryMap.isEmpty()) {

                // Log.d(MyApp.LOG_TAG, "Second_Activity: onShootResult(): myBombHistoryMap size= " +(myBombHistoryMap.size()));

                if (myBombHistoryMap.size() == 10) {

                    setNoBattle();
                }
            }
        }
    }

    @Override
    public void onBattleStop() {

        // Log.d(MyApp.LOG_TAG, "Main_Activity: onBattleStop()");

        setNoBattle();
    }

    private void reachMyOpponentsMap(Map<String, MyOpponent> opponentsMap) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: reachMyOpponentsMap()");

        if (myOpponentsMap != null) {

            if (myOpponentsMap.size() > 0)
                myOpponentsMap.clear();

            if (opponentsMap != null)
                myOpponentsMap.putAll(opponentsMap);
        }
    }

    private void clearEnemiesList() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: clearEnemiesList()");

        enemiesList.clear();
    }

    private void setStartBattle() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setStartBattle()");

        // заявляю что я теперь в состоянии войны
        appUser.setIsInBattle("1");

        MyApp.setBattleState(MyApp.BATTLE);

        googleMap.setOnMarkerClickListener(null);

        setMarker();

        sendPointsService.setInterval(MyApp.SEND_POINTS_SHORT_INTERVAL);
    }

    private void setNoBattle() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setNoBattle()");

        // заявляю что я теперь не в состоянии войны
        appUser.setIsInBattle("0");

        MyApp.setBattleState(MyApp.NO_BATTLE);

        sendPointsService.setInterval(MyApp.SEND_POINTS_LONG_INTERVAL);

        //////////////////////////////////////////////////////////////////////

        myBomb = null;

        bombMarkerShow = false;

        //////////////////////////////////////////////////////////////////////

        MyApp.clearMyBombHistoryMap();
        MyApp.clearEnemyBombHistoryMap();

        MyApp.setEnemyId("");

        // назначаем карте слушателя щелчка по маркеру
        googleMap.setOnMarkerClickListener(getMarkerClickListener());

        //////////////////////////////////////////////////////////////////////

        myShootsMap.clear();

        myOpponentsMap.clear();

        // enemiesList.clear();
        clearEnemiesList();

        //////////////////////////////////////////////////////////////////////

        // если сервис отправки выстрела запущен
        if (shootServiceStarted)
            // остановить его
            stopSendShootService();

        setShotNumValue();

        // showEndBattleSnackBar(5);

        showBattleResult();
    }

    private void setReloadMode() {

        MyApp.addMyBombToBombHistoryMap(myBomb);

        // Log.d(MyApp.LOG_TAG, "(" +MyApp.getMyBombHistoryMap().size() +") add bomb to MyBombHistoryMap: lat= " +myBomb.getLatitude()+ ", lon= " +myBomb.getLongitude());

        showReloadSnackBar(10);

        // если сервис отправки выстрела не запущен
        if (!shootServiceStarted)
            // запустить его
            startSendShootService();
    }

    private void setReadyToShootMode() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setReadyToShootMode()");

        // очищаем список с последним выстрелом
        List<String> myCurrentShootIdList = appUser.getMyCurrentShootIdList();
        myCurrentShootIdList.clear();

        myBomb = null;

        // меняем показатель жизни
        setLifeValue();

        // меняем показатель кол-ва сделанных выстрелов
        setShotNumValue();
    }

    private void showBattleResult() {

        Intent intent = new Intent(context, BattleResult_Activity.class);
        startActivity(intent);
    }

    private void setMyBombRoute() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute()");

        // если не получено выбранное оружие
        if(mySelectedWeapon == null)
            // стоп
            return;

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute(): myLocation.getLatitude()= " +myLocation.getLatitude()+ ", myLocation.getLongitude()= " +myLocation.getLongitude());
        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute(): myBomb.getLatitude()= " +myBomb.getLatitude()+ ", myBomb.getLongitude()= " +myBomb.getLongitude());

        // получаем скорость полета снаряда
        int speedFly = mySelectedWeapon.getSpeedFly();

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute(): speedFly= " +speedFly);

        // получаем разницу по широте
        double latDifference = (myBomb.getLatitude() - myLocation.getLatitude());

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute(): latDifference= " +latDifference);

        // получаем разницу по долготе
        double lonDifference = (myBomb.getLongitude() - myLocation.getLongitude());

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute(): lonDifference= " +lonDifference);

        // получаем шаг для прибавки широты
        double latStep = (latDifference / speedFly);

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute(): latStep= " +latStep);

        // получаем шаг для прибавки долготы
        double lonStep = (lonDifference / speedFly);

        // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute(): lonStep= " +lonStep);

        // будем хранить новую широту
        double newLat =  myLocation.getLatitude();

        // будем хранить новую долготу
        double newLon =  myLocation.getLongitude();

        for(int i=0; i<speedFly; i++) {

            newLat += latStep;
            newLon += lonStep;

            routePointsList.add(new LatLng(newLat, newLon));

            // Log.d(MyApp.LOG_TAG, "Second_Activity: setMyBombRoute(): (" +i+ ") newLat= " +newLat+ ", newLon= " +newLon);
        }
    }

    private int getSelectedEnemyId(String opponentId) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: getSelectedEnemyId(): opponentId= " +opponentId);

        int result = -1;

        for (int i = 0; i < enemiesList.size(); i++) {

            MyOpponent enemy = enemiesList.get(i);

            if (enemy.getId().equals(opponentId)) {

                result = i;

                break;
            }
        }

        return result;
    }

    private void changeBattleState() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: changeBattleState()");

        int enemiesSum = 0;

        switch (MyApp.getBattleState()) {

            case MyApp.NO_BATTLE:   // режим вне боя
                enemiesSum = enemiesList.size();

                // если в радиус поражения попал один оппонент либо никто
                if (enemiesSum < 2)
                    // показываем окно для подтвеждения отправки выстрела
                    showAcceptShootDialog();
                    // если в радиус поражения попало несколько оппонентов
                else
                    // сообщаем, что надо выбрать одного из оппонентов
                    showSelectEnemyInfoDialog();

                break;

            case MyApp.BATTLE:      // режим боя

                // если снаряд задан
                if (myBomb != null)
                    // показываем окно для подтвеждения отправки выстрела
                    showAcceptShootDialog();

                break;
        }
    }

    private void shootOnEnemy(MyOpponent enemy) {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: shootOnEnemy()");

        // Log.d(MyApp.LOG_TAG, "1) Second_Activity: shootOnEnemy() myBomb is null: " +(myBomb == null));

        if(myBomb != null) {

            try {

                JSONObject sendShootJSONObj = new JSONObject();
                JSONArray positionJSONArr   = new JSONArray();

                JSONObject userJSONObj      = new JSONObject();

                //////////////////////////////// POSITION //////////////////////////////////////

                JSONObject pointJSONObj = new JSONObject();

                String time = "" + System.currentTimeMillis();

                pointJSONObj.put("lat", "" + myLocation.getLatitude());
                pointJSONObj.put("lon", "" + myLocation.getLongitude());
                pointJSONObj.put("time", time);

                positionJSONArr.put(pointJSONObj);

                userJSONObj.put("POSITION", positionJSONArr);

                //////////////////////////////// GAME_STATUS ///////////////////////////////////

                // Log.d(MyApp.LOG_TAG, "Second_Activity: shootOnEnemy() enemy is null: " +(enemy == null));

                // если соперник получен
                if (enemy != null) {
                    // получаем идентификатор соперника
                    String enemyId = enemy.getId();

                    ////////////////////////////////////////////////////////////////////////////////

                    // убираем из коллекции всех соперников, кроме того что попал в радиус поражения

                    Set<String> myOpponentsIdsSet = myOpponentsMap.keySet();
                    List<String> myOpponentsIdsList = new ArrayList<String>();

                    if (myOpponentsIdsSet != null)
                        myOpponentsIdsList.addAll(myOpponentsIdsSet);

                    for (int i = 0; i < myOpponentsIdsList.size(); i++) {

                        String opponentId = myOpponentsIdsList.get(i);

                        // если это это не соперник попавший в радиус поражения
                        if (!opponentId.equals(enemyId))
                            // удаляем его из коллекции соперников
                            appUser.removeOpponentFromMap(opponentId);
                    }

                    ////////////////////////////////////////////////////////////////////////////////

                    // запоминаем идентификатор врага
                    MyApp.setEnemyId(enemyId);

                    ////////////////////////////////////////////////////////////////////////////////

                    if (MyApp.getBattleState() == MyApp.NO_BATTLE)
                        // setBattleState(BATTLE);
                        setStartBattle();

                    ////////////////////////////////////////////////////////////////////////////////

                    // формируем блок GAME_STATUS
                    JSONObject gameStatusJSONObj = new JSONObject();

                    int isActive = 0;
                    int isWar = 0;

                    // если готов к бою
                    // if (appUser.isActive())
                    // меняем значение
                    // isActive = 1;

                    // если уже в бою
                    if (appUser.isInBattle())
                        // меняем значение
                        isWar = 1;

                    // gameStatusJSONObj.put("is_active",      "" + isActive);
                    gameStatusJSONObj.put("is_war",         "" + isWar);
                    gameStatusJSONObj.put("id_opponents",   MyApp.getEnemyId());
                    gameStatusJSONObj.put("time_to_ping",   "");

                    userJSONObj.put("GAME_STATUS",  gameStatusJSONObj);
                }

                //////////////////////////////// SHOT //////////////////////////////////////////

                JSONObject shootJSONObj = new JSONObject();
                shootJSONObj.put("id",          myBomb.getId());
                shootJSONObj.put("is_active",   "1");
                shootJSONObj.put("lon",         "" + myBomb.getLongitude());
                shootJSONObj.put("lat",         "" + myBomb.getLatitude());
                shootJSONObj.put("time",        time);

                userJSONObj.put("SHOT",         shootJSONObj);

                ///////////////////////////////////////////////////////////

                JSONArray playersJSONArr = new JSONArray();
                playersJSONArr.put(userJSONObj);

                String userIdStr = appUser.getId();

                if (userIdStr == null)
                    return;

                if (userIdStr.equals(""))
                    return;

                JSONObject playersJSONObj = new JSONObject();
                playersJSONObj.put(userIdStr, userJSONObj);

                sendShootJSONObj.put("PLAYERS", playersJSONObj);

                ///////////////////////////////////////////////////////////

                // получаем идентификатор запускаемого снаряда
                String bombKey = myBomb.getId();

                // очищаем список с последним запущенным снарядом и кладем новый
                List<String> myCurrentShootIdList = appUser.getMyCurrentShootIdList();
                myCurrentShootIdList.clear();
                myCurrentShootIdList.add(bombKey);

                myShootsMap.put(bombKey, sendShootJSONObj);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            // вычисляем маршрут полета снаряда
            setMyBombRoute();

            // запускаем перезарядку оружия
            setReloadMode();
        }
    }

    ////////////////////////////////////// UTILS //////////////////////////////////////////////////

    private void showPD() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: showPD()");

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(getResources().getString(R.string.init_text));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }
    }

    private void hidePD() {

        // Log.d(MyApp.LOG_TAG, "Second_Activity: hidePD()");

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * сохранение заданных значений в Preferences
     *
     * @param field - поле
     * @param value - значение
     */
    private void saveTextInPreferences(String field, String value) {
        SharedPreferences.Editor ed = shPref.edit();
        ed.putString(field, value);
        ed.commit();
    }

    /**
     * загрузка сохраненных значений из Preferences
     */
    private void loadTextFromPreferences() {

        // Log.d(MyApp.LOG_TAG, "Main_Activity: loadTextFromPreferences()");

        if (shPref.contains("server_url"))
            MyApp.setServerUrl(shPref.getString("server_url", ""));

        if (shPref.contains("zoom_level"))
            MyApp.setZoomLevel(Float.parseFloat(shPref.getString("zoom_level", "" + MyApp.getZoomLevel())));
        else
            MyApp.setZoomLevel(MyApp.getZoomLevel());
    }
}