package com.kaist.onschedule;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

//public abstract class ProximityReceiver extends BroadcastReceiver {
public abstract class ProximityReceiver extends BroadcastReceiver {
    private static final String TAG = "kaist_ProximityReceiver";
    /*
    public static final String EVENT_ID_INTENT_EXTRA = "EventIDIntentExtraKey";
    */
    private static final String WHERE_CURRENT = "current";
    private static final String WHERE_ROUTE1 = "route1";
    private static final String WHERE_ROUTE2 = "route2";
    private static final String WHERE_ROUTE3 = "route3";
    private static final String WHERE_ROUTE4 = "route4";
    private static final String WHERE_ROUTE5 = "route5";
    private static final String WHERE_DESTINATION = "destination";
    private static final int CURRENT = 0;
    private static final int ROUTE1 = 1;
    private static final int ROUTE2 = 2;
    private static final int ROUTE3 = 3;
    private static final int ROUTE4 = 4;
    private static final int ROUTE5 = 5;
    private static final int DESTINATION = 6;
    private static final int NOTIFICATION_ID = 1000;
    public Context mContext;//Context of calling BroadCast Receiver
    private String bidId;   //Hold the value of bid Id

    public ProximityReceiver(Context context) {
        mContext = context;
    }

    public ProximityReceiver(String bidId) {
        // TODO Auto-generated constructor stub
        this.bidId = bidId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.i(TAG, "Proximity Receiver");
        /*
        long eventID = intent.getLongExtra(EVENT_ID_INTENT_EXTRA, -1);
        Log.i(TAG, "Proximity Alert Intent Received, eventID = " + eventID);
        */

        /*
        if(intent.getStringExtra("where").equals(WHERE_CURRENT)) {
            onProximityEvent(intent.getStringExtra("where"));
        } else if (intent.getStringExtra("where").equals(WHERE_ROUTE)) {

        }
        */

        //throw new UnsupportedOperationException("Not yet implemented");
        String key = LocationManager.KEY_PROXIMITY_ENTERING;

        Boolean entering = intent.getBooleanExtra(key, false);
        if (entering) {
            Log.d(getClass().getSimpleName(), "entering");
            onProximityEvent(intent.getStringExtra("where"), true);
            Toast.makeText(mContext, "You are entering", Toast.LENGTH_LONG).show();
        }
        else {
            Log.d(getClass().getSimpleName(), "exiting");
            onProximityEvent(intent.getStringExtra("where"), true);
            Toast.makeText(mContext, "You are exiting", Toast.LENGTH_LONG).show();;
        }

        //sendNotification(context);

    }

    public void sendNotification(Context mcontext){
        // String extra=arg1.getExtras().getString("alert").toString();
        long when = System.currentTimeMillis();
        String message = "You are near of driver pickup area.";
        NotificationManager notificationManager = (NotificationManager) mcontext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(R.drawable.ic_info_black_24dp,message, when);
        String title = "Proximity Alert!";
        Intent notificationIntent = new Intent();
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(mcontext, 0,notificationIntent, 0);
        //notification.setLatestEventInfo(mcontext, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_ALL;
        notificationManager.notify(0, notification);
    }


    protected abstract void onProximityEvent(String where, boolean close);
}
