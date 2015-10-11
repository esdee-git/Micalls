package com.apps.esdee.micalls;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Utils {

    public static void setAlarm(Context context, String incomingNumber) {
        PendingIntent pendingIntent = createPendingIntent(context, incomingNumber);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (5 * 60 * 1000), pendingIntent);//ring after 5 minutes
    }

    public static void cancelAlarm(Context context, String incomingNumber) {
        PendingIntent pendingIntent = createPendingIntent(context, incomingNumber);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    static PendingIntent createPendingIntent(Context context, String incomingNumber) {
        Intent intent = new Intent(context, MissedCallAlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putString("incomingNumber", incomingNumber);
        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        return pendingIntent;
    }
}
