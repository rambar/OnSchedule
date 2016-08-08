package com.kaist.onschedule;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import net.daum.mf.speech.api.TextToSpeechClient;
import net.daum.mf.speech.api.TextToSpeechListener;
import net.daum.mf.speech.api.TextToSpeechManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import static java.lang.Thread.sleep;

public class ReportActivity extends AppCompatActivity implements TextToSpeechListener {

    private static final String TAG = "kaist_ReportActivity";
    private static final String PROX_ALERT_INTENT = new String("com.kaist.onschedule.action.proximityalert");
    private SchedDataBase mSchedDB;
    private SchedInfo mSchedInfo;
    private LocationManager lm;

    LocationManager locationManager;
    LocationListener locationListner;
    Location currentLocation;
    PendingIntent proximityIntent;
    ProximityReceiver receiver;

    private static final String WHERE_CURRENT = "current";
    private static final String WHERE_ROUTE = "route";
    private static final String WHERE_ROUTE1 = "route1";
    private static final String WHERE_ROUTE2 = "route2";
    private static final String WHERE_ROUTE3 = "route3";
    private static final String WHERE_ROUTE4 = "route4";
    private static final String WHERE_ROUTE5 = "route5";
    private static final String WHERE_DESTINATION = "destination";
    private static final String WHERE_DEPARTURE = "departure";
    private static final String SPEECH_RUN = "달리세요";
    private static final String SPEECH_WALK = "걸으세요";
    private static final int CURRENT = 0;
    private static final int ROUTE1 = 1;
    private static final int ROUTE2 = 2;
    private static final int ROUTE3 = 3;
    private static final int ROUTE4 = 4;
    private static final int ROUTE5 = 5;
    private static final int DESTINATION = 6;
    private static final int DEPARTURE = 7;
    private static final int ALERT_RADIUS = 200;
    //private static String[] order = {"first", "second"};
    private static String[] order = {"첫번째", "두번째"};
    private int index = 0;
    private static boolean enableTTS = false;
    private final int SLEEP_SHORT = 3000;
    private final int SLEEP_LONG = 4000;

    private static final int CLOSE = 0;
    private static final int FAR = 1;
    private boolean[] isCurrent = {false, false};
    private boolean[] isDeparture = {false, false};
    private boolean[][] isRoute = {{false, false}, {false, false}, {false, false}, {false, false}, {false, false}};
    private boolean[] isDestination = {false, false};
    private String[][] mRouteArray = {{"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}};
    private String[] mRoute = {"", "", "", "", ""};
    private double[][] mPoint = {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}};
    private double[] mDeparture = {0, 0};
    private double[] mDestination = {0, 0};
    private String[] splittime = {"", ""};
    public static String strSeparator = "__,__";
    private TextToSpeechClient ttsClient;

    private static SubwayInfo subwayInfo;
    private TextToSpeech myTTS;

    private TextView tvReport;
    private ScrollView svReport;
    private Button btnFinishReport;

    public static void setEnableTTS(boolean enable) {
        Log.i(TAG, "TTS Enable=" + enable);
        enableTTS = enable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tvReport = (TextView) findViewById(R.id.tvReport);
        svReport = (ScrollView) findViewById(R.id.svReport);
        btnFinishReport = (Button) findViewById(R.id.btnFinishReport);
        btnFinishReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });

        TextToSpeechManager.getInstance().initializeLibrary(getApplicationContext());

