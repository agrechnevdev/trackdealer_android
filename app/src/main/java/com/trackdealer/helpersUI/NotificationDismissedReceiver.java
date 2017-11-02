package com.trackdealer.helpersUI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.trackdealer.utils.ConstValues.ACTION_TO_ACTIVITY;
import static com.trackdealer.utils.ConstValues.ACTION_TO_SERVICE;
import static com.trackdealer.utils.ConstValues.STOPFOREGROUND_ACTION;
import static com.trackdealer.utils.ConstValues.TYPE_OF_ACTION;

/**
 * Created by grechnev-av on 02.11.2017.
 */

public class NotificationDismissedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentDismiss = new Intent(ACTION_TO_SERVICE);
        intentDismiss.putExtra(TYPE_OF_ACTION, STOPFOREGROUND_ACTION);
        context.sendBroadcast(intentDismiss);

        Intent intentActivity = new Intent(ACTION_TO_ACTIVITY);
        intentActivity.putExtra(TYPE_OF_ACTION, STOPFOREGROUND_ACTION);
        context.sendBroadcast(intentActivity);
    }
}