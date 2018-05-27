package com.trackdealer.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.player.event.PlayerState;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.BottomNavigationHelper;
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IConnected;
import com.trackdealer.interfaces.IDispatchTouch;
import com.trackdealer.interfaces.ILogout;
import com.trackdealer.interfaces.ITrackListState;
import com.trackdealer.models.ShowPlaylist;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.login.FirstChoseSongActivity;
import com.trackdealer.ui.login.PreloginActivity;
import com.trackdealer.ui.main.chart.ChartFragment;
import com.trackdealer.ui.main.chart.PlaylistDialog;
import com.trackdealer.ui.main.favour.FavourFragment;
import com.trackdealer.ui.main.profile.ProfileFragment;
import com.trackdealer.ui.mvp.UserSettingsPresenter;
import com.trackdealer.ui.mvp.UserSettingsView;
import com.trackdealer.utils.Prefs;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_NOT_FIRST_START;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class MainActivity extends DeezerActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ITrackListState, ILogout, UserSettingsView {

    private final String TAG = "MainActivity ";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    @Bind(R.id.main_bot_nav_view)
    BottomNavigationView botNavView;

    CompositeDisposable compositeDisposable;
    ChartFragment chartFragment;
    FavourFragment favourFragment;
    ProfileFragment profileFragment;
    PlaylistDialog playlistDialog;

    IConnected iConnected;
    IDispatchTouch iDispatchTouch;

    HeadSetReceiver headSetReceiver;
    TelephonyManager telephonyManager;
    CallStateListener callStateListener;


    UserSettingsPresenter userSettingsPresenter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        // restore existing deezer Connection

        ((BaseApp) getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        compositeDisposable = new CompositeDisposable();

        botNavView.setOnNavigationItemSelectedListener(this);
        BottomNavigationHelper.removeShiftMode(botNavView);

        setupPlayerUI();

        chartFragment = new ChartFragment();
        favourFragment = FavourFragment.newInstance(true);
        profileFragment = new ProfileFragment();
        iConnected = profileFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, chartFragment).commit();
        iDispatchTouch = chartFragment;

        headSetReceiver = new HeadSetReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headSetReceiver, filter);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        callStateListener = new CallStateListener();
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        if (!Prefs.getBoolean(this, SHARED_FILENAME_USER_DATA, SHARED_KEY_NOT_FIRST_START)) {
            startActivity(new Intent(this, FirstChoseSongActivity.class));
        }

        if (!Prefs.getBoolean(this, SHARED_FILENAME_USER_DATA, SHARED_KEY_NOT_FIRST_START)) {
            new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                    .setTopColorRes(R.color.colorWhite)
                    .setButtonsColorRes(R.color.colorOrange)
                    .setIcon(R.drawable.app_logo_bold_middle)
                    .setMessage(R.string.info_first_start)
                    .setPositiveButton(R.string.ok, v -> Prefs.putBoolean(this, SHARED_FILENAME_USER_DATA, SHARED_KEY_NOT_FIRST_START, true))
                    .show();
        }

        userSettingsPresenter = new UserSettingsPresenter(restapi, this);
        userSettingsPresenter.attachView(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeezerConnect event) {
        iConnected.connectSuccess(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Long playTrackId) {
        chartFragment.updatePositionIndicator();
        if (playlistDialog != null) {
            playlistDialog.updatePositionIndicator();
        }
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ShowPlaylist showPlaylist) {
        playlistDialog = PlaylistDialog.newInstance(getString(R.string.current_playlist));
        playlistDialog.show(getSupportFragmentManager(), "playlist");
    }

    @Override
    public void logout() {
        Prefs.clearUserData(this);
        if (trackPlayer.getPlayerState() == PlayerState.PLAYING)
            trackPlayer.stop();
        startActivity(new Intent(this, PreloginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        userSettingsPresenter.getUserSeettings(0);
        Timber.d(TAG + " onResume() ");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void getUserSettingsSuccess() {
        // do nothing
    }

    @Override
    public void getUserSettingsFailed(String error) {
        // do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d(TAG + " onDestroy() ");
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
        callStateListener = null;
        telephonyManager = null;
        compositeDisposable.dispose();
        unregisterReceiver(headSetReceiver);
        userSettingsPresenter.detachView();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return iDispatchTouch.dispatchTouch() || super.dispatchTouchEvent(ev);
    }

    @Override
    public void updatePosIndicator() {
        SPlay.init().playTrackId = SPlay.init().getPlayingTrackId();
        chartFragment.updatePositionIndicator();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.base_menu_list:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, chartFragment).commit();
                iDispatchTouch = chartFragment;
                return true;

            case R.id.base_menu_chose_song:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, favourFragment).commit();
                iDispatchTouch = favourFragment;
                return true;

            case R.id.base_menu_logout:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, profileFragment).commit();
                return true;
        }
        return false;
    }

    private void setupPlayerUI() {
        setPlayerVisible(false);
        setButtonEnabled(mButtonPlayerSkipBackward, false);
        setButtonEnabled(mButtonPlayerStop, false);
    }

    private class HeadSetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        pausePlayer();
                        break;
                    case 1:
                        Log.d(TAG, "Headset plugged");
                        break;
                }
            }
        }
    }

    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // called when someone is ringing to this phone
                    pausePlayer();
                    break;
            }
        }
    }

    public void pausePlayer() {
        if (trackPlayer != null && trackPlayer.getPlayerState() == PlayerState.PLAYING) {
            trackPlayer.pause();
        }
    }


    public void changeToolbar() {

    }
}
