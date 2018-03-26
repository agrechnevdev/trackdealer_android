package com.trackdealer;

/**
 * Created by grechnev-av on 29.08.2017.
 */

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDelegate;

import com.trackdealer.module.AppModule;
import com.trackdealer.module.NetModule;
import com.trackdealer.net.DaggerNetComponent;
import com.trackdealer.net.NetComponent;
import com.trackdealer.utils.ConstValues;

import timber.log.Timber;

public class BaseApp extends Application {

    private NetComponent netComponent;

    @Override
    public void onCreate() {
        super.onCreate();

//        StaticUtils.putDefaultTracks(getApplicationContext());

        netComponent = DaggerNetComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule(ConstValues.mainUrl, getApplicationContext()))
                .build();


        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());

//            ExcludedRefs excludedRefs = AndroidExcludedRefs.createAppDefaults()
//                    .instanceField("android.view.inputmethod.InputMethodManager", "sInstance")
//                    .instanceField("android.view.inputmethod.InputMethodManager", "mLastSrvView")
//                    .instanceField("com.android.internal.policy.PhoneWindow$DecorView", "mContext")
//                    .instanceField("android.support.v7.widget.SearchView$SearchAutoComplete", "mContext")
//                    .build();
//
//            LeakCanary.refWatcher(this)
//                    .listenerServiceClass(DisplayLeakService.class)
//                    .excludedRefs(excludedRefs)
//                    .buildAndInstall();
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public NetComponent getNetComponent() {
        return netComponent;
    }

}
