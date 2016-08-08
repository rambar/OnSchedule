package com.kaist.onschedule;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.parser.ParseException;


/**
 * Created by user on 2016-06-01.
 */
public class JSONManager {

    private static Context mContext;
    private static final String TAG = "kaist_JSON";
    private static String[] mPoint = {"",""};
    private static LocationPointXY point;
    private static SubwayInfo subwayInfo;
    private static SubwayInfo[] subwayInfoArray;
    public static String mPointX = "";
    public static String mPointY = "";
    public static String mAddr = "";

    public JSONManager(Context c) {
        mContext = c;
        point = new LocationPointXY();
        subwayInfo = new SubwayInfo(0);
    }

//    public static String get_JSONDATA(String url) {
      public static void getJSONDATA(String text , boolean whitespace, String latitude, String longitude) {
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

            if(entity != null) {
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
                    dbId =  db.getid(mAddr, mPointX, mPointY);
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

    public static SubwayInfo[] getJSONDATA(String nextstation , boolean whitespace) {
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

            if(entity != null) {
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

    public  String[] getPointArray() {

        String requestURI = SchedConstant.DAUM_ADDR_TO_COORD + SchedConstant.DAUM_MAP_API_KEY + "&q=야탑&output=json";
        parseAddressToCoord(requestURI, "");
        Log.i(TAG, mPoint[0] + " " + mPoint[1]);
        return mPoint;
    }

    public static void printAddressToCoord(String requestURI, String address) {

        try {
            address = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = openJSONReader(requestURI + address);
        printJSONData(bufferedReader);
        closeJSONReader(bufferedReader);
    }



    public static void parseAddressToCoord(String requestURI, String address) {

        try {
            address = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = openJSONReader(requestURI + address);
        parseJSONData(bufferedReader);
        closeJSONReader(bufferedReader);

    }



    public static void printJSONData(BufferedReader bufferedReader) {

        String inputLine;
        try {
            while ( (inputLine = bufferedReader.readLine()) != null ) {
                System.out.println(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseJSONData(BufferedReader bufferedReader) {

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject)jsonParser.parse(bufferedReader);
            JSONObject channelObject = (JSONObject)jsonObject.get("channel");
            System.out.println("================= " + "전체 검색결과" + " ================== ");
            System.out.println("\tresult : " + channelObject.get("result"));
            System.out.println("\tpageCount : " + channelObject.get("pageCount"));
            System.out.println("\ttitle : " + channelObject.get("title"));
            System.out.println("\ttotalCount : " + channelObject.get("totalCount"));
            System.out.println("\tdescription : " + channelObject.get("description"));

            JSONArray itemObjectList = (JSONArray)channelObject.get("item");

            int i = 1;
            for (Object tempObject : itemObjectList) {
                System.out.println("");
                System.out.println("================= " + i + "번째 검색결과" + " ================= ");
                tempObject = (JSONObject)tempObject;
                System.out.println("\ttitle : " + ((JSONObject)tempObject).get("title") );
                System.out.println("\tpoint_x : " + ((JSONObject) tempObject).get("point_x"));
                System.out.println("\tpoint_y : " + ((JSONObject) tempObject).get("point_y"));
                if(i == 1) {
                    mPoint[0] = (String) ((JSONObject)tempObject).get("point_x");
                    mPoint[1] = (String) ((JSONObject)tempObject).get("point_y");
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setPoints(BufferedReader br, boolean whitespace) {
        try {
            JSONParser p = new JSONParser();
            JSONObject obj = (JSONObject) p.parse(br);

            JSONObject channelObj = (JSONObject) obj.get("channel");
            JSONArray itemObj = (JSONArray) channelObj.get("item");

            int i = 1;
            for (Object tempObj : itemObj) {
                tempObj = (JSONObject)tempObj;
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

    public static SubwayInfo[] setNextStation(BufferedReader br, boolean whitespace) {
        try {
            JSONParser p = new JSONParser();
            JSONObject obj = (JSONObject) p.parse(br);

            JSONArray arrivalList = (JSONArray) obj.get("realtimeArrivalList");

            int i = 0;
            subwayInfoArray = new SubwayInfo[arrivalList.size()];
            for(int j = 0; j < arrivalList.size(); j++)
            {
                subwayInfoArray[j] = new SubwayInfo(j);
            }
            for (Object tempObj : arrivalList) {
                tempObj = (JSONObject)tempObj;

                subwayInfoArray[i].setStatnTid(String.valueOf(((JSONObject) tempObj).get("statnTid")));
                subwayInfoArray[i].setStatnId(String.valueOf(((JSONObject) tempObj).get("statnId")));
                subwayInfoArray[i].setStatnNm(String.valueOf(((JSONObject) tempObj).get("statnNm")));
                subwayInfoArray[i].setBarvlDt(String.valueOf(((JSONObject) tempObj).get("barvlDt")));
                subwayInfoArray[i].setArvlMsg2(String.valueOf(((JSONObject) tempObj).get("arvlMsg2")));
                subwayInfoArray[i].setArvlMsg3(String.valueOf(((JSONObject) tempObj).get("arvlMsg3")));
                subwayInfoArray[i].setArvlMsg3(String.valueOf(((JSONObject) tempObj).get("updnLine")));
                //if (i == 0) {

                    /*
                    subwayInfo.setStatnTid(String.valueOf(((JSONObject) tempObj).get("statnTid")));
                    subwayInfo.setStatnId(String.valueOf(((JSONObject) tempObj).get("statnId")));
                    subwayInfo.setStatnNm(String.valueOf(((JSONObject) tempObj).get("statnNm")));
                    subwayInfo.setArvlMsg2(String.valueOf(((JSONObject) tempObj).get("arvlMsg2")));
                    subwayInfo.setArvlMsg3(String.valueOf(((JSONObject) tempObj).get("arvlMsg3")));
                    subwayInfo.setArvlMsg3(String.valueOf(((JSONObject) tempObj).get("barvlDt")));
                    */

                    SubwayNameIDInfo subwayNameIDInfo = new SubwayNameIDInfo(subwayInfoArray[i].getStatnNm(), subwayInfoArray[i].getStatnId());
                    SubWayNameIDDataBase subwaydb = new SubWayNameIDDataBase(mContext, null, null, 1);
                    String dbsubwayId = "";
                    try {
                        dbsubwayId =  subwaydb.getid(subwayInfoArray[i].getStatnNm(), subwayInfoArray[i].getStatnId());
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

                /*
                subwayInfo.setStatnTid(String.valueOf(((JSONObject) tempObj).get("statnTid")));
                subwayInfo.setStatnId(String.valueOf(((JSONObject) tempObj).get("statnId")));
                subwayInfo.setStatnNm(String.valueOf(((JSONObject) tempObj).get("statnNm")));
                subwayInfo.setBarvlDt(String.valueOf(((JSONObject) tempObj).get("barvlDt")));
                subwayInfo.setArvlMsg2(String.valueOf(((JSONObject) tempObj).get("arvlMsg2")));
                subwayInfo.setArvlMsg3(String.valueOf(((JSONObject) tempObj).get("arvlMsg3")));
                */
                /*
                subwayInfoArray[i].setStatnTid(String.valueOf(((JSONObject) tempObj).get("statnTid")));
                subwayInfoArray[i].setStatnId(String.valueOf(((JSONObject) tempObj).get("statnId")));
                subwayInfoArray[i].setStatnNm(String.valueOf(((JSONObject) tempObj).get("statnNm")));
                subwayInfoArray[i].setBarvlDt(String.valueOf(((JSONObject) tempObj).get("barvlDt")));
                subwayInfoArray[i].setArvlMsg2(String.valueOf(((JSONObject) tempObj).get("arvlMsg2")));
                subwayInfoArray[i].setArvlMsg3(String.valueOf(((JSONObject) tempObj).get("arvlMsg3")));
                */
                //subwayInfoArray[i] = subwayInfo;
                Log.i(TAG, "setNextStation " + i + " " + subwayInfoArray[i].getStatnTid() + " " + subwayInfoArray[i].getStatnId() + " " + subwayInfoArray[i].getArvlMsg2() +  " " + subwayInfoArray[i].getArvlMsg3() + " " + subwayInfoArray[i].getBarvlDt() + " " + subwayInfoArray[i].getUpdnLine());
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

    public SubwayInfo[] getSubwayJSONInfo() {
        return subwayInfoArray;
    }

    public static void setAddress(BufferedReader br, boolean whitespace) {
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

    public static BufferedReader openJSONReader(String requestURI) {

        URL requestURL = null;
        try {
            requestURL = new URL(requestURI);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        URLConnection urlConnection = null;
        try {
            urlConnection = requestURL.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader( new InputStreamReader(urlConnection.getInputStream()) );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bufferedReader;
    }



    public static boolean closeJSONReader(BufferedReader bufferedReader) {

        boolean closeJSONReaderResult = true;
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            closeJSONReaderResult = false;
        }

        return closeJSONReaderResult;
    }


    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

            Log.i(TAG, "Get "+ result);
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        Log.i(TAG, "convertInputStreamToString " + result);

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        return true;
        /*
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
            */
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.i(TAG, "onPostExecute " + result);
        }
    }
}


