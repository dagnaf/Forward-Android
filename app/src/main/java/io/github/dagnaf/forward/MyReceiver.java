package io.github.dagnaf.forward;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyReceiver extends BroadcastReceiver {

    private static final String TAG = MyReceiver.class.getSimpleName();
    // TODO
    // Locale customization
    private static final DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINESE);

    // TODO
    // Message summarize to avoid spam
    @Override
    public void onReceive(Context context, Intent intent) {
        // how-do-i-get-the-sharedpreferences-from-a-preferenceactivity-in-android
        // http://stackoverflow.com/a/2614771/4833462
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean("pref_forward", false)) return;

        String body = null;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            if (bundle == null) return;
            try{
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus == null) return;
                msgs = new SmsMessage[pdus.length];
                String msg_from = null;
                String msg_body = "";
                String msg_time = null;
                for(int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    if (msg_from == null) msg_from = msgs[i].getOriginatingAddress();
                    msg_body += msgs[i].getMessageBody();
                    if (msg_time == null) msg_time = formatter.format(new Date(msgs[i].getTimestampMillis()));
                }
                body = "SMS From: " + msg_from + " \n" + msg_body + " \n" + msg_time + " \n\n";
            } catch (Exception e) {
                Log.i(TAG, "onReceive: SMS " + e);
            }
        } else if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            // android-broadcast-receiver-for-call-not-working-marshmallow
            // http://stackoverflow.com/a/38914288/4833462
            if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String call_from = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                String call_time = formatter.format(new Date(System.currentTimeMillis()));
                body = "Call From: " + call_from + " \n" + call_time + " \n\n";
            }
            // else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
            //        TelephonyManager.EXTRA_STATE_IDLE)) {
            // }
            // else if (intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(
            //         TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            // }
        }

        if (body == null) return;

        String email = prefs.getString("pref_email", null);
        String pw = prefs.getString("pref_pw", null);

        new AsyncTask<String, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(String... p) {
                return MailUtils.sendMail(p[0], p[1], p[2], p[3]);
            }
        }.execute(email, pw, email, body);
    }
}
