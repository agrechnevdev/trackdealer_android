package com.trackdealer.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.BottomNavigationHelper;
import com.trackdealer.interfaces.IChoseTrack;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.Restapi;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class MainActivity extends PlayerActivity implements BottomNavigationView.OnNavigationItemSelectedListener, PlayerWrapperListener, IChoseTrack {

    private final String TAG = "MainActivity";
    private TrackPlayer mTrackPlayer;

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    @Bind(R.id.main_bot_nav_view)
    BottomNavigationView botNavView;

    CompositeDisposable compositeDisposable;
    ChartFragment chartFragment;
    FavourFragment favourFragment;

    Track playingTrack;

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
        createPlayer();
        chartFragment = new ChartFragment();
        favourFragment = new FavourFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, chartFragment).commit();
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
    public void choseTrackForPlay(TrackInfo trackInfo) {
        if (playingTrack != null && playingTrack.getId() == trackInfo.getTrackId()) {
            if (mTrackPlayer.getPlayerState() == PlayerState.PLAYING)
                mTrackPlayer.pause();
            else if (mTrackPlayer.getPlayerState() == PlayerState.PAUSED)
                mTrackPlayer.play();
            else if (mTrackPlayer.getPlayerState() == PlayerState.STOPPED)
                mTrackPlayer.playTrack(playingTrack.getId());
        } else {
            if (mTrackPlayer.getPlayerState() == PlayerState.PLAYING)
                mTrackPlayer.stop();
            displayTrackInfo(trackInfo);
            setPlayerVisible(true);
            loadSong(trackInfo);
        }
    }

    public void loadSong(TrackInfo trackInfo) {
        DeezerRequest request = DeezerRequestFactory.requestTrack(trackInfo.getTrackId());
        AsyncDeezerTask task = new AsyncDeezerTask(mDeezerConnect, new JsonRequestListener() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onResult(final Object result, final Object requestId) {
                        playingTrack = (Track) result;
                        Timber.d(TAG + mTrackPlayer.getPlayerState().name());
                        mTrackPlayer.playTrack(playingTrack.getId());
                    }

                    @Override
                    public void onUnparsedResult(final String response, final Object requestId) {
                        handleError(new DeezerError("Unparsed reponse"));
                    }

                    @Override
                    public void onException(final Exception exception,
                                            final Object requestId) {
                        handleError(exception);
                    }
                });
        task.execute(request);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.base_menu_list:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, chartFragment).commit();
                return true;

            case R.id.base_menu_chose_song:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, favourFragment).commit();
                return false;

            case R.id.base_menu_logout:
                SessionStore sessionStore = new SessionStore();
                if (sessionStore.restore(mDeezerConnect, this)) {
                    Toast.makeText(this, "Уже залогинен в Deezer", Toast.LENGTH_LONG).show();
                } else {
                    connectToDeezer();
                }
                return false;
        }
        return false;
    }


    private void createPlayer() {
        try {
            doDestroyPlayer();
            mTrackPlayer = new TrackPlayer(getApplication(), mDeezerConnect, new WifiAndMobileNetworkStateChecker());
            mTrackPlayer.addPlayerListener(this);
            setAttachedPlayer(mTrackPlayer);
        } catch (TooManyPlayersExceptions | DeezerError e) {
            handleError(e);
        }
    }

    private void setupPlayerUI() {
        setPlayerVisible(false);
        setButtonEnabled(mButtonPlayerSkipBackward, false);
        setButtonEnabled(mButtonPlayerSkipForward, false);
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

    }

    @Override
    public void onAllTracksEnded() {
    }

    @Override
    public void onRequestException(final Exception e, final Object requestId) {
        handleError(e);
    }

}
