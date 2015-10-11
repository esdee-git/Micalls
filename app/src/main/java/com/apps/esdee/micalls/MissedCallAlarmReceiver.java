package com.apps.esdee.micalls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.CallLog;
import android.util.Log;

import java.util.Date;

public class MissedCallAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MissedCallAlarmReceiver", "alarm received");

        //lets first check if there are missed calls not acknowledged by the user
        String timestamp = String.valueOf(getTodayTimestamp());
        //AndroidStudio insists on explicitly calling checkPermission and try/catch SecurityException
        if (hasPermission(context, "android.permission.READ_CALL_LOG")) {
            try {
                //http://stackoverflow.com/questions/22087625/not-getting-missed-call-value-from-call-logs-android
                Cursor cursor = context.getContentResolver()
                        .query(CallLog.Calls.CONTENT_URI,
                                new String[]{CallLog.Calls.DATE, CallLog.Calls.TYPE,
                                        CallLog.Calls.DURATION, CallLog.Calls.NUMBER,
                                        CallLog.Calls.IS_READ, CallLog.Calls.NEW,
                                        CallLog.Calls._ID},
//                                CallLog.Calls.DATE + ">?"
//                                        + " and "
//                                        + CallLog.Calls.TYPE + "=?",
//                                new String[]{timestamp, String.valueOf(CallLog.Calls.MISSED_TYPE)},
                                CallLog.Calls.NEW + " = " + "1" + " AND "
                                        + CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE,
                                null,
                                CallLog.Calls.DATE);
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    //while (!cursor.isAfterLast())
                    {
                        int isRead = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.IS_READ));
                        int isNew = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW));

                        //this is redundant: if (isRead == 0 || isNew == 1)
                        //btw isRead is usually 0, as it is set to 1 only when user interacts with the number (e.g. calls back)
                        //isNew is the reliable check, but it is already included in the filter in the query above
                        {
                            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(2000);
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            MediaPlayer mp = MediaPlayer.create(context, notification);
                            mp.start();

                            String incomingNumber = "re-setting alarm";
                            Bundle extras = intent.getExtras();
                            if (extras != null) {
                                incomingNumber = extras.getString("incomingNumber");
                            }
                            Utils.setAlarm(context, incomingNumber);
                            return;
                        }

                        //cursor.moveToNext();
                    }
                }
                cursor.close();
            } catch (SecurityException exception) {
                Log.e("MissedCallAlarmReceiver", "SecurityException: " + exception.getMessage());
            }
        }
    }

    long getTodayTimestamp() {
        return new Date().getTime();
    }

    //http://stackoverflow.com/questions/18236801/programmatically-retrieve-permissions-from-manifest-xml-in-android/18237962#18237962
    //for example, permission can be "android.permission.WRITE_EXTERNAL_STORAGE"
    boolean hasPermission(Context ctx, String permission) {
        return ctx.getPackageManager().checkPermission(permission, ctx.getPackageName())
                == PackageManager.PERMISSION_GRANTED;
    }
}