        ttsClient = new TextToSpeechClient.Builder()
                .setApiKey(SchedConstant.DAUM_MAP_API_KEY)              // 발급받은 api key
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_1)            // 음성합성방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM)  //TTS 음색 모드 설정(여성 차분한 낭독체)
                .setListener(this)
                .build();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                Log.i(TAG, "Latitude=" + String.valueOf(location.getLatitude()) + " Longitude=" + String.valueOf(location.getLongitude()));

                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeUpdates(locationListner);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        mSchedDB = new SchedDataBase(this, null, null, 1);
        mSchedInfo = new SchedInfo();

        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");

        /* 해당 data, time 정보의 Schedule Database 정보 가져오기 */
        int i = 0;
        do {
            i++;
            mSchedInfo = mSchedDB.getEachRowOfSchedDatabase(i);

            if (date.equals((String) mSchedInfo.get_date()) && time.equals((String) mSchedInfo.get_time())) {
                Log.i(TAG, mSchedInfo.get_date() + " " + mSchedInfo.get_time() + " " + mSchedInfo.get_departure() + " " + mSchedInfo.get_destination());
                break;
            }
        } while (!mSchedDB.isGetDBFinish());

        /* Route 정보 Array 로 분리 */
        mRoute[0] = mSchedInfo.get_cponfoot1();
        mRoute[1] = mSchedInfo.get_cponfoot2();
        mRoute[2] = mSchedInfo.get_cponfoot3();
        mRoute[3] = mSchedInfo.get_cponfoot4();
        mRoute[4] = mSchedInfo.get_cponfoot5();

        for (int j = 0; j < 5; j++) {
            if (!mRoute[j].isEmpty()) {
                mRouteArray[j] = convertStringToArray(mRoute[j]);
                Log.i(TAG, "*********mRoute[4]=" + mRoute[j] + "*********mRouteArray[j]=" + mRouteArray[j]);
                for (int q = 0 ; q < 4; q++) {
                    Log.i(TAG, "*********mRouteArray[" + j + "][" + q + "]=" +  mRouteArray[j][q]);
                }
            }
        }

        /* Get current location point x ando point y */
        LocationDataBase locDB = new LocationDataBase(this, null, null, 1);
        LocationInfo info = new LocationInfo();
        int j = 0;
        do {
            j++;
            info = locDB.getEachRowOfLocationDatabase(j);

            //entries.add(new SchedInfo());
            if (info.get_address() != null && info.get_address().equals(mSchedInfo.get_departure())) {
                Log.i(TAG, info.get_address() + " " + info.get_pointx() + " " + info.get_pointy());
                break;
            }
        } while (!locDB.isGetDBFinish());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        try {
            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1000,
                            1, locationListner);
                    Log.i(TAG, "Network");
                    if (locationManager != null) {
                        currentLocation = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (currentLocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                1000,
                                1, locationListner);
                        Log.i(TAG, "GPS Enabled");
                        if (locationManager != null) {
                            currentLocation = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        /* TO DO */
        /* If current location is not registered departure, Toast warning message */
        // create an intent to the Alarm Receiver class
        receiver = new ProximityReceiver(this) {
            @Override
            protected void onProximityEvent(String where, boolean close) {
                //To Do
                if (where.equals(WHERE_CURRENT)) {
                    if (close) {
                        Log.i(TAG, "current location close event");
                        isCurrent[CLOSE] = true;
                        isCurrent[FAR] = false;
                    } else {
                        Log.i(TAG, "current location far event");
                        isCurrent[CLOSE] = false;
                        isCurrent[FAR] = true;
                    }
                } else if (where.equals(WHERE_DEPARTURE)) {
                    if (close) {
                        Log.i(TAG, "departure location close event");
                        isDeparture[CLOSE] = true;
                        isDeparture[FAR] = false;
                    } else {
                        Log.i(TAG, "departure location far event");
                        isDeparture[CLOSE] = false;
                        isDeparture[FAR] = true;
                    }
                } else if (where.contains(WHERE_ROUTE)) {

                    where = where.replaceAll("\\D+","");
                    //where = "5";
                    Intent removeintent = new Intent();
                    PendingIntent premoveintent = PendingIntent.getBroadcast(ReportActivity.this, Integer.valueOf(where) - 1, removeintent, 0);
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.removeProximityAlert(premoveintent);

                    if(!where.isEmpty()) {

                        if (close) {
                            isRoute[Integer.valueOf(where) - 1][CLOSE] = true;
                            isRoute[Integer.valueOf(where) - 1][FAR] = false;

                            SubWayNameIDDataBase subwaydb = new SubWayNameIDDataBase(getBaseContext(), null, null, 1);
                            SubwayNameIDInfo subwayNameIDInfo = new SubwayNameIDInfo();

                            String[] subwaydbid  = {"", "", "", ""}; //동일 이름의 여러 노선의 ID 가 다르므로 4개로 설정
                            int i = 0;
                            int l = 0;

                            do {

                                subwayNameIDInfo = subwaydb.getEachRowOfSubwayNameIDDatabase(i);

                                Log.i(TAG, "mRouteArray[" + (Integer.valueOf(where) - 1) + "][2]=" + mRouteArray[Integer.valueOf(where) - 1][2] + "subwayNameIDInfo.getName()=" + subwayNameIDInfo.getName());
                                if (subwayNameIDInfo.getName() != null && subwayNameIDInfo.getName().contains(mRouteArray[Integer.valueOf(where) - 1][2])) {
                                    if(l < subwaydbid.length) {
                                        subwaydbid[l] = subwayNameIDInfo.getSubwayid();
                                    }
                                    l++;
                                }
                                i++;
                            } while (!subwaydb.isGetDBFinish());
                            i = 0;

                            JSONManager json = new JSONManager(getBaseContext());
                            SubwayInfo[] subwayInfoArray;
                            //matcher = pattern.matcher(mRouteArray[selection][0]);
                            //json.getJSONDATA(mRouteArray[selection][2], false);
                            json.getJSONDATA(mRouteArray[Integer.valueOf(where) - 1][0], false);
                            subwayInfoArray = json.getSubwayJSONInfo();

                            String[]  arrivalTime = {"", "", "", ""}; // 몇 분 몇 초 후
                            String updn = "";
                            int k = 0;
                            for(int j = 0 ; j < subwayInfoArray.length ; j++) {

                            /* 경유지의 다음 전철역 정보에 해당하는 JSON 정보의 도착 정보 가져오기 */
                                Log.i(TAG, "subwayInfoArray[" + j + "].getStatnTid()=" + subwayInfoArray[j].getStatnTid() + " subwaydbid=" + subwaydbid[j] + " subwayInfoArray[j].getBarvlDt()=" + subwayInfoArray[j].getBarvlDt());


                                if (subwaydbid[0].isEmpty() && subwaydbid[1].isEmpty() && subwaydbid[2].isEmpty() && subwaydbid[3].isEmpty()) {
                                    Log.i(TAG, "No Subway ID information");
                                } else {
                                    for (l = 0; l < subwaydbid.length; l++) {
                                        int sub;
                                        if (!subwaydbid[l].isEmpty() && !subwayInfoArray[j].getStatnTid().isEmpty()) {
                                            Log.i(TAG, "subwaydbid[" + l + "]=" + subwaydbid[l] + "subwayInfoArray[" + j + "].getStatnTid()=" + subwayInfoArray[j].getStatnTid());
                                            sub = Integer.valueOf(subwaydbid[l]) - Integer.valueOf(subwayInfoArray[j].getStatnTid());
                                            index = l;
                                            //subwaydbid[index] = subwayInfoArray[j].getStatnTid();
                                            if (sub == 0) {
                                                Log.i(TAG, "**************subwaydbid[" + l + "]=" + subwaydbid[l] + "subwayInfoArray[" + j + "].getStatnTid()=" + subwayInfoArray[j].getStatnTid());
                                                updn = "하행";
                                                break;
                                            } else if ((0 < sub && sub < 3) || (0 > sub && sub > -3)) {
                                                updn = "상행";
                                                break;
                                            }
                                        }
                                    }
                                    l = 0;
                                }

                                //if((!subwaydbid[index].isEmpty() && subwayInfoArray[j].getStatnTid().contains(subwaydbid[index]) && subwayInfoArray[j].getArvlMsg3().contains(updn))) {
                                if((!subwaydbid[index].isEmpty() && subwayInfoArray[j].getArvlMsg3().contains(updn))) {
                                    Log.i(TAG, "k=" + k + "order.length=" + order.length);
                                    if(k < order.length) {
                                        Log.i(TAG, "write text view" + k);
                                        tvReport.append("The " + order[k] + " subway for " + mRouteArray[Integer.valueOf(where) - 1][2]);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            tvReport.append(System.lineSeparator());
                                        }
                                        tvReport.append(" might arrive after " + subwayInfoArray[j].getArvlMsg2());
                                        if(enableTTS) {
                                            Log.i(TAG, "TTS Client");
                                            ttsClient.setSpeechText(order[k] + mRouteArray[Integer.valueOf(where) - 1][2] + "향 열차가 " + subwayInfoArray[j].getArvlMsg2() + "에 도착합니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                            ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                            try {
                                                sleep(SLEEP_SHORT);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            tvReport.append(System.lineSeparator());
                                            tvReport.append(System.lineSeparator());
                                        }
                                        svReport.fullScroll(View.FOCUS_DOWN);
                                        arrivalTime[k] = subwayInfoArray[j].getBarvlDt();
                                        k++;
                                    }
                                }
                            }

                            Log.i(TAG, "mSchedInfo.get_time()=" + mSchedInfo.get_time());

                            String[] time = mSchedInfo.get_time().split(":");
                            int schedhour = Integer.valueOf(time[0]);
                            int schedmin = Integer.valueOf(time[1]);
                            Calendar cal = Calendar.getInstance();
                            int currminute = cal.get(Calendar.MINUTE);
                            //12 hour format
                            int currhour = cal.get(Calendar.HOUR);
                            //24 hour format
                            int currhourofday = cal.get(Calendar.HOUR_OF_DAY);

                            int timediff = 0;
                            int timediffmin = 0;
                            int timediffhour = 0;
                            if (schedmin < currminute) {
                                timediffmin = 60 - (currminute - schedmin);
                                if (schedhour > currhourofday) {
                                    timediffhour = schedhour - currhourofday - 1;
                                }
                            } else {
                                timediffmin = schedmin - currminute;
                                if (schedhour > currhourofday) {
                                    timediffhour = schedhour - currhourofday;
                                }
                            }
                            timediff = timediffhour * 60 * 60 * 1000 + timediffmin * 60 * 1000;

                            long firstDifference = 0;
                            long secondDifference = 0;
                            if(!mRouteArray[Integer.valueOf(where) - 1][2].contains("Empty")) {

                                /* 첫번째 지하철 도착 예정 시간 */
                                Log.i(TAG, " arrivalTime[0]=" + arrivalTime[0]);
                                String[] firstArrTime = arrivalTime[0].split("\\s+");
                                long firstMillisecArrivalTime = 0;
                                if (firstArrTime.length == 2) {
                                    firstArrTime[1] = firstArrTime[1].replaceAll("\\D+", ""); //초
                                    firstArrTime[0] = firstArrTime[0].replaceAll("\\D+", ""); //분
                                    if(!firstArrTime[0].isEmpty() && !firstArrTime[1].isEmpty()) {
                                        firstMillisecArrivalTime = Integer.valueOf(firstArrTime[1]) * 1000 + Integer.valueOf(firstArrTime[0]) * 60 * 1000;
                                    }
                                } else if (firstArrTime.length == 1) {
                                    firstArrTime[0] = firstArrTime[0].replaceAll("\\D+", ""); //초
                                    if (!firstArrTime[0].isEmpty()) {
                                        firstMillisecArrivalTime = Integer.valueOf(firstArrTime[0]) * 1000;
                                    }

                                }

                                firstDifference = timediff - firstMillisecArrivalTime;

                                 /* 두번째 지하철 도착 예정 시간 */
                                Log.i(TAG, " arrivalTime[1]=" + arrivalTime[1]);
                                String[] secondArrTime = arrivalTime[1].split("\\s+");
                                long secondMillisecArrivalTime = 0;
                                if (secondArrTime.length == 2) {
                                    secondArrTime[1] = secondArrTime[1].replaceAll("\\D+", ""); //초
                                    secondArrTime[0] = secondArrTime[0].replaceAll("\\D+", ""); //분
                                    if(!secondArrTime[0].isEmpty() && !secondArrTime[1].isEmpty()) {
                                        secondMillisecArrivalTime = Integer.valueOf(secondArrTime[1]) * 1000 + Integer.valueOf(secondArrTime[0]) * 60 * 1000;
                                    }

                                } else if (secondArrTime.length == 1) {
                                    secondArrTime[0] = secondArrTime[0].replaceAll("\\D+", ""); //초
                                    if(!secondArrTime[0].isEmpty()) {
                                        secondMillisecArrivalTime = Integer.valueOf(secondArrTime[0]) * 1000;
                                    }
                                }
                                secondDifference = timediff - secondMillisecArrivalTime;
                            }

                            Log.i(TAG, " mSchedInfo.get_leadtimetotal()=" + mSchedInfo.get_leadtimetotal());
                            String[] leadtimetotal = mSchedInfo.get_leadtimetotal().split("\\s+");
                            long millisecondLeadTimeTotal = 0;
                            if(leadtimetotal.length == 2) {
                                if(!leadtimetotal[0].isEmpty() && !leadtimetotal[1].isEmpty()) {
                                    millisecondLeadTimeTotal = Integer.valueOf(leadtimetotal[0]) * 60 * 60 * 1000 + Integer.valueOf(leadtimetotal[1]) * 60 * 1000;
                                }
                            } else {
                                if(!leadtimetotal[0].isEmpty()) {
                                    millisecondLeadTimeTotal = Integer.valueOf(leadtimetotal[0]) * 60 * 1000;
                                }
                            }

                            String[] routeTime = mRouteArray[Integer.valueOf(where) - 1][1].split("\\s+");
                            Log.i(TAG, " mRouteArray[Integer.valueOf(where) - 1][1]=" +  mRouteArray[Integer.valueOf(where) - 1][1]);
                            long millisecondRouteTime = 0;
                            if(routeTime.length == 2) {
                                if(!routeTime[0].isEmpty() && !routeTime[1].isEmpty()) {
                                    millisecondRouteTime = Integer.valueOf(routeTime[0]) * 60 * 1000 + Integer.valueOf(routeTime[1]) * 1000;
                                }
                            } else {
                                if(!routeTime[0].isEmpty()) {
                                    millisecondRouteTime = Integer.valueOf(routeTime[0]) * 1000;
                                }
                            }

                            if(!mRouteArray[Integer.valueOf(where) - 1][2].contains("Empty")) {
                                if (secondDifference > (millisecondLeadTimeTotal - millisecondRouteTime)) {
                                    tvReport.append("You are not late. Keep walking");
                                    if (enableTTS) {
                                        Log.i(TAG, "TTS Client");
                                        //ttsClient.setSpeechText("You are not late. Keep walking");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                        ttsClient.setSpeechText("걸어가셔도 늦지 않습니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                        ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                        try {
                                            sleep(SLEEP_SHORT);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }


                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        tvReport.append(System.lineSeparator());
                                        tvReport.append(System.lineSeparator());
                                    }
                                    svReport.fullScroll(View.FOCUS_DOWN);
                                } else {
                                    if (firstDifference > (millisecondLeadTimeTotal - millisecondRouteTime)) {
                                        tvReport.append("You are not late. Keep walking");
                                        if (enableTTS) {
                                            Log.i(TAG, "TTS Client");
                                            ttsClient.setSpeechText("걸어가셔도 늦지 않습니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                            ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                            try {
                                                sleep(SLEEP_SHORT);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            tvReport.append(System.lineSeparator());
                                            tvReport.append(System.lineSeparator());
                                        }
                                        svReport.fullScroll(View.FOCUS_DOWN);
                                    } else {
                                        tvReport.append("You need to run toward " + mRouteArray[ROUTE1 - 1][0]);
                                        if(enableTTS) {
                                            Log.i(TAG, "TTS Client");
                                            ttsClient.setSpeechText( mRouteArray[ROUTE1 - 1][0] + "으로 뛰어 가셔야 합니다. ");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                            ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                            try {
                                                sleep(SLEEP_SHORT);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            tvReport.append(System.lineSeparator());
                                            tvReport.append(System.lineSeparator());
                                        }
                                        svReport.fullScroll(View.FOCUS_DOWN);
                                    }
                                }
                            } else {
                                if (timediff > millisecondRouteTime) {
                                    tvReport.append("You are not late. Keep walking");
                                    if(enableTTS) {
                                        ttsClient.setSpeechText("걸어가셔도 늦지 않습니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅
                                        ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                        try {
                                            sleep(SLEEP_SHORT);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        tvReport.append(System.lineSeparator());
                                        tvReport.append(System.lineSeparator());
                                    }
                                    svReport.fullScroll(View.FOCUS_DOWN);
                                } else {
                                    tvReport.append("You need to run toward " + mRouteArray[ROUTE1 - 1][0]);
                                    if(enableTTS) {
                                        Log.i(TAG, "TTS Client");
                                        ttsClient.setSpeechText( mRouteArray[ROUTE1 - 1][0] + "으로 뛰어 가셔야 합니다. ");   //뉴톤톡 하고자 하는 문자열을 미리 세팅
                                        ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                        try {
                                            sleep(SLEEP_SHORT);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        tvReport.append(System.lineSeparator());
                                        tvReport.append(System.lineSeparator());
                                    }
                                    svReport.fullScroll(View.FOCUS_DOWN);
                                }
                            }

                        } else {
                            isRoute[Integer.valueOf(where) - 1][CLOSE] = false;
                            isRoute[Integer.valueOf(where) - 1][FAR] = true;
                        }
                        Intent removerouteintent = new Intent();
                        PendingIntent premoverouteintent = PendingIntent.getBroadcast(ReportActivity.this, (Integer.valueOf(where) - 1), removerouteintent, 0);
                        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        locationManager.removeProximityAlert(premoverouteintent);
                    }

                }else if (where.equals(WHERE_DESTINATION)) {
                    if (close) {
                        isDestination[CLOSE] = true;
                        isDestination[FAR] = false;
                        tvReport.append("You are arrived in the destination");
                        if(enableTTS) {
                            Log.i(TAG, "TTS Client");
                            ttsClient.setSpeechText("목적지에 도착하였습니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                            ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                            try {
                                sleep(SLEEP_SHORT);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            tvReport.append(System.lineSeparator());
                            tvReport.append(System.lineSeparator());
                        }
                        svReport.fullScroll(View.FOCUS_DOWN);

                    } else {
                        isDestination[CLOSE] = false;
                        isDestination[FAR] = true;
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
        registerReceiver(receiver, filter);

        /* Verify I'm in the departure location after 10 secs */
        Intent myintent = new Intent(PROX_ALERT_INTENT);
        /*
        myintent.putExtra("where", WHERE_CURRENT);
        proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, CURRENT, myintent, 0);

        Log.i(TAG, "currentLocation=" + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
        locationManager.addProximityAlert(currentLocation.getLatitude(), currentLocation.getLongitude(), ALERT_RADIUS, -1, proximityIntent);
        */
        myintent.putExtra("where", WHERE_DEPARTURE);
        proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, DEPARTURE, myintent, 0);
        LocationInfo locinfo = new LocationInfo();
        LocationDataBase dbDepart = new LocationDataBase(getBaseContext(), null, null, 1);
        int id = 0;
        do {
            id++;
            locinfo = dbDepart.getEachRowOfLocationDatabase(id);

            if (locinfo.get_address() != null && locinfo.get_address().contains(mSchedInfo.get_departure())) {
                mDeparture[0] = Double.parseDouble(locinfo.get_pointy());
                mDeparture[1] = Double.parseDouble(locinfo.get_pointx());
            }
        } while (!dbDepart.isGetDBFinish());

        Log.i(TAG, "addProximityAlert mDeparture[0]=" + mDeparture[0] + "mDeparture[1]=" + mDeparture[1]);
        locationManager.addProximityAlert(mDeparture[0], mDeparture[1], ALERT_RADIUS, -1, proximityIntent);

        /* get route and destination location point x and point y */
        new BackgroundProcess().execute("get route and destination");

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent removeintent = new Intent();
                PendingIntent premoveintent = PendingIntent.getBroadcast(ReportActivity.this, CURRENT, removeintent, 0);
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.removeProximityAlert(premoveintent);

                if (isDeparture[CLOSE]) {
                    tvReport.append("You are in the departure now");
                    if (enableTTS) {
                        Log.i(TAG, "TTS Client");
                        //ttsClient.setSpeechText("You are in the departure");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.setSpeechText("출발지에 있습니다.");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                        try {
                            sleep(SLEEP_SHORT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        tvReport.append(System.lineSeparator());
                        tvReport.append(System.lineSeparator());
                        svReport.fullScroll(View.FOCUS_DOWN);
                    }
                    tvReport.append("You need to go out within 20 minutes");
                    if (enableTTS) {
                        Log.i(TAG, "TTS Client");
                        //ttsClient.setSpeechText("You need to go out within 20 minutes");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.setSpeechText("20분 이내에 출발하셔야 합니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                        try {
                            sleep(SLEEP_SHORT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        tvReport.append(System.lineSeparator());
                        tvReport.append(System.lineSeparator());
                    }
                    svReport.fullScroll(View.FOCUS_DOWN);
                    //isCurrent[CLOSE] = false;
                    isDeparture[CLOSE] = false;
                } else {
                    //tvReport.append("You are not now in the departure");
                    tvReport.append("If you are not in the departure now,");
                    if (enableTTS) {
                        Log.i(TAG, "TTS Client");
                        //ttsClient.setSpeechText("You are in the departure");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        //ttsClient.setSpeechText("출발지에 있지 않습니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.setSpeechText("출발지에 있지 않으시면");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                        try {
                            sleep(SLEEP_SHORT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        tvReport.append(System.lineSeparator());
                        tvReport.append(System.lineSeparator());
                    }
                    svReport.fullScroll(View.FOCUS_DOWN);
                    tvReport.append("You can't use onschedule service");
                    if (enableTTS) {
                        Log.i(TAG, "TTS Client");
                        //ttsClient.setSpeechText("You are in the departure");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.setSpeechText("서비스를 이용하실 수 없습니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                        try {
                            sleep(SLEEP_SHORT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        tvReport.append(System.lineSeparator());
                        tvReport.append(System.lineSeparator());
                        svReport.fullScroll(View.FOCUS_DOWN);
                    }
                    tvReport.append("You need to go out within 20 minutes");
                    if (enableTTS) {
                        Log.i(TAG, "TTS Client");
                        //ttsClient.setSpeechText("You need to go out within 20 minutes");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.setSpeechText("20분 이내에 출발하셔야 합니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                        ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                        try {
                            sleep(SLEEP_SHORT);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        tvReport.append(System.lineSeparator());
                        tvReport.append(System.lineSeparator());
                    }
                    svReport.fullScroll(View.FOCUS_DOWN);
                }
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isDeparture[FAR]) {
                            splittime = mSchedInfo.get_leadtimetotal().split("\\s+");
                            if (splittime.length == 1) {
                                tvReport.append("You have only total lead time " + splittime[0] + " minutes");
                                if(enableTTS) {
                                    Log.i(TAG, "TTS Client");
                                    //ttsClient.setSpeechText("You have only total lead time " + splittime[0] + " minutes");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                    ttsClient.setSpeechText("스케쥴까지 총 소요 시간 " + splittime[0] + "분 남았습니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                    ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                    try {
                                        sleep(SLEEP_LONG);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    tvReport.append(System.lineSeparator());
                                    tvReport.append(System.lineSeparator());
                                }
                            } else {
                                tvReport.append("You have only total lead time " + splittime[0] + "hour " + splittime[1] + "minutes");
                                if(enableTTS) {
                                    Log.i(TAG, "TTS Client");
                                    //ttsClient.setSpeechText("You have only total lead time " + splittime[0] + "hour " + splittime[1] + "minutes");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                    ttsClient.setSpeechText("스케쥴까지 총 소요 시간 " + splittime[0] + "시간" + splittime[1] + "분 남았습니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                            ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                    try {
                                        sleep(SLEEP_LONG);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    tvReport.append(System.lineSeparator());
                                    tvReport.append(System.lineSeparator());
                                }
                            }

                            if (mRouteArray[ROUTE1 - 1][0] != null && !mRouteArray[ROUTE1 - 1][0].isEmpty()) {
                                tvReport.append("You need to run toward " + mRouteArray[ROUTE1 - 1][0]);
                                if (enableTTS) {
                                    Log.i(TAG, "TTS Client");
                                    //ttsClient.setSpeechText("You need to run toward " + mRouteArray[ROUTE1 - 1][0]);   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                    ttsClient.setSpeechText(mRouteArray[ROUTE1 - 1][0] + "으로 뛰어 가셔야 합니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                    ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                    try {
                                        sleep(SLEEP_SHORT);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                    tvReport.append(System.lineSeparator());
                                    tvReport.append(System.lineSeparator());
                                }
                                svReport.fullScroll(View.FOCUS_DOWN);
                            }

                            /* Test Code */
                            boolean test = false;
                            if (test) {
                                String where = "5";
                                boolean close = true;
                                if (close) {
                                    isRoute[Integer.valueOf(where) - 1][CLOSE] = true;
                                    isRoute[Integer.valueOf(where) - 1][FAR] = false;

                                    SubWayNameIDDataBase subwaydb = new SubWayNameIDDataBase(getBaseContext(), null, null, 1);
                                    SubwayNameIDInfo subwayNameIDInfo = new SubwayNameIDInfo();

                                    String[] subwaydbid = {"", "", "", ""};
                                    int i = 0;
                                    int l = 0;

                                    do {

                                        subwayNameIDInfo = subwaydb.getEachRowOfSubwayNameIDDatabase(i);

                                        Log.i(TAG, "mRouteArray[Integer.valueOf(where) - 1][2]=" + mRouteArray[Integer.valueOf(where) - 1][2] + "subwayNameIDInfo.getName()=" + subwayNameIDInfo.getName());
                                        if (subwayNameIDInfo.getName() != null && subwayNameIDInfo.getName().contains(mRouteArray[Integer.valueOf(where) - 1][2])) {
                                            subwaydbid[l] = subwayNameIDInfo.getSubwayid();
                                            l++;
                                            Log.i(TAG, "Get Route Next Station SubwayID name =" + subwayNameIDInfo.getName() + "ID=" + subwaydbid[l]);
                                        } /*else {
                                            subwaydbid[i] = "";
                                        }
                                        */
                                        i++;
                                    } while (!subwaydb.isGetDBFinish());

                                    JSONManager json = new JSONManager(getBaseContext());
                                    SubwayInfo[] subwayInfoArray;
                                    //matcher = pattern.matcher(mRouteArray[selection][0]);
                                    //json.getJSONDATA(mRouteArray[selection][2], false);
                                    json.getJSONDATA(mRouteArray[Integer.valueOf(where) - 1][0], false);
                                    subwayInfoArray = json.getSubwayJSONInfo();
                                    int k = 0;

                                    String[] arrivalTime = {"", "", "", ""}; // 몇 분 몇 초 후
                                    for (int j = 0; j < subwayInfoArray.length; j++) {
                                /* 경유지의 다음 전철역 정보에 해당하는 JSON 정보의 도착 정보 가져오기 */
                                        Log.i(TAG, "subwayInfoArray[" + j + "].getStatnTid()=" + subwayInfoArray[j].getStatnTid() + " subwaydbid=" + subwaydbid[j] + " subwayInfoArray[j].getBarvlDt()=" + subwayInfoArray[j].getBarvlDt());

                                        if (subwaydbid.length != 0) {
                                            for (int m = 0; m < subwaydbid.length; m++) {
                                                int sub;
                                                if (!subwaydbid[m].isEmpty() && !subwayInfoArray[j].getStatnTid().isEmpty()) {
                                                    Log.i(TAG, "subwaydbid[m]=" + subwaydbid[m] + "subwayInfoArray[" + j + "].getStatnTid()=" + subwayInfoArray[j].getStatnTid());
                                                    sub = Integer.valueOf(subwaydbid[m]) - Integer.valueOf(subwayInfoArray[j].getStatnTid());
                                                    if (sub == 0) {
                                                        index = m;
                                                        Log.i(TAG, "subwaydbid[" + m + "]=" + subwaydbid[m] + "subwayInfoArray[" + j + "].getStatnTid()=" + subwayInfoArray[j].getStatnTid());
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        Log.i(TAG, "subwaydbid[" + index + "]=" + subwaydbid[index] + "subwayInfoArray[" + j + "].getStatnTid()=" + subwayInfoArray[j].getStatnTid());
                                        if ((!subwaydbid[index].isEmpty() && subwayInfoArray[j].getStatnTid().contains(subwaydbid[index]))) {

                                            if (k < order.length) {
                                                Log.i(TAG, "write text view" + k);
                                                //tvReport.append("The " + order[k] + "subway for " + subwayNameIDInfo.getName() + " might arrive after " + subwayInfoArray[j].getArvlMsg2());
                                                tvReport.append("The " + order[k] + "subway for " + mRouteArray[Integer.valueOf(where) - 1][2] + " might arrive after " + subwayInfoArray[j].getArvlMsg2());
                                                if(enableTTS) {
                                                    Log.i(TAG, "TTS Client");
                                                    //ttsClient.setSpeechText("The " + order[k] + "subway for " + mRouteArray[Integer.valueOf(where) - 1][2] + " might arrive after " + subwayInfoArray[j].getArvlMsg2());   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                    ttsClient.setSpeechText(order[k] + mRouteArray[Integer.valueOf(where) - 1][2] + "향 열차가 " + subwayInfoArray[j].getArvlMsg2() + "에 도착합니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                    ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                                    try {
                                                        sleep(SLEEP_SHORT);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                    tvReport.append(System.lineSeparator());
                                                    tvReport.append(System.lineSeparator());
                                                }
                                                svReport.fullScroll(View.FOCUS_DOWN);
                                                arrivalTime[k] = subwayInfoArray[j].getBarvlDt();
                                                k++;
                                            }


                                            //if(subwayInfoArray[j].getBarvlDt().contains("분 후") || subwayInfoArray[j].getBarvlDt().contains("초 후"))
                                        }
                                    }

                                    Log.i(TAG, "mSchedInfo.get_time()=" + mSchedInfo.get_time());

                                    String[] time = mSchedInfo.get_time().split(":");
                                    int schedhour = Integer.valueOf(time[0]);
                                    int schedmin = Integer.valueOf(time[1]);
                                    Calendar cal = Calendar.getInstance();
                                    int currminute = cal.get(Calendar.MINUTE);
                                    //12 hour format
                                    int currhour = cal.get(Calendar.HOUR);
                                    //24 hour format
                                    int currhourofday = cal.get(Calendar.HOUR_OF_DAY);

                                    int timediff = 0;
                                    int timediffmin = 0;
                                    int timediffhour = 0;
                                    if (schedmin < currminute) {
                                        timediffmin = 60 - (currminute - schedmin);
                                        if (schedhour > currhourofday) {
                                            timediffhour = schedhour - currhourofday - 1;
                                        }
                                    } else {
                                        timediffmin = schedmin - currminute;
                                        if (schedhour > currhourofday) {
                                            timediffhour = schedhour - currhourofday;
                                        }
                                    }
                                    timediff = timediffhour * 60 * 60 * 1000 + timediffmin * 60 * 1000;

                                    long firstDifference = 0;
                                    long secondDifference = 0;
                                    if (!mRouteArray[Integer.valueOf(where) - 1][2].contains("Empty")) {
                                        /* 첫번째 지하철 도착 예정 시간 */
                                        Log.i(TAG, " arrivalTime[0]=" + arrivalTime[0]);
                                        String[] firstArrTime = arrivalTime[0].split("\\s+");
                                        long firstMillisecArrivalTime = 0;
                                        if (firstArrTime.length == 2) {
                                            firstArrTime[1] = firstArrTime[1].replaceAll("\\D+", ""); //초
                                            firstArrTime[0] = firstArrTime[0].replaceAll("\\D+", ""); //분
                                            if (!firstArrTime[0].isEmpty() && !firstArrTime[1].isEmpty()) {
                                                firstMillisecArrivalTime = Integer.valueOf(firstArrTime[1]) * 1000 + Integer.valueOf(firstArrTime[0]) * 60 * 1000;
                                            }
                                        } else if (firstArrTime.length == 1) {
                                            firstArrTime[0] = firstArrTime[0].replaceAll("\\D+", ""); //초
                                            if (!firstArrTime[0].isEmpty()) {
                                                firstMillisecArrivalTime = Integer.valueOf(firstArrTime[0]) * 1000;
                                            }

                                        }

                                        firstDifference = timediff - firstMillisecArrivalTime;
                                /*
                                if (firstArrTime[2] != null && !firstArrTime[2].isEmpty()) {
                                    firstArrTime[1] = firstArrTime[1].replaceAll("\\D+", ""); //초
                                    firstArrTime[0] = firstArrTime[0].replaceAll("\\D+", ""); //분
                                    firstMillisecArrivalTime = Integer.valueOf(firstArrTime[1]) * 1000 + Integer.valueOf(firstArrTime[0]) * 60 * 1000;

                                } else if (firstArrTime[1] != null && !firstArrTime[1].isEmpty()) {
                                    firstArrTime[0] = firstArrTime[0].replaceAll("\\D+", ""); //초
                                    firstMillisecArrivalTime = Integer.valueOf(firstArrTime[0]) * 1000;
                                }
                                */


                                        /* 두번째 지하철 도착 예정 시간 */
                                        Log.i(TAG, " arrivalTime[1]=" + arrivalTime[1]);
                                        String[] secondArrTime = arrivalTime[1].split("\\s+");
                                        long secondMillisecArrivalTime = 0;
                                        if (secondArrTime.length == 2) {
                                            secondArrTime[1] = secondArrTime[1].replaceAll("\\D+", ""); //초
                                            secondArrTime[0] = secondArrTime[0].replaceAll("\\D+", ""); //분
                                            if (!secondArrTime[0].isEmpty() && !secondArrTime[1].isEmpty()) {
                                                secondMillisecArrivalTime = Integer.valueOf(secondArrTime[1]) * 1000 + Integer.valueOf(secondArrTime[0]) * 60 * 1000;
                                            }

                                        } else if (secondArrTime.length == 1) {
                                            secondArrTime[0] = secondArrTime[0].replaceAll("\\D+", ""); //초
                                            if (!secondArrTime[0].isEmpty()) {
                                                secondMillisecArrivalTime = Integer.valueOf(secondArrTime[0]) * 1000;
                                            }
                                        }

                                        secondDifference = timediff - secondMillisecArrivalTime;
                                    }

                                    Log.i(TAG, " mSchedInfo.get_leadtimetotal()=" + mSchedInfo.get_leadtimetotal());
                                    String[] leadtimetotal = mSchedInfo.get_leadtimetotal().split("\\s+");
                                    long millisecondLeadTimeTotal = 0;
                                    if (leadtimetotal.length == 2) {
                                        if (!leadtimetotal[0].isEmpty() && !leadtimetotal[1].isEmpty()) {
                                            millisecondLeadTimeTotal = Integer.valueOf(leadtimetotal[0]) * 60 * 60 * 1000 + Integer.valueOf(leadtimetotal[1]) * 60 * 1000;
                                        }
                                    } else {
                                        if (!leadtimetotal[0].isEmpty()) {
                                            millisecondLeadTimeTotal = Integer.valueOf(leadtimetotal[0]) * 60 * 1000;
                                        }
                                    }

                                    String[] routeTime = mRouteArray[Integer.valueOf(where) - 1][1].split("\\s+");
                                    Log.i(TAG, " mRouteArray[Integer.valueOf(where) - 1][1]=" + mRouteArray[Integer.valueOf(where) - 1][1]);
                                    long millisecondRouteTime = 0;
                                    if (routeTime.length == 2) {
                                        if (!routeTime[0].isEmpty() && !routeTime[1].isEmpty()) {
                                            millisecondRouteTime = Integer.valueOf(routeTime[0]) * 60 * 1000 + Integer.valueOf(routeTime[1]) * 1000;
                                        }
                                    } else {
                                        if (!routeTime[0].isEmpty()) {
                                            millisecondRouteTime = Integer.valueOf(routeTime[0]) * 1000;
                                        }
                                    }

                                    if (!mRouteArray[Integer.valueOf(where) - 1][2].contains("Empty")) {
                                        if (secondDifference > (millisecondLeadTimeTotal - millisecondRouteTime)) {
                                            tvReport.append("You are not late. Keep walking");
                                            if(enableTTS) {
                                                Log.i(TAG, "TTS Client");
                                                ttsClient.setSpeechText("You are not late. Keep walking");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                                try {
                                                    sleep(SLEEP_SHORT);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                tvReport.append(System.lineSeparator());
                                                tvReport.append(System.lineSeparator());
                                            }
                                            svReport.fullScroll(View.FOCUS_DOWN);
                                        } else {
                                            if (firstDifference > (millisecondLeadTimeTotal - millisecondRouteTime)) {
                                                tvReport.append("You are not late. Keep walking");
                                                if(enableTTS) {
                                                    Log.i(TAG, "TTS Client");
                                                    ttsClient.setSpeechText("You are not late. Keep walking");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                    ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                                    try {
                                                        sleep(SLEEP_SHORT);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                    tvReport.append(System.lineSeparator());
                                                    tvReport.append(System.lineSeparator());
                                                }
                                                svReport.fullScroll(View.FOCUS_DOWN);
                                            } else {
                                                tvReport.append("You need to run toward " + mRouteArray[ROUTE1 - 1][0]);
                                                if(enableTTS) {
                                                    Log.i(TAG, "TTS Client");
                                                    //ttsClient.setSpeechText("You need to run toward " + mRouteArray[ROUTE1 - 1][0]);   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                    ttsClient.setSpeechText( mRouteArray[ROUTE1 - 1][0] + "으로 뛰어 가셔야 합니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                    ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                                    try {
                                                        sleep(SLEEP_SHORT);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                    tvReport.append(System.lineSeparator());
                                                    tvReport.append(System.lineSeparator());
                                                }
                                                svReport.fullScroll(View.FOCUS_DOWN);
                                            }
                                        }
                                    } else {
                                        if (timediff > millisecondRouteTime) {
                                            tvReport.append("You are not late. Keep walking");
                                            if(enableTTS) {
                                                ttsClient.setSpeechText("You are not late. Keep walking");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                                try {
                                                    sleep(SLEEP_SHORT);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                tvReport.append(System.lineSeparator());
                                                tvReport.append(System.lineSeparator());
                                            }
                                            svReport.fullScroll(View.FOCUS_DOWN);
                                        } else {
                                            tvReport.append("You need to run toward " + mRouteArray[ROUTE1 - 1][0]);
                                            if(enableTTS) {
                                                //ttsClient.setSpeechText("You need to run toward " + mRouteArray[ROUTE1 - 1][0]);   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                ttsClient.setSpeechText( mRouteArray[ROUTE1 - 1][0] + "으로 뛰어 가셔야 합니다");   //뉴톤톡 하고자 하는 문자열을 미리 세팅.
                                                ttsClient.play();       //세팅된 문자열을 합성하여 재생.
                                                try {
                                                    sleep(SLEEP_SHORT);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                tvReport.append(System.lineSeparator());
                                                tvReport.append(System.lineSeparator());
                                            }
                                            svReport.fullScroll(View.FOCUS_DOWN);
                                        }
                                    }

                                } else {
                                    isRoute[Integer.valueOf(where) - 1][CLOSE] = false;
                                    isRoute[Integer.valueOf(where) - 1][FAR] = true;
                                }
                                Intent removerouteintent = new Intent();
                                PendingIntent premoverouteintent = PendingIntent.getBroadcast(ReportActivity.this, (Integer.valueOf(where) - 1), removerouteintent, 0);
                                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                locationManager.removeProximityAlert(premoverouteintent);

                            }
                        }
                        /* test code */
                        if (false) {
                            Handler h = new Handler();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                                    myintent.putExtra("where", WHERE_ROUTE1);
                                    sendBroadcast(myintent);
                                    Handler h2 = new Handler();
                                    h2.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent myintent = new Intent(PROX_ALERT_INTENT);
                                            myintent.putExtra("where", WHERE_ROUTE5);
                                            sendBroadcast(myintent);
                                            Handler h3 = new Handler();
                                            h3.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                                                    myintent.putExtra("where", WHERE_DESTINATION);
                                                    sendBroadcast(myintent);
                                                }
                                            }, 10000);
                                        }
                                    }, 10000);
                                }
                            }, 10000);
                        }
                    }
                //}, 10000); // 출발지 10초 후 출발여부 확인
                }, 1200000); // 출발지 20분 후 출발여부 확인
            }
        }, 10000); // 출발지 위치 확인

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TextToSpeechManager.getInstance().finalizeLibrary();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        final TextView text = (TextView) findViewById(R.id.tvReport);
        CharSequence userText = text.getText();
        outState.putCharSequence("savedText", userText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final TextView text = (TextView)findViewById(R.id.tvReport);
        CharSequence userText = savedInstanceState.getCharSequence("savedText");
        text.setText(userText);

    }

    public static String[] convertStringToArray(String str) {
        String[] arr = str.split(strSeparator);
        return arr;
    }

    @Override
    public void onFinished() {
        int intSentSize = ttsClient.getSentDataSize();      //세션 중에 전송한 데이터 사이즈
        int intRecvSize = ttsClient.getReceivedDataSize();  //세션 중에 전송받은 데이터 사이즈

        final String strInacctiveText = "handleFinished() SentSize : " + intSentSize + "     RecvSize : " + intRecvSize;

        Log.i(TAG, strInacctiveText);

    }

    @Override
    public void onError(int i, String s) {

    }

    class BackgroundProcess extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (params[0].contains("get route and destination")) {
                LocationInfo info = new LocationInfo();
                LocationDataBase db = new LocationDataBase(getBaseContext(), null, null, 1);

                for (int k = 0; k < 5; k++) {
                    if (!mRouteArray[k][0].isEmpty()) {
                        LocationInfo locinfo = new LocationInfo();
                        int i = 0;
                        do {
                            i++;
                            locinfo = db.getEachRowOfLocationDatabase(i);

                            if (locinfo.get_address() != null && locinfo.get_address().contains(mRouteArray[k][0])) {
                                mPoint[k][0] = Double.parseDouble(locinfo.get_pointy());
                                mPoint[k][1] = Double.parseDouble(locinfo.get_pointx());
                                Log.i(TAG, "doInBackground mPoint[" + k + "][0]=" + mPoint[k][0] + "mPoint[" + k + "][1]=" + mPoint[k][1]);
                            }
                        } while (!db.isGetDBFinish());
                    }
                }
                LocationInfo locinfo = new LocationInfo();
                int i = 0;
                do {
                    i++;
                    locinfo = db.getEachRowOfLocationDatabase(i);

                    if (locinfo.get_address() != null && locinfo.get_address().contains(mSchedInfo.get_destination())) {
                        mDestination[0] = Double.parseDouble(locinfo.get_pointy());
                        mDestination[1] = Double.parseDouble(locinfo.get_pointx());
                        Log.i(TAG, "doInBackground mDestination[0]=" + mDestination[0] + "mDestination[1]=" + mDestination[1]);
                    }
                } while (!db.isGetDBFinish());

                return "set addProximityAlert for Route and Destination";
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("set addProximityAlert for Route and Destination")) {
                /*
                if (mDeparture[0] != 0) {
                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                    myintent.putExtra("where", WHERE_DEPARTURE);
                    proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, DEPARTURE, myintent, 0);
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.addProximityAlert(mDeparture[0], mDeparture[1], ALERT_RADIUS, -1, proximityIntent);
                    Log.i(TAG, "proximity alert of departure is set");
                }
                */
                if (mPoint[ROUTE1-1][0] != 0) {
                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                    myintent.putExtra("where", WHERE_ROUTE1);
                    proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, ROUTE1, myintent, 0);
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.addProximityAlert(mPoint[ROUTE1-1][0], mPoint[ROUTE1-1][1], ALERT_RADIUS, -1, proximityIntent);
                    Log.i(TAG, "proximity alert of route1 is set");
                }
                if (mPoint[ROUTE2-1][0] != 0) {
                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                    myintent.putExtra("where", WHERE_ROUTE2);
                    proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, ROUTE2, myintent, 0);
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.addProximityAlert(mPoint[ROUTE2-1][0], mPoint[ROUTE2-1][1], ALERT_RADIUS, -1, proximityIntent);
                    Log.i(TAG, "proximity alert of route2 is set");
                }
                if (mPoint[ROUTE3-1][0] != 0) {
                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                    myintent.putExtra("where", WHERE_ROUTE3);
                    proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, ROUTE3, myintent, 0);
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.addProximityAlert(mPoint[ROUTE3-1][0], mPoint[ROUTE3-1][1], ALERT_RADIUS, -1, proximityIntent);
                    Log.i(TAG, "proximity alert of route3 is set");
                }
                if (mPoint[ROUTE4-1][0] != 0) {
                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                    myintent.putExtra("where", WHERE_ROUTE4);
                    proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, ROUTE4, myintent, 0);
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.addProximityAlert(mPoint[ROUTE4-1][0], mPoint[ROUTE4-1][1], ALERT_RADIUS, -1, proximityIntent);
                    Log.i(TAG, "proximity alert of route4 is set");
                }
                if (mPoint[ROUTE5-1][0] != 0) {
                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                    myintent.putExtra("where", WHERE_ROUTE5);
                    proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, ROUTE5, myintent, 0);
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.addProximityAlert(mPoint[ROUTE5-1][0], mPoint[ROUTE5-1][1], ALERT_RADIUS, -1, proximityIntent);
                    Log.i(TAG, "proximity alert of route5 is set");
                }
                if (mDestination[0] != 0) {
                    Intent myintent = new Intent(PROX_ALERT_INTENT);
                    myintent.putExtra("where", WHERE_DESTINATION);
                    proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, DESTINATION, myintent, 0);
                    if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationManager.addProximityAlert(mDestination[0], mDestination[1], ALERT_RADIUS, -1, proximityIntent);
                    Log.i(TAG, "proximity alert of destination is set");
                }
            }

        }
    }

    class BackgroundSubwayInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            subwayInfo = new SubwayInfo(0);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
    /*
    private class VerifyLocation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Intent myintent = new Intent(PROX_ALERT_INTENT);
            myintent.putExtra("where", "current");
            proximityIntent = PendingIntent.getBroadcast(ReportActivity.this, 0, myintent, 0);

            Log.i(TAG, "currentLocation=" + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return TODO;
            }
            locationManager.addProximityAlert(currentLocation.getLatitude(), currentLocation.getLongitude(), 5, -1, proximityIntent);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
    */

    protected void onResume() {
        super.onResume();
        /*
        registerReceiver(new ProximityReceiver(), mIntentFilter);
        */
        /*
        registerReceiver(new ProximityReceiver() {
            @Override
            protected void onProximityEvent() {

            }
        }, mIntentFilter);
        */
    }
}
