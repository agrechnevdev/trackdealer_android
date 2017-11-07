package com.trackdealer.ui.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.trackdealer.R;
import com.trackdealer.helpersUI.NotificationDismissedReceiver;
import com.trackdealer.ui.main.MainActivity;

import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static com.trackdealer.utils.ConstValues.ACTION_TO_ACTIVITY;
import static com.trackdealer.utils.ConstValues.ACTION_TO_SERVICE;
import static com.trackdealer.utils.ConstValues.ARTIST;
import static com.trackdealer.utils.ConstValues.BUTTON_PLAY;
import static com.trackdealer.utils.ConstValues.CHANGE_INFO_ACTION;
import static com.trackdealer.utils.ConstValues.FOREGROUND_SERVICE;
import static com.trackdealer.utils.ConstValues.MAIN_ACTION;
import static com.trackdealer.utils.ConstValues.NEXT_ACTION;
import static com.trackdealer.utils.ConstValues.PAUSE_ACTION;
import static com.trackdealer.utils.ConstValues.PLAY_ACTION;
import static com.trackdealer.utils.ConstValues.PREV_ACTION;
import static com.trackdealer.utils.ConstValues.STARTFOREGROUND_ACTION;
import static com.trackdealer.utils.ConstValues.STOPFOREGROUND_ACTION;
import static com.trackdealer.utils.ConstValues.TRACK_NAME;
import static com.trackdealer.utils.ConstValues.TYPE_OF_ACTION;

/**
 * Created by paulruiz on 10/28/14.
 */
public class MediaPlayerService extends Service {

    Notification status;
    private final String TAG = "MediaPlayerService";
    RemoteViews views;
    NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        Timber.d(TAG + " onCreate() ");
        final IntentFilter myFilter = new IntentFilter(ACTION_TO_SERVICE);
        registerReceiver(mReceiver, myFilter);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(STARTFOREGROUND_ACTION)) {
            showNotification(intent.getExtras().getString(ARTIST), intent.getExtras().getString(TRACK_NAME), intent.getExtras().getBoolean(BUTTON_PLAY));
        }
        return START_STICKY;
    }

    private void showNotification(String artist, String trackName, Boolean buttonPlay) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.player_notification);

        Intent playIntent = new Intent(ACTION_TO_ACTIVITY);
        playIntent.putExtra(TYPE_OF_ACTION, PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getBroadcast(this, 1, playIntent, 0);

        Intent nextIntent = new Intent(ACTION_TO_ACTIVITY);
        nextIntent.putExtra(TYPE_OF_ACTION, NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getBroadcast(this, 2, nextIntent, 0);

        Intent closeIntent = new Intent(ACTION_TO_ACTIVITY);
        closeIntent.putExtra(TYPE_OF_ACTION, STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getBroadcast(this, 3, closeIntent, 0);

//        Intent openIntent = new Intent(ACTION_TO_ACTIVITY);
//        openIntent.putExtra(TYPE_OF_ACTION, RESUME_MAIN_ACTION);
//        PendingIntent popenIntent = PendingIntent.getBroadcast(this, 3, openIntent, 0);
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent popenIntent = PendingIntent.getActivity(this, 3, openIntent, 0);

        Intent intent = new Intent(this, NotificationDismissedReceiver.class);
        intent.putExtra(MAIN_ACTION, FOREGROUND_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, FOREGROUND_SERVICE, intent, 0);

        views.setOnClickPendingIntent(R.id.button_pause, pplayIntent);
        views.setOnClickPendingIntent(R.id.button_skip_forward, pnextIntent);
        views.setTextViewText(R.id.text_track, trackName);
        views.setTextViewText(R.id.text_artist, artist);
        views.setOnClickPendingIntent(R.id.player_notification_main_lay, popenIntent);
        if (buttonPlay)
            views.setImageViewResource(R.id.button_pause, R.drawable.ic_pause);
        else
            views.setImageViewResource(R.id.button_pause, R.drawable.ic_play);

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.flags = Notification.FLAG_AUTO_CANCEL;
        status.icon = R.drawable.app_logo_2;
        status.deleteIntent = pendingIntent;
        mNotificationManager.notify(FOREGROUND_SERVICE, status);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d(TAG + " onDestroy() ");
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String typeOfAction = intent.getExtras().getString(TYPE_OF_ACTION);
            Timber.d(TAG + " onReceive() " + typeOfAction);
            switch (typeOfAction) {
                case (CHANGE_INFO_ACTION):
                    status.contentView.setTextViewText(R.id.text_artist, intent.getExtras().getString(ARTIST));
                    status.contentView.setTextViewText(R.id.text_track, intent.getExtras().getString(TRACK_NAME));
                    mNotificationManager.notify(FOREGROUND_SERVICE, status);
                    break;
                case (PLAY_ACTION):
                    status.contentView.setImageViewResource(R.id.button_pause, R.drawable.ic_pause);
                    status.flags = Notification.FLAG_ONGOING_EVENT;
                    mNotificationManager.notify(FOREGROUND_SERVICE, status);
                    break;
                case (PAUSE_ACTION):
                    status.contentView.setImageViewResource(R.id.button_pause, R.drawable.ic_play);
                    status.flags = Notification.FLAG_AUTO_CANCEL;
                    mNotificationManager.notify(FOREGROUND_SERVICE, status);
                    break;
                case (PREV_ACTION):
                    break;
                case (NEXT_ACTION):
                    break;
                case (STOPFOREGROUND_ACTION):
                    stopForeground(true);
                    stopSelf();
                    break;

            }

        }
    };
}