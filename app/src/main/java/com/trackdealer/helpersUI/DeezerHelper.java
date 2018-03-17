package com.trackdealer.helpersUI;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.trackdealer.R;

import org.greenrobot.eventbus.EventBus;

import timber.log.Timber;

/**
 * Created by anton on 17.03.2018.
 */

public class DeezerHelper {

    private static DeezerHelper instance = null;

    public static final String APP_ID = "250582";
    public DeezerConnect mDeezerConnect = null;

    private DeezerHelper() {
    }

    public static synchronized DeezerHelper init() {
        if (instance == null)
            instance = new DeezerHelper();
        return instance;
    }

    public void establishDeezerConnect(Context context) {
        mDeezerConnect = new DeezerConnect(context, APP_ID);
        new SessionStore().restore(mDeezerConnect, context);
    }

    public void disconnectFromDeezer(Context context) {
        if (mDeezerConnect != null) {
            mDeezerConnect.logout(context);
        }
        new SessionStore().clear(context);
    }

    public void destroyDeezerConnect(Context context) {
        if (mDeezerConnect != null) {
            mDeezerConnect.logout(context);
            mDeezerConnect = null;
        }
    }



}
