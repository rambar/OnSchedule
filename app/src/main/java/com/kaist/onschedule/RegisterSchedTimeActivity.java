package com.kaist.onschedule;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class RegisterSchedTimeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "kaist_RegisterSchedTime";
    private TimePicker tp;
    private EditText etDepart;
    private EditText etDesti;
    private SchedDataBase mSchedDB;
    private String date = "";
    private SchedInfoDetail mInfoDetail;
    private LocationManager locationManager;
    private LocationListener locationListner;
    private String provider;
    private Location currentLocation;
    private boolean selectDeparture = false;
    private static boolean isFinishSetAddress = false;
    private String[][] mRouteArray = {{"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}};
    private String[][] mRouteArrayFromIntent = {{"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}, {"", "", "", ""}};
    private String[] mRoute = {"", "", "", "", ""};
    private String mleadTimeTotal = "";
    public static String strSeparator = "__,__";
    private int selection = 0;
    Button btnRoute1;
    Button btnRoute2;
    Button btnRoute3;
    Button btnRoute4;
    Button btnRoute5;
    Button btnLeadTimeTotal;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_sched_time);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* fix android.os.NetworkOnMainThreadException at android.os.StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java:1117) */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                Log.i(TAG, "Latitude=" + String.valueOf(location.getLatitude()) + " Longitude=" + String.valueOf(location.getLongitude()));
                final JSONManager j = new JSONManager(getBaseContext());
                j.getJSONDATA(null, true, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                if (selectDeparture) {
                    etDepart.setText(j.getAddress());
                } else {
                    Log.i(TAG, "onLocationChanged " + j.getAddress());
                    etDesti.setText(j.getAddress());
                }
                isFinishSetAddress = true;

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

        btnRoute1 = (Button) findViewById(R.id.btnRoute1);
        btnRoute2 = (Button) findViewById(R.id.btnRoute2);
        btnRoute3 = (Button) findViewById(R.id.btnRoute3);
        btnRoute4 = (Button) findViewById(R.id.btnRoute4);
        btnRoute5 = (Button) findViewById(R.id.btnRoute5);
        btnLeadTimeTotal = (Button) findViewById(R.id.btnTotalLeadTime);
        btnRoute1.setOnClickListener(this);
        btnRoute2.setOnClickListener(this);
        btnRoute3.setOnClickListener(this);
        btnRoute4.setOnClickListener(this);
        btnRoute5.setOnClickListener(this);
        btnLeadTimeTotal.setOnClickListener(this);

        Intent intent = getIntent();
        date = intent.getStringExtra("date");

        mInfoDetail = new SchedInfoDetail();
        mInfoDetail.setHour(intent.getStringExtra("hour"));
        mInfoDetail.setMinute(intent.getStringExtra("minute"));
        mInfoDetail.setDeparture(intent.getStringExtra("departure"));
        mInfoDetail.setDestination(intent.getStringExtra("destination"));
        mInfoDetail.setId(intent.getIntExtra("id", -1));
        Log.i(TAG, "id=" + mInfoDetail.getId());
        mInfoDetail.setRoute1(intent.getStringExtra("route1"));
        mInfoDetail.setRoute2(intent.getStringExtra("route2"));
        mInfoDetail.setRoute3(intent.getStringExtra("route3"));
        mInfoDetail.setRoute4(intent.getStringExtra("route4"));
        mInfoDetail.setRoute5(intent.getStringExtra("route5"));
        mInfoDetail.setLeadtimetotal(intent.getStringExtra("leadtimetotal"));
        if (mInfoDetail.getRoute1() != null && !mInfoDetail.getRoute1().isEmpty()) {
            btnRoute1.setBackgroundColor(Color.parseColor("#ffa500"));
            mRouteArrayFromIntent[0] = convertStringToArray(mInfoDetail.getRoute1());
            for (int l = 0; l < 4; l++) {
                Log.i(TAG, "mRouteArrayFromIntent[0][" + l + "]=" + mRouteArrayFromIntent[0][l]);
            }
        }
        if (mInfoDetail.getRoute2() != null && !mInfoDetail.getRoute2().isEmpty()) {
            btnRoute2.setBackgroundColor(Color.parseColor("#ffa500"));
            mRouteArrayFromIntent[1] = convertStringToArray(mInfoDetail.getRoute2());
            for (int l = 0; l < 4; l++) {
                Log.i(TAG, "mRouteArrayFromIntent[1][" + l + "]=" + mRouteArrayFromIntent[1][l]);
            }
        }
        if (mInfoDetail.getRoute3() != null &&!mInfoDetail.getRoute3().isEmpty()) {
            btnRoute3.setBackgroundColor(Color.parseColor("#ffa500"));
            mRouteArrayFromIntent[2] = convertStringToArray(mInfoDetail.getRoute3());
            for (int l = 0; l < 4; l++) {
                Log.i(TAG, "mRouteArrayFromIntent[2][" + l + "]=" + mRouteArrayFromIntent[2][l]);
            }
        }
        if (mInfoDetail.getRoute4() != null && !mInfoDetail.getRoute4().isEmpty()) {
            btnRoute4.setBackgroundColor(Color.parseColor("#ffa500"));
            mRouteArrayFromIntent[3] = convertStringToArray(mInfoDetail.getRoute4());
            for (int l = 0; l < 4; l++) {
                Log.i(TAG, "mRouteArrayFromIntent[3][" + l + "]=" + mRouteArrayFromIntent[3][l]);
            }
        }
        if (mInfoDetail.getRoute5() != null && !mInfoDetail.getRoute5().isEmpty()) {
            btnRoute5.setBackgroundColor(Color.parseColor("#ffa500"));
            mRouteArrayFromIntent[4] = convertStringToArray(mInfoDetail.getRoute5());
            for (int l = 0; l < 4; l++) {
                Log.i(TAG, "mRouteArrayFromIntent[4][" + l + "]=" + mRouteArrayFromIntent[4][l]);
            }
        }
        if (mInfoDetail.getLeadtimetotal() != null && !mInfoDetail.getLeadtimetotal().isEmpty()) {
            btnLeadTimeTotal.setBackgroundColor(Color.parseColor("#ffa500"));
            mleadTimeTotal = mInfoDetail.getLeadtimetotal();
        }


        tp = (TimePicker) findViewById(R.id.timePickerSched);
        if (mInfoDetail.getHour() != null && mInfoDetail.getMinute() != null) {
            tp.setCurrentHour(Integer.valueOf(mInfoDetail.getHour()));
            tp.setCurrentMinute(Integer.valueOf(mInfoDetail.getMinute()));
        }
        etDepart = (EditText) findViewById(R.id.etDepart);
        etDesti = (EditText) findViewById(R.id.etDesti);

        if (mInfoDetail.getDeparture() != null && mInfoDetail.getDestination() != null) {
            etDepart.setText(mInfoDetail.getDeparture());
            Log.i(TAG, "onCreate " + mInfoDetail.getDestination());
            etDesti.setText(mInfoDetail.getDestination());
        }

        Button btnGoDaum = (Button) findViewById(R.id.btnGoDaum);
        btnGoDaum.setOnClickListener(this);

        Button btnSaveSched = (Button) findViewById(R.id.btnSaveSched);
        btnSaveSched.setOnClickListener(this);

        Button btnMyLocation = (Button) findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(this);

        mSchedDB = new SchedDataBase(this, null, null, 1);
        Log.i(TAG, "SchedDatabase");
    }

    @Override
    public void onClick(final View v) {
        String time = String.valueOf(tp.getCurrentHour()) + ":" + String.valueOf(tp.getCurrentMinute());
        String departure = etDepart.getText().toString();
        String destination = etDesti.getText().toString();
        String[] pointCoord = {"", "", "", ""};
        Pattern pattern = Pattern.compile("\\s");
        Matcher matcher;
        String url;
        switch (v.getId()) {
            case R.id.btnGoDaum:
                if (departure.isEmpty() || destination.isEmpty()) {
                    Toast.makeText(this, "You need to type a departure and a destination", Toast.LENGTH_SHORT).show();
                    break;
                }
                /* JSON Data 처리 */
                JSONManager j = new JSONManager(this);

                for (int i = 0; i < 2; i++) {
                    if (i == 0) {
                        matcher = pattern.matcher(departure);
                        j.getJSONDATA(departure, matcher.find(), null, null);

                        pointCoord[i] = j.getPointY();
                        pointCoord[i + 1] = j.getPointX();
                    } else {
                        matcher = pattern.matcher(destination);
                        j.getJSONDATA(destination, matcher.find(), null, null);

                        pointCoord[2 * i] = j.getPointY();
                        pointCoord[2 * i + 1] = j.getPointX();
                    }
                }

                //Uri uri = Uri.parse("https://apis.daum.net/local/geo/addr2coord?apikey=216bc5446f914e4d836ad5e1b352884f&q=도곡역&output=json"); //주소 -> 좌표
                //Uri uri = Uri.parse("https://apis.daum.net/local/v1/search/keyword.json?apikey=216bc5446f914e4d836ad5e1b352884f&query=도곡역"); //키워드 -> 좌표
                Uri uri = Uri.parse(SchedConstant.DAUM_ROUTE + pointCoord[0] + "," + pointCoord[1] + "&ep=" + pointCoord[2] + "," + pointCoord[3] + SchedConstant.DAUM_ROUTE_PUBLIC);
                Intent i = new Intent(Intent.ACTION_VIEW, uri);
                i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;

            case R.id.btnSaveSched:
                if (departure.isEmpty() || destination.isEmpty() || mleadTimeTotal.isEmpty()) {
                    Toast.makeText(this, "You need to type a departure, a destination\n and total lead time", Toast.LENGTH_SHORT).show();
                    break;
                }

                Log.i(TAG, "mRoute[0]" + mRoute[0] + "mRoute[1]" + mRoute[1] + "mRoute[2]" + mRoute[2] + "mRoute[3]" + mRoute[3] + "mRoute[4]" + mRoute[4] + "mleadTimeTotal" + mleadTimeTotal);
                SchedInfo info = new SchedInfo(date, time, departure, destination, mRoute[0], mRoute[1], mRoute[2], mRoute[3], mRoute[4], mleadTimeTotal);
                //initRouteString();
                Log.i(TAG, "date=" + date + " time=" + time + " departure=" + departure + " destination=" + destination);
                if (mInfoDetail.getId() != -1) {
                    mSchedDB.updateSchedule(mInfoDetail.getId(), info);
                    Log.i(TAG, "updateSchedule");
                } else {
                    mSchedDB.addSchedule(info);
                    Log.i(TAG, "addSchedule");
                }

                /* Alarm 설정 */
                String[] d = date.split("-");
                Log.i(TAG, d[0] + " " + d[1] + " " + d[2] + " " + tp.getCurrentHour() +
                        " " + tp.getCurrentMinute());

                java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm:ss");

                java.util.Date date = null;
                try {
                    date = df.parse(time + ":00");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);

                Log.i(TAG, "mleadTimeTotal=" + mleadTimeTotal);

                matcher = pattern.matcher(mleadTimeTotal);
                if (matcher.find()) {
                    String[] splited = mleadTimeTotal.split("\\s+");
                    cal.add(Calendar.HOUR, -(Integer.valueOf(splited[0]) + (Integer.valueOf(splited[1]) + 20) / 60));
                    cal.add(Calendar.MINUTE, -((Integer.valueOf(splited[1]) + 20) % 60));
                } else {
                    cal.add(Calendar.HOUR, -((Integer.valueOf(mleadTimeTotal) + 20) / 60));
                    cal.add(Calendar.MINUTE, -((Integer.valueOf(mleadTimeTotal) + 20) % 60));
                }

                Log.i(TAG, "hour=" + cal.get(Calendar.HOUR_OF_DAY) + " min=" + cal.get(Calendar.MINUTE));
                setAlarm(Integer.valueOf(d[0]), Integer.valueOf(d[1]) - 1, Integer.valueOf(d[2]), tp.getCurrentHour(), tp.getCurrentMinute());
                break;

            case R.id.btnMyLocation:

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

                AlertDialog.Builder b = new AlertDialog.Builder(RegisterSchedTimeActivity.this);
                b.setMessage("Choose Departure or Destination");
                b.setNegativeButton("Departure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectDeparture = true;
                    }
                });
                b.setPositiveButton("Destination", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectDeparture = false;
                    }
                });
                b.show();
                break;

            case R.id.btnRoute1:
            case R.id.btnRoute2:
            case R.id.btnRoute3:
            case R.id.btnRoute4:
            case R.id.btnRoute5:
            case R.id.btnTotalLeadTime:
                LinearLayout layout;

                final EditText routeName = new EditText(RegisterSchedTimeActivity.this);
                final EditText leadTime = new EditText(RegisterSchedTimeActivity.this);
                final EditText nextStation = new EditText(RegisterSchedTimeActivity.this);
                final EditText transport = new EditText(RegisterSchedTimeActivity.this);
                final EditText leadTimeTotal = new EditText(RegisterSchedTimeActivity.this);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterSchedTimeActivity.this);

                if(v.getId() == R.id.btnTotalLeadTime) {
                    alertDialog.setTitle("Total Lead Time");
                    leadTimeTotal.setText(mleadTimeTotal);
                } else {
                    if(v.getId() == R.id.btnRoute1) {
                        selection = 0;
                    } else if(v.getId() == R.id.btnRoute2) {
                        selection = 1;
                    } else if(v.getId() == R.id.btnRoute3) {
                        selection = 2;
                    } else if(v.getId() == R.id.btnRoute4) {
                        selection = 3;
                    } else if(v.getId() == R.id.btnRoute5) {
                        selection = 4;
                    }
                    if(mRouteArrayFromIntent[selection] != null) {

                        for (int k = 0; k < 4 ; k++) {
                            if(!mRouteArrayFromIntent[selection][k].isEmpty()) {
                                if (k == 0) {
                                    routeName.setText(mRouteArrayFromIntent[selection][k]);
                                } else if (k == 1) {
                                    leadTime.setText(mRouteArrayFromIntent[selection][k]);
                                } else if (k == 2) {
                                    nextStation.setText(mRouteArrayFromIntent[selection][k]);
                                } else if (k == 3) {
                                    transport.setText(mRouteArrayFromIntent[selection][k]);
                                }
                            }
                        }
                    }

                    alertDialog.setTitle("ROUTE" + String.valueOf(selection + 1));
                }

                layout = new LinearLayout(this);
                if(v.getId() == R.id.btnTotalLeadTime) {
                    layout.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    layout.setLayoutParams(lp);

                    leadTimeTotal.setLayoutParams(lp);

                    leadTimeTotal.setHint("Total Lead Time for Departure\n(hour min)");
                    layout.addView(leadTimeTotal);
                } else {
                /* Dialog Multiple EditText View */
                    layout.setOrientation(LinearLayout.VERTICAL);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    layout.setLayoutParams(lp);

                    routeName.setLayoutParams(lp);
                    leadTime.setLayoutParams(lp);
                    nextStation.setLayoutParams(lp);
                    transport.setLayoutParams(lp);

                    routeName.setHint("Name Without 역");
                    leadTime.setHint("Lead Time For Route\n(min second)");
                    nextStation.setHint("If Route is Subway Station,\nWrite Next Station Name Without 역");
                    transport.setHint("If Route is Bus Station,\nWrite Bus #");
                    layout.addView(routeName);
                    layout.addView(leadTime);
                    layout.addView(nextStation);
                    layout.addView(transport);
                }
                alertDialog.setView(layout);

                alertDialog.setIcon(R.drawable.ic_sync_black_24dp);

                alertDialog.setPositiveButton("Set",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                JSONManager json = new JSONManager(getBaseContext());
                                Log.i(TAG, routeName.getText().toString());
                                if(v.getId() == R.id.btnTotalLeadTime) {
                                    mleadTimeTotal = leadTimeTotal.getText().toString();
                                } else {
                                    mRouteArray[selection][0] = routeName.getText().toString();
                                    mRouteArray[selection][1] = leadTime.getText().toString();
                                    mRouteArray[selection][2] = nextStation.getText().toString();
                                    mRouteArray[selection][3] = transport.getText().toString();
                                    mRoute[selection] = convertArrayToString(mRouteArray[selection]);
                                    Log.i(TAG, "mRoute[selection]" + mRoute[selection]);

                                    Pattern pattern = Pattern.compile("\\s");
                                    Matcher matcher;
                                    if (!routeName.getText().toString().isEmpty()) {
                                        /* JSON Data 처리 */
                                        JSONManager j = new JSONManager(getBaseContext());

                                        /* 지하철만 고려함. 버스 정류장은 추후 작업 필요*/
                                        matcher = pattern.matcher(mRouteArray[selection][0] + "역");
                                        j.getJSONDATA(mRouteArray[selection][0] + "역", matcher.find(), null, null);
                                        json.getJSONDATA(mRouteArray[selection][0], false);
                                    }
                                    if(!nextStation.getText().toString().isEmpty() || !nextStation.getText().toString().equals("Empty")) {
                                        json.getJSONDATA(mRouteArray[selection][2], false);
                                    }
                                }
                            }
                        });

                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
                break;

            default:
                break;
        }
    }

    public void initRouteString() {
        mRoute[0] = "";
        mRoute[1] = "";
        mRoute[2] = "";
        mRoute[3] = "";
        mRoute[4] = "";
    }

    public static String convertArrayToString(String[] array){
        String str = "";
        for (int i = 0;i<array.length; i++) {
            if(array[i].isEmpty()) {
                str = str + "Empty";
            } else {
                str = str+array[i];
            }
            // Do not append comma at the end of last element
            if(i<array.length - 1){
                str = str+strSeparator;
            }
        }
        return str;
    }

    public static String[] convertStringToArray(String str){
        String[] arr = str.split(strSeparator);
        return arr;
    }

    private void setAlarm(int year, int month, int day, int hour, int minute) {
        AlarmManager alarm_manager;
        PendingIntent pending_intent;

        // initialize our alarm manager
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // create an instance of a calendar
        final Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.clear();
        calendar.set(year, month, day, hour, minute, 0);

        // create an intent to the Alarm Receiver class
        Intent my_intent = new Intent(this, AlarmReceiver.class);

        /* Information to get the item of the database stored this date and time */
        my_intent.putExtra("date", date);
        my_intent.putExtra("time", String.valueOf(tp.getCurrentHour()) + ":" + String.valueOf(tp.getCurrentMinute()));

        // create a pending intent that delays the intent
        // until the specified calendar time
        pending_intent = PendingIntent.getBroadcast(RegisterSchedTimeActivity.this, 0,
                my_intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // set the alarm manager
        alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                pending_intent);

        Log.i(TAG, "setAlarm " + calendar.getTimeInMillis());
    }
}
