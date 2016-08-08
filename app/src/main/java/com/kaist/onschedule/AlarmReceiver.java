package com.kaist.onschedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "kaist_AlarmReceiver";

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast
        // .
        Log.i(TAG, "Receive Alarm");

        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");

        /*
        // create an intent to the report service
        Intent service_intent = new Intent(context, StartReportService.class);

        // start the report service
        context.startService(service_intent);
        */
        Intent i = new Intent();
        i.putExtra("date", date);
        i.putExtra("time", time);

        i.setClassName("com.kaist.onschedule", "com.kaist.onschedule.ReportActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        /* 뉴톤톡으로 음성 신호 : OnSchedule 이 시작되었습니다 */
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
