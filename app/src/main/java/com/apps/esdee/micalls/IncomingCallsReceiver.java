package com.apps.esdee.micalls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IncomingCallsReceiver extends BroadcastReceiver {

    public IncomingCallsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.e("IncomingCallsReceiver", "no extras!");
            return;
        }
        String incomingNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

        PhoneStateChangeListener phoneStateChangeListener = new PhoneStateChangeListener(context, incomingNumber);
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateChangeListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
}

class PhoneStateChangeListener extends PhoneStateListener {

    static boolean wasRinging;
    static String lastMissedCall = "";
    Context context = null;
    String incomingNumber = "";

    public PhoneStateChangeListener(Context ctx, String incoming) {
        context = ctx;
        incomingNumber = incoming;
    }

    @Override
    public void onCallStateChanged(int state, String incoming) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                Log.i("PhoneStateChange", "RINGING");
                wasRinging = true;
                lastMissedCall = "";
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.i("PhoneStateChange", "OFFHOOK");

                if (!wasRinging) {
                    Log.i("PhoneStateChange", "was not ringing - not possible");
                } else {
                    Log.i("PhoneStateChange", "call was answered");
                }

                wasRinging = false;
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                Log.i("PhoneStateChange", "IDLE");

                if (!wasRinging) {
                    Log.i("PhoneStateChange", "was not ringing - weird");
                } else {
                    Log.i("PhoneStateChange", "missed the call");

                    //might come here several times during a call sequence so make sure
                    //certain missed call is processed only once
                    if (!lastMissedCall.equalsIgnoreCase(incomingNumber)) {
                        lastMissedCall = incomingNumber;
                        Utils.setAlarm(context, incomingNumber);
                    }
                }

                wasRinging = false;
                break;
        }
    }
}
