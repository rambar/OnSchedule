package com.kaist.onschedule;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.parser.ParseException;

public class JSONSubwayManager implements JSONInterface {

    private static Context mContext;
    private static final String TAG = "kaist_JSON";
    private static String[] mPoint = {"", ""};
    private static LocationPointXY point;
    private static SubwayInfo subwayInfo;
    private static SubwayInfo[] subwayInfoArray;
    public static String mPointX = "";
    public static String mPointY = "";
    public static String mAddr = "";

    public JSONSubwayManager(Context c) {
        mContext = c;
        point = new LocationPointXY();
        subwayInfo = new SubwayInfo(0);
    }

    public void getJSONDATA(String text, boolean whitespace, String latitude, String longitude) {
        //StringBuffer json = new StringBuffer();
        String url = "";
        HttpClient httpclient = new DefaultHttpClient();

        try {
            if (text == null && !latitude.isEmpty() && !longitude.isEmpty()) {
                url = SchedConstant.DAUM_COORD_TO_ADDR + SchedConstant.DAUM_MAP_API_KEY + "&longitude=" + longitude + "&latitude=" + latitude + SchedConstant.DAUM_COORD_TO_ADDR_TAIL;

            } else {
                if (whitespace) {
                    url = SchedConstant.DAUM_ADDR_TO_COORD + SchedConstant.DAUM_MAP_API_KEY + "&q=" + text + "&output=json";
                    url = url.replace(" ", "%20");
                } else {
                    url = SchedConstant.DAUM_KEYWORD_TO_COORD + SchedConstant.DAUM_MAP_API_KEY + "&query=" + text;
                }
            }

            Log.i(TAG, url);
            HttpGet httpget = new HttpGet(url);

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                if (text != null) {
                    setPoints(rd, whitespace);
                } else {
                    setAddress(rd, whitespace);
                }

                /* Add Location Database */
                Log.i(TAG, "Location Database");
                LocationInfo info = new LocationInfo(mAddr, mPointX, mPointY);
                LocationDataBase db = new LocationDataBase(mContext, null, null, 1);
                String dbId = "";
                try {
                    dbId = db.getid(mAddr, mPointX, mPointY);
                    if (dbId == null) {
                        db.addLocation(info);
                        Log.i(TAG, "Add New Location Database");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, point.getPointX() + " " + point.getPointY() + " " + point.getAddress());
            }
            httpget.abort();
            httpclient.getConnectionManager().shutdown();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        //return json.toString();
    }

    public SubwayInfo[] getJSONDATA(String nextstation, boolean whitespace) {
        //StringBuffer json = new StringBuffer();
        String url = "";
        HttpClient httpclient = new DefaultHttpClient();
        SubwayInfo[] info = new SubwayInfo[0];

        try {
            if (nextstation != null) {
                url = SchedConstant.SEOUL_METRO_REAL_TIME_STATION_ARRIVAL + nextstation;
                if (whitespace) {
                    url = url.replace(" ", "%20");
                }
            }

            Log.i(TAG, url);
            HttpGet httpget = new HttpGet(url);

            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                /*
                String line = "";
                while ((line = rd.readLine()) != null) {
                    json.append(line);
                }
                */
                if (nextstation != null) {
                    info = setNextStation(rd, whitespace);
                }
            }
            httpget.abort();
            httpclient.getConnectionManager().shutdown();
            return info;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        return null;

        //return json.toString();
    }

    private static void setPoints(BufferedReader br, boolean whitespace) {
        try {
            JSONParser p = new JSONParser();
            JSONObject obj = (JSONObject) p.parse(br);

            JSONObject channelObj = (JSONObject) obj.get("channel");
            JSONArray itemObj = (JSONArray) channelObj.get("item");

            int i = 1;
            for (Object tempObj : itemObj) {
                tempObj = (JSONObject) tempObj;
                if (i == 1) {

                    if (whitespace) {
                        point.setPointX(String.valueOf(((JSONObject) tempObj).get("point_x")));
                        point.setPointY(String.valueOf(((JSONObject) tempObj).get("point_y")));
                    } else {
                        point.setPointX(String.valueOf(((JSONObject) tempObj).get("longitude")));
                        point.setPointY(String.valueOf(((JSONObject) tempObj).get("latitude")));
                    }
                    point.setAddress(String.valueOf(((JSONObject) tempObj).get("title")));
                    //point.setPointX(String.valueOf(((JSONObject) tempObj).get("point_x")));
                    //point.setPointY(String.valueOf(((JSONObject) tempObj).get("point_y")));
                    mAddr = point.getAddress();
                    mPointX = point.getPointX();
                    mPointY = point.getPointY();
                }
                i++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static SubwayInfo[] setNextStation(BufferedReader br, boolean whitespace) {
        try {
            JSONParser p = new JSONParser();
            JSONObject obj = (JSONObject) p.parse(br);

            JSONArray arrivalList = (JSONArray) obj.get("realtimeArrivalList");

            int i = 0;
            subwayInfoArray = new SubwayInfo[arrivalList.size()];
            for (int j = 0; j < arrivalList.size(); j++) {
                subwayInfoArray[j] = new SubwayInfo(j);
            }
            for (Object tempObj : arrivalList) {
                tempObj = (JSONObject) tempObj;

                subwayInfoArray[i].setStatnTid(String.valueOf(((JSONObject) tempObj).get("statnTid")));
                subwayInfoArray[i].setStatnId(String.valueOf(((JSONObject) tempObj).get("statnId")));
                subwayInfoArray[i].setStatnNm(String.valueOf(((JSONObject) tempObj).get("statnNm")));
                subwayInfoArray[i].setBarvlDt(String.valueOf(((JSONObject) tempObj).get("barvlDt")));
                subwayInfoArray[i].setArvlMsg2(String.valueOf(((JSONObject) tempObj).get("arvlMsg2")));
                subwayInfoArray[i].setArvlMsg3(String.valueOf(((JSONObject) tempObj).get("arvlMsg3")));
                subwayInfoArray[i].setArvlMsg3(String.valueOf(((JSONObject) tempObj).get("updnLine")));
                //if (i == 0) {

                SubwayNameIDInfo subwayNameIDInfo = new SubwayNameIDInfo(subwayInfoArray[i].getStatnNm(), subwayInfoArray[i].getStatnId());
                SubWayNameIDDataBase subwaydb = new SubWayNameIDDataBase(mContext, null, null, 1);
                String dbsubwayId = "";
                try {
                    dbsubwayId = subwaydb.getid(subwayInfoArray[i].getStatnNm(), subwayInfoArray[i].getStatnId());
                    Log.i(TAG, "subwayInfo.getStatnNm()=" + subwayInfoArray[i].getStatnNm() + "subwayInfo.getStatnId()=" + subwayInfoArray[i].getStatnId());
                    if (dbsubwayId == null) {
                        Log.i(TAG, "subwayNameIDInfo.getName()=" + subwayNameIDInfo.getName() + " subwayNameIDInfo.getSubwayid()=" + subwayNameIDInfo.getSubwayid());
                        subwaydb.addSubwayNameID(subwayNameIDInfo);
                        Log.i(TAG, "Add New Subway Name ID Database");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                //}

                //subwayInfoArray[i] = subwayInfo;
                Log.i(TAG, "setNextStation " + i + " " + subwayInfoArray[i].getStatnTid() + " " + subwayInfoArray[i].getStatnId() + " " + subwayInfoArray[i].getArvlMsg2() + " " + subwayInfoArray[i].getArvlMsg3() + " " + subwayInfoArray[i].getBarvlDt() + " " + subwayInfoArray[i].getUpdnLine());
                i++;
            }

            //Log.i(TAG, "setNextStation " + subwayInfo.getStatnTid() + " " + subwayInfo.getStatnId() + " " + subwayInfo.getArvlMsg2() +  " " + subwayInfo.getArvlMsg3() + " " + subwayInfo.getBarvlDt());
            return subwayInfoArray;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SubwayInfo[] getTransportationJSONInfo() {
        return subwayInfoArray;
    }

    private static void setAddress(BufferedReader br, boolean whitespace) {
        try {
            JSONParser p = new JSONParser();
            JSONObject obj = (JSONObject) p.parse(br);

            point.setAddress(String.valueOf(obj.get("fullName")));
            point.setPointX(String.valueOf(obj.get("x")));
            point.setPointY(String.valueOf(obj.get("y")));

            mAddr = point.getAddress();
            mPointX = point.getPointX();
            mPointY = point.getPointY();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPointX() {
        return mPointX;
    }

    public String getPointY() {
        return mPointY;
    }

    public String getAddress() {
        return mAddr;
    }
}


