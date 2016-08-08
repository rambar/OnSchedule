package com.kaist.onschedule;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

public class RegisterSchedDateActivity extends AppCompatActivity implements View.OnClickListener{

    private DatePicker dp;
    SchedInfoDetail mInfoDetail;
    //private SchedInfo mSchedInfo;
    public static final String TAG = "kaist_DateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInfoDetail = new SchedInfoDetail();
        Intent i = getIntent();

        //if (i.getStringExtra("year") != null)
        mInfoDetail.setYear(i.getStringExtra("year"));
        mInfoDetail.setMonth(i.getStringExtra("month"));
        mInfoDetail.setDay(i.getStringExtra("day"));
        mInfoDetail.setHour(i.getStringExtra("hour"));
        mInfoDetail.setMinute(i.getStringExtra("minute"));
        mInfoDetail.setDeparture(i.getStringExtra("departure"));
        mInfoDetail.setDestination(i.getStringExtra("destination"));
        mInfoDetail.setId(i.getIntExtra("id", -1));
        mInfoDetail.setRoute1(i.getStringExtra("route1"));
        mInfoDetail.setRoute2(i.getStringExtra("route2"));
        mInfoDetail.setRoute3(i.getStringExtra("route3"));
        mInfoDetail.setRoute4(i.getStringExtra("route4"));
        mInfoDetail.setRoute5(i.getStringExtra("route5"));
        mInfoDetail.setLeadtimetotal(i.getStringExtra("leadtimetotal"));

        setContentView(R.layout.activity_register_sched_date);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        dp = (DatePicker) findViewById(R.id.datePicker);

        if(mInfoDetail.getYear() != null && mInfoDetail.getMonth() != null && mInfoDetail.getDay() != null) {
            dp.init(Integer.parseInt(mInfoDetail.getYear()), Integer.parseInt(mInfoDetail.getMonth()) - 1, Integer.parseInt(mInfoDetail.getDay()), null);
        }
        Button btnDate = (Button) findViewById(R.id.btnDate);
        btnDate.setOnClickListener(this);
        setSupportActionBar(toolbar);

        //mSchedInfo = new SchedInfo();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDate:
                String date = dp.getYear() + "-" + (dp.getMonth() + 1) + "-" + dp.getDayOfMonth();
                Log.i(TAG, date);
                //mSchedInfo.set_date(date);
                Intent i = new Intent(RegisterSchedDateActivity.this, com.kaist.onschedule.RegisterSchedTimeActivity.class);
                i.putExtra("date", date);
                i.putExtra("hour", mInfoDetail.getHour());
                i.putExtra("minute", mInfoDetail.getMinute());
                i.putExtra("departure", mInfoDetail.getDeparture());
                i.putExtra("destination", mInfoDetail.getDestination());
                i.putExtra("id", mInfoDetail.getId());
                if(mInfoDetail.getRoute1() != null && !mInfoDetail.getRoute1().isEmpty()) {
                    i.putExtra("route1", mInfoDetail.getRoute1());
                }
                if(mInfoDetail.getRoute2() != null && !mInfoDetail.getRoute2().isEmpty()) {
                    i.putExtra("route2", mInfoDetail.getRoute2());
                }
                if(mInfoDetail.getRoute3() != null && !mInfoDetail.getRoute3().isEmpty()) {
                    i.putExtra("route3", mInfoDetail.getRoute3());
                }
                if(mInfoDetail.getRoute4() != null && !mInfoDetail.getRoute4().isEmpty()) {
                    i.putExtra("route4", mInfoDetail.getRoute4());
                }
                if(mInfoDetail.getRoute5() != null && !mInfoDetail.getRoute5().isEmpty()) {
                    i.putExtra("route5", mInfoDetail.getRoute5());
                }
                if(mInfoDetail.getLeadtimetotal() != null && !mInfoDetail.getLeadtimetotal().isEmpty()) {
                    i.putExtra("leadtimetotal", mInfoDetail.getLeadtimetotal());
                }
                i.addFlags(i.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
        }
    }
}
