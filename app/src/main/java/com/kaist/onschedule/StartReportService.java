package com.kaist.onschedule;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StartReportService extends Service {
    private static final String TAG = "kaist_ReportService";

    public StartReportService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // notification
        // set up the notification service
        NotificationManager notify_manager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // set up an intent that goes to the Main Activity
        Intent intent_report_activity = new Intent(this.getApplicationContext(), com.kaist.onschedule.ReportActivity.class);
        // set up a pending intent
        PendingIntent pending_intent_main_activity = PendingIntent.getActivity(this, 0,
                intent_report_activity, 0);
        Log.i(TAG, "onStartCommand");
        // make the notification parameters
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Notification notification_popup = new Notification.Builder(this)
                    .setContentTitle("Schedule is comming")
                    .setContentText("OnSchedule is ready")
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentIntent(pending_intent_main_activity)
                    .setAutoCancel(true)
                    .build();
            notification_popup.defaults |= Notification.DEFAULT_VIBRATE;
            notification_popup.defaults |= Notification.DEFAULT_SOUND;

            notify_manager.notify(0, notification_popup);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
