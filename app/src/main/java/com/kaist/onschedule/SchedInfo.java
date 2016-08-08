package com.kaist.onschedule;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by user on 2016-05-19.
 */
public class SchedInfo {
    private static final String TAG = "kaist_SchedInfo";
    /* DataBase attribute */
    private int _id;
    private String _date;
    private String _time;
    private String _departure;
    private String _destination;
    private int _numofcp;
    private String _cponfoot1;
    private String _cponfoot2;
    private String _cponfoot3;
    private String _cponfoot4;
    private String _cponfoot5;
    private String _leadtimetotal;
    private final int NAME = 0;
    private final int LEADTIME = 1;
    private final int TRANSPORT = 2;


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_date() {
        return _date;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public String get_time() {
        return _time;
    }

    public void set_time(String _time) {
        this._time = _time;
    }

    public String get_departure() {
        return _departure;
    }

    public void set_departure(String _departure) {
        this._departure = _departure;
    }

    public String get_destination() {
        return _destination;
    }

    public void set_destination(String _destination) {
        this._destination = _destination;
    }

    public String get_leadtimetotal() {
        return _leadtimetotal;
    }

    public void set_leadtimetotal(String _leadtimetotal) {
        this._leadtimetotal = _leadtimetotal;
    }

    public int get_numofcp() {
        return _numofcp;
    }

    public void set_numofcp(int _numofcp) {
        this._numofcp = _numofcp;
    }

    public String get_cponfoot1() {
        return _cponfoot1;
    }

    public void set_cponfoot1(String _cponfoot1) {
        this._cponfoot1 = _cponfoot1;
    }

    public String get_cponfoot2() {
        return _cponfoot2;
    }

    public void set_cponfoot2(String _cponfoot2) {
        this._cponfoot2 = _cponfoot2;
    }

    public String get_cponfoot3() {
        return _cponfoot3;
    }

    public void set_cponfoot3(String _cponfoot3) {
        this._cponfoot3 = _cponfoot3;
    }

    public String get_cponfoot4() {
        return _cponfoot4;
    }

    public void set_cponfoot4(String _cponfoot4) {
        this._cponfoot4 = _cponfoot4;
    }

    public String get_cponfoot5() {
        return _cponfoot5;
    }

    public void set_cponfoot5(String _cponfoot5) {
        this._cponfoot5 = _cponfoot5;
    }

    /* Variables */
    private int mNumOfCheckPointsOnFoot;
    private String mChecPoint[][];

    public SchedInfo () {

    }
    /*
    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_date() {
        return _date;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public String get_time() {
        return _time;
    }

    public void set_time(String _time) {
        this._time = _time;
    }

    public String get_departure() {
        return _departure;
    }

    public void set_departure(String _departure) {
        this._departure = _departure;
    }

    public String get_destination() {
        return _destination;
    }

    public void set_destination(String _destination) {
        this._destination = _destination;
    }

    public String get_leadtime() {
        return _leadtime;
    }

    public void set_leadtime(String _leadtime) {
        this._leadtime = _leadtime;
    }

    public int get_numofcp() {
        return _numofcp;
    }

    public void set_numofcp(int _numofcp) {
        this._numofcp = _numofcp;
    }

    public String get_cponfoot1() {
        return _cponfoot1;
    }

    public void set_cponfoot1(String _cponfoot1) {
        this._cponfoot1 = _cponfoot1;
    }

    public String get_cponfoot2() {
        return _cponfoot2;
    }

    public void set_cponfoot2(String _cponfoot2) {
        this._cponfoot2 = _cponfoot2;
    }

    public String get_cponfoot3() {
        return _cponfoot3;
    }

    public void set_cponfoot3(String _cponfoot3) {
        this._cponfoot3 = _cponfoot3;
    }

    public String get_cponfoot4() {
        return _cponfoot4;
    }

    public void set_cponfoot4(String _cponfoot4) {
        this._cponfoot4 = _cponfoot4;
    }

    public String get_cponfoot5() {
        return _cponfoot5;
    }

    public void set_cponfoot5(String _cponfoot5) {
        this._cponfoot5 = _cponfoot5;
    }
*/
    public void setCheckPointsOnFoot(String checkpoint) {
        /* TO DO */
    }

    public String[][] getChecPointsOnFoot() {
        if (mChecPoint != null) {
            return mChecPoint;
        }
        return null;
    }

    public void printSchedInfo() {
        Log.i(TAG, _date + "," + _time + "," + _departure + "," + _destination);
    }
    public SchedInfo(String date, String time, String departure, String destination){
        this._date = date;
        this._time = time;
        this._departure = departure;
        this._destination = destination;
    }

    public SchedInfo(String date, String time, String departure, String destination, String cponfoot1, String cponfoot2, String cponfoot3, String cponfoot4, String cponfoot5, String leadtimetotal){
        this._date = date;
        this._time = time;
        this._departure = departure;
        this._destination = destination;
        this._cponfoot1 = cponfoot1;
        this._cponfoot2 = cponfoot2;
        this._cponfoot3 = cponfoot3;
        this._cponfoot4 = cponfoot4;
        this._cponfoot5 = cponfoot5;
        this._leadtimetotal = leadtimetotal;
    }
}
