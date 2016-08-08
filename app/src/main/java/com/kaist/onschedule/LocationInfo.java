package com.kaist.onschedule;

import android.util.Log;

/**
 * Created by user on 2016-06-05.
 */
public class LocationInfo {
    private static final String TAG = "kaist_LocationInfo";
    /* DataBase attribute */
    private int _id;
    private String _address;
    private String _pointx;
    private String _pointy;

    public LocationInfo() {
    }

    public LocationInfo(String address, String pointx, String pointy){
        this._address = address;
        this._pointx = pointx;
        this._pointy = pointy;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_address() {
        return _address;
    }

    public void set_address(String _address) {
        this._address = _address;
    }

    public String get_pointx() {
        return _pointx;
    }

    public void set_pointx(String _pointx) {
        this._pointx = _pointx;
    }

    public String get_pointy() {
        return _pointy;
    }

    public void set_pointy(String _pointy) {
        this._pointy = _pointy;
    }
}
