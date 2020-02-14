package com.dzakdzaks.firebasemessaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * ==================================//==================================
 * ==================================//==================================
 * Created on Friday, 14 February 2020 at 19:32.
 * Project Name => FirebaseMessaging
 * Package Name => com.dzakdzaks.firebasemessaging
 * ==================================//==================================
 * ==================================//==================================
 */
public class PushAutoStart extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, FirebaseMessagingServices.class);
        context.startService(i);
    }
}