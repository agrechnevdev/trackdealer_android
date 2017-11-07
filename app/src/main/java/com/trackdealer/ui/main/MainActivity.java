package com.trackdealer.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
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
import com.trackdealer.ui.main.chart.ChartFragment;
import com.trackdealer.ui.main.chart.PlaylistDialog;
import com.trackdealer.ui.main.favour.FavourFragment;
import com.trackdealer.ui.main.profile.ProfileFragment;
import com.trackdealer.utils.Prefs;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class MainActivity extends DeezerActivity implements BottomNavigationView.OnNavigationItemSelectedListener, ITrackListState, ILogout {

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
        favourFragment = new FavourFragment();
        profileFragment = new ProfileFragment();
        iConnected = profileFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, chartFragment).commit();
        iDispatchTouch = chartFragment;
//        if(Prefs.getString(this, SHARED_FILENAME_TRACK, SHARED_KEY_FIRST_TIME).equals("")) {
//          startActivity(new Intent(this, TutorialActivity.class));
//        }
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
        playlistDialog = new PlaylistDialog();
        playlistDialog.show(getSupportFragmentManager(), "playlist");
    }

    @Override
    public void logout() {
        Prefs.clearUserData(this);
        if (trackPlayer.getPlayerState() == PlayerState.PLAYING)
            trackPlayer.stop();
        doDestroyPlayer();
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d(TAG + " onResume() ");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d(TAG + " onDestroy() ");
        compositeDisposable.dispose();
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
}
