package com.trackdealer.ui.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.BottomNavigationHelper;
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IConnected;
import com.trackdealer.interfaces.ITrackListState;
import com.trackdealer.models.PositionPlay;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.chart.ChartFragment;
import com.trackdealer.ui.main.favour.FavourFragment;
import com.trackdealer.ui.main.profile.ProfileFragment;

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

public class MainActivity extends DeezerActivity implements BottomNavigationView.OnNavigationItemSelectedListener, PlayerWrapperListener, ITrackListState {

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

    IConnected iConnected;

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

        recreatePlayer();
        trackPlayer.addPlayerListener(this);

        chartFragment = new ChartFragment();
        favourFragment = new FavourFragment();
        profileFragment = new ProfileFragment();
        iConnected = profileFragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, chartFragment).commit();
//        if(Prefs.getString(this, SHARED_FILENAME_TRACK, SHARED_KEY_FIRST_TIME).equals("")) {
//          startActivity(new Intent(this, TutorialActivity.class));
//        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeezerConnect event) {
        iConnected.connectSuccess(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PositionPlay posPlay) {
        chartFragment.changePositionIndicator();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d(TAG + " onResume() ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d(TAG + " onDestroy() ");
        compositeDisposable.dispose();
    }

    @Override
    public void changePosIndicator() {
        SPlay.init().positionPlay = new PositionPlay(-1, getPositionPlay());
        chartFragment.changePositionIndicator();
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.base_menu_list:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, chartFragment).commit();
                return true;

            case R.id.base_menu_chose_song:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, favourFragment).commit();
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

    //////////////////////////////////////////////////////////////////////////////////////
    // Player listener
    //////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPlayTrack(PlayableEntity playableEntity) {

    }

    @Override
    public void onTrackEnded(PlayableEntity playableEntity) {
        playNextTrack();
    }

    @Override
    public void onAllTracksEnded() {
    }

    @Override
    public void onRequestException(final Exception e, final Object requestId) {
        handleError("Ошибка при запросе на сервер", e);
    }

}
