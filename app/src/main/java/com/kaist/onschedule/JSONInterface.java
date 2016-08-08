package com.kaist.onschedule;

public interface JSONInterface {
    void getJSONDATA(String text, boolean whitespace, String latitude, String longitude);

    SubwayInfo[] getJSONDATA(String nextstation, boolean whitespace);

    String getPointX();
    String getPointY();
    String getAddress();

    SubwayInfo[] getTransportationJSONInfo();
}
