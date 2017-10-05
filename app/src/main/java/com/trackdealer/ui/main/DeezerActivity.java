package com.trackdealer.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.trackdealer.R;
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IChoseTrack;
import com.trackdealer.interfaces.IConnectDeezer;
import com.trackdealer.models.PositionPlay;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.ConnectionsManager;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.StaticUtils;

import org.greenrobot.eventbus.EventBus;

import java.net.ConnectException;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class DeezerActivity extends AppCompatActivity implements IConnectDeezer, IChoseTrack, PlayerWrapperListener {

    private final String TAG = "DeezerActivity ";

    private PlayerHandler mPlayerHandler = new PlayerHandler();

    CompositeDisposable subscription;

    @Bind(R.id.button_stop)
    protected ImageButton mButtonPlayerStop;
    @Bind(R.id.button_pause)
    protected ImageButton mButtonPlayerPause;
    @Bind(R.id.button_skip_forward)
    protected ImageButton mButtonPlayerSkipForward;
    @Bind(R.id.button_skip_backward)
    protected ImageButton mButtonPlayerSkipBackward;
    @Bind(R.id.button_seek_backward)
    protected ImageButton mButtonPlayerSeekBackward;
    @Bind(R.id.button_seek_forward)
    protected ImageButton mButtonPlayerSeekForward;

    @Bind(R.id.button_repeat)
    protected ImageButton mButtonPlayerRepeat;

    @Bind(R.id.seek_progress)
    SeekBar mSeekBar;


    @Bind(R.id.text_time)
    TextView mTextTime;
    @Bind(R.id.text_length)
    TextView mTextLength;

    @Bind(R.id.text_artist)
    TextView mTextArtist;
    @Bind(R.id.text_track)
    TextView mTextTrack;
    //    @Bind(R.id.text_position)
//    TextView mTextPos;


    protected DeezerConnect mDeezerConnect = null;
    protected TrackPlayer trackPlayer;
    public static final String APP_ID = "250582";
    protected static final String[] PERMISSIONS = new String[]{
            Permissions.BASIC_ACCESS, Permissions.OFFLINE_ACCESS,
            Permissions.MANAGE_LIBRARY
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        establishDeezerConnect();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        subscription = new CompositeDisposable();
        recreatePlayer();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d(TAG + " onResume() ");
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d(TAG + " onPause() ");
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d(TAG + " onDestroy() ");
        trackPlayer.removePlayerListener(this);
        doDestroyPlayer();
        subscription.dispose();

    }

    protected void playNextTrack() {
        List<TrackInfo> trackList = SPlay.init().playList;
        if (SPlay.init().playingTrack != null) {
            for (int i = 0; i < trackList.size(); i++) {
                if (trackList.get(i).getTrackId() == SPlay.init().playingTrack.getId()) {
                    if (i + 1 < trackList.size()) {
                        choseTrackForPlay(trackList.get(i + 1), i + 1);
                        return;
                    } else {
                        choseTrackForPlay(trackList.get(0), 0);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void playRandomTrack() {
        if (SPlay.init().playList.isEmpty() && !SPlay.init().showList.isEmpty()) {
            SPlay.init().playList.clear();
            SPlay.init().playList.addAll(SPlay.init().showList);
            int pos = new Random().nextInt(SPlay.init().playList.size());
            choseTrackForPlay(SPlay.init().playList.get(pos), pos);
        } else if (SPlay.init().playList.size() == 1) {
            if (SPlay.init().playingTrack == null) {
                choseTrackForPlay(SPlay.init().playList.get(0), 0);
            }
        } else if (SPlay.init().playList.size() > 1) {
            int pos = new Random().nextInt(SPlay.init().playList.size());
            if (SPlay.init().playingTrack == null) {
                choseTrackForPlay(SPlay.init().playList.get(pos), pos);
            } else {
                if (pos != getPosPlayForPlayList(SPlay.init().playingTrack.getId())) {
                    choseTrackForPlay(SPlay.init().playList.get(pos), pos);
                } else {
                    pos = pos == 0 ? SPlay.init().playList.size() - 1 : 0;
                    choseTrackForPlay(SPlay.init().playList.get(pos), pos);
                }
            }
        }
    }

    protected Integer getPosPlayForPlayList(Long trackId) {
        Integer positionPlay = -1;
        for (int i = 0; i < SPlay.init().playList.size(); i++) {
            if (trackId != null && trackId == SPlay.init().playList.get(i).getTrackId()) {
                positionPlay = i;
                break;
            }
        }
        return positionPlay;
    }

    protected Integer getPosPlayForIndicator(Long trackId) {
        Integer positionPlay = -1;
        for (int i = 0; i < SPlay.init().showList.size(); i++) {
            if (trackId != null && trackId == SPlay.init().showList.get(i).getTrackId()) {
                positionPlay = i;
                break;
            }
        }
        return positionPlay;
    }

    @Override
    public void choseTrackForPlay(TrackInfo trackInfo, Integer pos) {
        Track playingTrack = SPlay.init().playingTrack;
        PlayerState playerState = trackPlayer.getPlayerState();
        if (playingTrack != null && playingTrack.getId() == trackInfo.getTrackId()) {
            if (playerState == PlayerState.PLAYING)
                trackPlayer.pause();
            else if (playerState == PlayerState.PAUSED)
                trackPlayer.play();
            else if (trackPlayer.getPlayerState() == PlayerState.RELEASED)
                recreatePlayer();
        } else {
            if (playerState == PlayerState.PLAYING)
                trackPlayer.stop();
            else if (trackPlayer.getPlayerState() == PlayerState.RELEASED)
                recreatePlayer();

            // меняем позицию индикатора
            Integer oldPos = getPosPlayForIndicator(SPlay.init().getPlayingTrackId());
            Integer newPos = getPosPlayForIndicator(trackInfo.getTrackId());
            SPlay.init().positionPlay = new PositionPlay(oldPos, newPos);
            EventBus.getDefault().post(SPlay.init().positionPlay);

            // отображаем информацию о треке в плеере и кнопки
            displayTrackInfo(trackInfo, pos);
            // грузим песню
            loadSong(trackInfo);
        }
    }

    // грузим загружаем песню
    public void loadSong(TrackInfo trackInfo) {
        if (ConnectionsManager.isConnected(this)) {
            DeezerRequest request = DeezerRequestFactory.requestTrack(trackInfo.getTrackId());
            subscription.add(StaticUtils.requestFromDeezer(mDeezerConnect, request)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(obj -> {
                                SPlay.init().playingTrack = (Track) obj;

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        trackPlayer.playTrack(SPlay.init().playingTrack.getId());
                                    }
                                }).start();

                                setButtonEnabled(mButtonPlayerPause, true);
                                setButtonEnabled(mButtonPlayerSkipForward, true);
                            },
                            ex -> {
                                if (ex instanceof ConnectException) {
                                    recreatePlayer();
                                }
                                ErrorHandler.handleError(getApplicationContext(), "Не удалось загрузить песню.", (Exception) ex);
                            }));
        } else {
            ErrorHandler.handleError(getApplicationContext(), "Ошибка соединения.", new Exception());
        }
    }

    protected TrackPlayer recreatePlayer() {
        try {
            doDestroyPlayer();
            trackPlayer = new TrackPlayer(getApplication(), mDeezerConnect, new WifiAndMobileNetworkStateChecker());
            trackPlayer.addPlayerListener(this);
            setAttachedPlayer(trackPlayer);
        } catch (TooManyPlayersExceptions | DeezerError e) {
            ErrorHandler.handleError(getApplicationContext(), "Ошибка при создании плеера.", e);
        }
        return trackPlayer;
    }

    protected void setAttachedPlayer(final TrackPlayer player) {
        player.addOnBufferErrorListener(mPlayerHandler);
        player.addOnBufferStateChangeListener(mPlayerHandler);
        player.addOnBufferProgressListener(mPlayerHandler);
        player.addOnPlayerErrorListener(mPlayerHandler);
        player.addOnPlayerStateChangeListener(mPlayerHandler);
        player.addOnPlayerProgressListener(mPlayerHandler);
        if (trackPlayer.isAllowedToSeek()) {
            mSeekBar.setEnabled(true);
        }
    }

    protected void displayTrackInfo(final TrackInfo trackInfo, Integer pos) {
        //Зануляем прогресс воспроизведения
        showBufferProgress(0);
        showPlayerProgress(0);
        mTextArtist.setText(trackInfo.getArtist());
        mTextTrack.setText(trackInfo.getTitle());
        // блочим кнопки и показываем плеер
        setButtonEnabled(mButtonPlayerPause, false);
        setButtonEnabled(mButtonPlayerSkipForward, false);
        setPlayerVisible(true);
    }

    public synchronized void showPlayerProgress(final long timePosition) {
        mSeekBar.setProgress((int) timePosition / 1000);
    }

    public synchronized void showBufferProgress(final int position) {
        mSeekBar.setMax((int) trackPlayer.getTrackDuration() / 1000);
        long progress = (position * trackPlayer.getTrackDuration()) / 100;
        mSeekBar.setSecondaryProgress((int) progress / 1000);
    }

    public void showPlayerState(final PlayerState state) {
        mSeekBar.setEnabled(true);
        mButtonPlayerPause.setEnabled(true);
        mButtonPlayerStop.setEnabled(true);

        switch (state) {
            case STARTED:
//                mButtonPlayerPause.setEnabled(true);
//                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                break;
            case INITIALIZING:
//                mButtonPlayerPause.setEnabled(true);
//                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                break;
            case READY:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                showPlayerProgress(0);
                break;
            case PLAYING:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_pause);
                break;
            case PAUSED:
            case PLAYBACK_COMPLETED:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                break;

            case WAITING_FOR_DATA:
                mButtonPlayerPause.setEnabled(false);
                break;

            case STOPPED:
                mSeekBar.setEnabled(false);
                showPlayerProgress(0);
                showBufferProgress(0);
                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                mButtonPlayerStop.setEnabled(false);
                break;
            case RELEASED:
                break;
            default:
                break;
        }
    }

    private class PlayerHandler implements OnPlayerProgressListener, OnBufferProgressListener, OnPlayerStateChangeListener,
            OnPlayerErrorListener, OnBufferStateChangeListener, OnBufferErrorListener {

        @Override
        public void onBufferError(final Exception ex, final double percent) {
            Timber.d(TAG + "onBufferError");
            runOnUiThread(() -> ErrorHandler.handleError(getApplicationContext(), "Ошибка при загрузке буфера.", ex));
//            recreatePlayer();
//            playNextTrack();
        }

        @Override
        public void onBufferStateChange(final BufferState state, final double percent) {
            Timber.d(TAG + "onBufferStateChange " + state.name());
            runOnUiThread(() -> showBufferProgress((int) Math.round(percent)));
        }

        @Override
        public void onPlayerError(final Exception ex, final long timePosition) {
            Timber.d(TAG + "onPlayerError");
            runOnUiThread(() -> {
                ErrorHandler.handleError(getApplicationContext(), "Не удалось воспроизвести песню.", ex);
//                recreatePlayer();
//                playNextTrack();
            });
        }

        @Override
        public void onPlayerStateChange(final PlayerState state, final long timePosition) {
            Timber.d(TAG + "onPlayerStateChange " + state.name());
            runOnUiThread(() -> {
                showPlayerState(state);
                showPlayerProgress(timePosition);
            });
        }

        @Override
        public void onBufferProgress(final double percent) {
//            Timber.d(TAG + "onBufferProgress " + percent);
            runOnUiThread(() -> showBufferProgress((int) Math.round(percent)));
        }

        @Override
        public void onPlayerProgress(final long timePosition) {
//            Timber.d(TAG + "onPlayerProgress " + timePosition);
            runOnUiThread(() -> showPlayerProgress(timePosition));
        }
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.button_pause, R.id.button_skip_forward})
    public void onClickButton(final View v) {
        if (v == mButtonPlayerPause) {
            if (trackPlayer.getPlayerState() == PlayerState.PLAYING) {
                trackPlayer.pause();
            } else {
                trackPlayer.play();
            }
        } else if (v == mButtonPlayerSkipForward) {
            playNextTrack();
        }
    }

    protected void establishDeezerConnect() {
        Timber.d(TAG + " establishDeezerConnect() ");
        mDeezerConnect = new DeezerConnect(this, APP_ID);
        new SessionStore().restore(mDeezerConnect, this);
    }

    public void disconnectFromDeezer() {
        if (mDeezerConnect != null) {
            mDeezerConnect.logout(this);
        }
        new SessionStore().clear(this);
    }

    public void connectToDeezer() {
        SessionStore sessionStore = new SessionStore();
        if (sessionStore.restore(mDeezerConnect, this)) {
            Toast.makeText(this, "Уже залогинен в Deezer ", Toast.LENGTH_LONG).show();
        } else {
            mDeezerConnect.authorize(this, PERMISSIONS, mDeezerDialogListener);
        }
    }

    /**
     * A listener for the Deezer Login Dialog
     */
    private DialogListener mDeezerDialogListener = new DialogListener() {

        @Override
        public void onComplete(final Bundle values) {
            // store the current authentication info
            SessionStore sessionStore = new SessionStore();
            sessionStore.save(mDeezerConnect, getApplicationContext());
            Toast.makeText(getApplicationContext(), "Вы залогинились в Deezer!", Toast.LENGTH_LONG).show();
            recreatePlayer();

            EventBus.getDefault().post(mDeezerConnect);
        }

        @Override
        public void onException(final Exception exception) {
            Toast.makeText(getApplicationContext(), R.string.deezer_error_during_login,
                    Toast.LENGTH_LONG).show();
        }


        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), R.string.login_cancelled, Toast.LENGTH_LONG).show();
        }
    };

    /**
     * Will destroy player. Subclasses can override this hook.
     */
    protected void doDestroyPlayer() {

        if (trackPlayer == null) {
            // No player, ignore
            return;
        }
        if (trackPlayer.getPlayerState() == PlayerState.RELEASED) {
            // already released, ignore
            return;
        }
        // first, stop the player if it is not
        if (trackPlayer.getPlayerState() != PlayerState.STOPPED) {
            trackPlayer.stop();
        }
        // then release it
        trackPlayer.release();
    }

    public DeezerConnect getmDeezerConnect() {
        return mDeezerConnect;
    }

    /**
     * @param visible if the player UI should be visible
     */
    protected void setPlayerVisible(final boolean visible) {
        if (visible) {
            findViewById(R.id.player).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.player).setVisibility(View.GONE);
        }
    }

    protected void setButtonEnabled(final View button, final boolean enabled) {
        if (enabled) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }
        button.setEnabled(enabled);
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
        ErrorHandler.handleError(getApplicationContext(), "Ошибка при запросе на сервер.", e);
    }

}
