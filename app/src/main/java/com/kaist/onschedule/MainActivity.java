package com.kaist.onschedule;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "kaist_MainActivity";
    LocationManager locationManager;
    LocationListener locationListner;
    Location currentLocation;
    PendingIntent proximityIntent;
    AlertReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /* fix android.os.NetworkOnMainThreadException at android.os.StrictMode$AndroidBlockGuardPolicy.onNetwork(StrictMode.java:1117) */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Button btnShow = (Button) findViewById(R.id.btnShow);
        Button btnReg = (Button) findViewById(R.id.btnReg);
        Button btnSet = (Button) findViewById(R.id.btnSet);
        Button btnExit = (Button) findViewById(R.id.btnExit);
        //Button btnTest = (Button) findViewById(R.id.btnURI);


        btnShow.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnSet.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        //btnTest.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                Log.i(TAG, "Latitude=" + String.valueOf(location.getLatitude()) + " Longitude=" + String.valueOf(location.getLongitude()));

                /*
                AlertDialog.Builder b = new AlertDialog.Builder(RegisterSchedTimeActivity.this);
                b.setMessage("Choose Departure or Destination");
                b.setPositiveButton("Departure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etDepart.setText(j.getAddress());
                    }
                });
                b.setNegativeButton("Destination", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etDesti.setText(j.getAddress());
                    }
                });
                */
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btnShow:
                i = new Intent(MainActivity.this, com.kaist.onschedule.ShowSchedActivity.class);
                i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
            case R.id.btnReg:
                i = new Intent(MainActivity.this, com.kaist.onschedule.RegisterSchedDateActivity.class);
                i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
            case R.id.btnSet:
                i = new Intent(MainActivity.this, com.kaist.onschedule.SettingsSchedActivity.class);
                i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
            case R.id.btnExit:
                finish();
                ActivityCompat.finishAffinity(this);
                System.runFinalizersOnExit(true);
                System.exit(0);


            /*
            case R.id.btnURI:

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
                */
                /*
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListner);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListner);
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (currentLocation != null) {
                    locationManager.removeUpdates(locationListner);
                }
                */
                /*
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
                                */
                                /*
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                                */
                                /*
                            }
                        }
                        */
                        // if GPS Enabled get lat/long using GPS Services
                        /*
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
                                    */
                                    /*
                                    if (currentLocation != null) {
                                        latitude = currentLocation.getLatitude();
                                        longitude = currentLocation.getLongitude();
                                    }
                                    */

                                    /*
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


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
                Log.i(TAG, "addProximityAlert");
                */
                /* TO DO */
        /* If current location is not registered departure, Toast warning message */
                // create an intent to the Alarm Receiver class
        /*
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        */
        /*
        Intent proximity_intent = new Intent(this, ProximityReceiver.class);

        // create a pending intent that delays the intent
        // until the specified calendar time
        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, proximity_intent, PendingIntent.FLAG_UPDATE_CURRENT);
        lm.addProximityAlert(Double.valueOf(info.get_pointy()), Double.valueOf(info.get_pointx()), 5, -1, pi);
        */

        /*
        Intent proximity_intent = new Intent(PROXIMITY_INTENT_ACTION);
        intent.putExtra(ProximityReceiver.EVENT_ID_INTENT_EXTRA, 5);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, proximity_intent, PendingIntent.FLAG_CANCEL_CURRENT);

        lm.addProximityAlert(Double.valueOf(info.get_pointy()), Double.valueOf(info.get_pointx()), 100, -1, pendingIntent);
        */
                /*
                IntentFilter filter = new IntentFilter("com.kaist.onschedule.ProximityReceiver.action.PROXIMITY_ALERT");
                registerReceiver(new ProximityReceiver(getBaseContext()), filter);
                //LocationManager lm;
                //lm = (LocationManager) getSystemService(LOCATION_SERVICE);
                Intent proxi_intent = new Intent("com.kaist.onschedule.ProximityReceiver.action.PROXIMITY_ALERT");
                PendingIntent proximityIntent = PendingIntent.getBroadcast(this, 0, proxi_intent, 0);
                */
                /*
                receiver = new AlertReceiver();
                IntentFilter filter = new IntentFilter("kr");
                registerReceiver(receiver, filter);

                Intent intent = new Intent("kr");
                proximityIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

                Log.i(TAG, "currentLocation=" + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
                locationManager.addProximityAlert(currentLocation.getLatitude(), currentLocation.getLongitude(), 5, -1, proximityIntent);

                //Uri uri = Uri.parse("http://swopenAPI.seoul.go.kr/api/subway/sample/json/realtimeStationArrival/0/5/매봉"); //
                //Uri uri = Uri.parse("http://openAPI.seoul.go.kr:8088/78616f4452616c6c3639716446454e/json/SearchArrivalInfoByIDService/1/5/1004/1/3/"); //주소 -> 좌표
                //Uri uri = Uri.parse("https://apis.daum.net/local/v1/search/keyword.json?apikey=216bc5446f914e4d836ad5e1b352884f&query=도곡역"); //키워드 -> 좌표
                //i = new Intent(Intent.ACTION_VIEW, uri);
                //startActivity(i);

                //getSttnAcctoSpcifyRouteBusArvlPrearngeInfoList();
                break;
            */
            default:
                break;
        }
    }

    void getSttnAcctoSpcifyRouteBusArvlPrearngeInfoList() {
        StringBuilder urlBuilder = new StringBuilder("http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getSttnAcctoSpcifyRouteBusArvlPrearngeInfoList"); /*URL*/
        try {
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + SchedConstant.GOVERN_DATA_API_KEY); /*Service Key*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            urlBuilder.append("&" + URLEncoder.encode("cityCode", "UTF-8") + "=" + URLEncoder.encode("25", "UTF-8")); /*도시코드*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            urlBuilder.append("&" + URLEncoder.encode("routeId", "UTF-8") + "=" + URLEncoder.encode("DJB30300038ND", "UTF-8")); /*노선ID*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            urlBuilder.append("&" + URLEncoder.encode("nodeId", "UTF-8") + "=" + URLEncoder.encode("DJB8001169ND", "UTF-8")); /*정류소ID*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("999", "UTF-8")); /*검색건수*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        URL url = null;
        try {
            url = new URL(urlBuilder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        conn.setRequestProperty("Content-type", "application/json");
        try {
            System.out.println("Response code: " + conn.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader rd = null;
        try {
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.disconnect();
        Log.i(TAG, sb.toString());
    }

    private void getCtyCodeList() {
        StringBuilder urlBuilder = new StringBuilder("http://openapi.tago.go.kr/openapi/service/ArvlInfoInqireService/getCtyCodeList"); /*URL*/
        try {
            urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + SchedConstant.GOVERN_DATA_API_KEY); /*Service Key*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode("999", "UTF-8")); /*검색건수*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        URL url = null;
        try {
            url = new URL(urlBuilder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        conn.setRequestProperty("Content-type", "application/json");
        try {
            System.out.println("Response code: " + conn.getResponseCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader rd = null;
        try {
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.disconnect();
        System.out.println(sb.toString());
    }

    class AlertReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int count = 0;
            //System.out.println("In onRecevice" + count++);
            boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
            if (isEntering)
                Toast.makeText(context, " 지점에 접근중입니다..", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, " 지점에서 벗어납니다..", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    public void onStop() {
        super.onStop();
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
        locationManager.removeUpdates(locationListner);
        unregisterReceiver(receiver);
    }
    */
}
