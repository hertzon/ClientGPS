package com.ejemplos.nelson.clientgps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

// make sure we use a WakefulBroadcastReceiver so that we acquire a partial wakelock
public class GpsTrackerAlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "GPSTrax";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"en GpsTrackerAlarmReceiver");
        context.startService(new Intent(context, LocationService.class));
    }
}
