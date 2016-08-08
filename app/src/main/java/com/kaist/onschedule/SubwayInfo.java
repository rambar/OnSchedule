package com.kaist.onschedule;

/**
 * Created by user on 2016-06-04.
 */
public class SubwayInfo {
    public String statnTid;
    public String statnId;
    public String statnNm;
    public String barvlDt;
    public String arvlMsg2;
    public String arvlMsg3;

    public String getUpdnLine() {
        return updnLine;
    }

    public void setUpdnLine(String updnLine) {
        this.updnLine = updnLine;
    }

    public String updnLine;

    public SubwayInfo(int j) {

    }

    public String getStatnTid() {
        return statnTid;
    }

    public void setStatnTid(String statnTid) {
        this.statnTid = statnTid;
    }

    public String getStatnId() {
        return statnId;
    }

    public void setStatnId(String statnId) {
        this.statnId = statnId;
    }

    public String getStatnNm() {
        return statnNm;
    }

    public void setStatnNm(String statnNm) {
        this.statnNm = statnNm;
    }

    public String getBarvlDt() {
        return barvlDt;
    }

    public void setBarvlDt(String barvlDt) {
        this.barvlDt = barvlDt;
    }

    public String getArvlMsg2() {
        return arvlMsg2;
    }

    public void setArvlMsg2(String arvlMsg2) {
        this.arvlMsg2 = arvlMsg2;
    }

    public String getArvlMsg3() {
        return arvlMsg3;
    }

    public void setArvlMsg3(String arvlMsg3) {
        this.arvlMsg3 = arvlMsg3;
    }
}
