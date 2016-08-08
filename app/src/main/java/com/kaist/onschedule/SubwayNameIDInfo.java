package com.kaist.onschedule;

/**
 * Created by user on 2016-06-08.
 */
public class SubwayNameIDInfo {
    public String name;
    public String subwayid;

    public SubwayNameIDInfo() {

    }

    public SubwayNameIDInfo(String name, String subwayid) {
        this.name = name;
        this.subwayid = subwayid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubwayid() {
        return subwayid;
    }

    public void setSubwayid(String subwayid) {
        this.subwayid = subwayid;
    }
}
