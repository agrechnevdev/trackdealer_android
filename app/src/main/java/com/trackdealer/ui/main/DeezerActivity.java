package com.trackdealer.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.trackdealer.models.ShowPlaylist;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.ui.notification.MediaPlayerService;
import com.trackdealer.utils.ConnectionsManager;
import com.trackdealer.utils.ConstValues;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.StaticUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.ACTION_TO_ACTIVITY;
import static com.trackdealer.utils.ConstValues.ACTION_TO_SERVICE;
import static com.trackdealer.utils.ConstValues.ARTIST;
import static com.trackdealer.utils.ConstValues.BUTTON_PLAY;
import static com.trackdealer.utils.ConstValues.CHANGE_INFO_ACTION;
import static com.trackdealer.utils.ConstValues.NEXT_ACTION;
import static com.trackdealer.utils.ConstValues.RESUME_MAIN_ACTION;
import static com.trackdealer.utils.ConstValues.TYPE_OF_ACTION;
import static com.trackdealer.utils.ConstValues.PAUSE_ACTION;
import static com.trackdealer.utils.ConstValues.PLAY_ACTION;
import static com.trackdealer.utils.ConstValues.STOPFOREGROUND_ACTION;
import static com.trackdealer.utils.ConstValues.TRACK_NAME;


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

    @Bind(R.id.button_playlist)
    protected ImageButton mButtonPlayerPlaylist;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        establishDeezerConnect();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        subscription = new CompositeDisposable();

        final IntentFilter myFilter = new IntentFilter(ACTION_TO_ACTIVITY);
        registerReceiver(mReceiver, myFilter);

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
        mDeezerConnect.logout(this);
        mDeezerConnect = null;
        trackPlayer.removePlayerListener(this);
        doDestroyPlayer();
        subscription.dispose();
        unregisterReceiver(mReceiver);
    }

    protected void playNextTrack() {
        List<TrackInfo> trackList = SPlay.init().playList;
        if (SPlay.init().playingTrack != null) {
            for (int i = 0; i < trackList.size(); i++) {
                if (trackList.get(i).getDeezerId() == SPlay.init().playingTrack.getId()) {
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
                if (pos != SPlay.init().getPosPlayForPlayList(SPlay.init().playingTrack.getId())) {
                    choseTrackForPlay(SPlay.init().playList.get(pos), pos);
                } else {
                    pos = pos == 0 ? SPlay.init().playList.size() - 1 : 0;
                    choseTrackForPlay(SPlay.init().playList.get(pos), pos);
                }
            }
        }
    }


    @Override
    public void choseTrackForPlay(TrackInfo trackInfo, Integer pos) {
        Track playingTrack = SPlay.init().playingTrack;
        PlayerState playerState = trackPlayer.getPlayerState();
        if (playingTrack != null && playingTrack.getId() == trackInfo.getDeezerId()) {
            if (playerState == PlayerState.PLAYING)
                trackPlayer.pause();
            else if (playerState == PlayerState.PAUSED)
                trackPlayer.play();
            else if (playerState == PlayerState.RELEASED)
                recreatePlayer();
        } else {
            if (playerState == PlayerState.PLAYING)
                trackPlayer.stop();
            if(playerState == PlayerState.STARTED) {
                startNotificationPlayer(trackInfo.getArtist(), trackInfo.getTitle(), false);
            }
            // меняем позицию индикатора
            SPlay.init().playTrackId = trackInfo.getDeezerId();
            EventBus.getDefault().post(SPlay.init().playTrackId);

            // отображаем информацию о треке в плеере и кнопки
            displayTrackInfo(trackInfo);
            // грузим песню
            loadSong(trackInfo);
        }
    }

    // грузим загружаем песню
    public void loadSong(TrackInfo trackInfo) {

        if (ConnectionsManager.isConnected(this)) {
            DeezerRequest request = DeezerRequestFactory.requestTrack(trackInfo.getDeezerId());
            subscription.add(StaticUtils.requestFromDeezer(mDeezerConnect, request)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(obj -> {
                                SPlay.init().playingTrack = (Track) obj;

//                                new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        trackPlayer.playTrack(SPlay.init().playingTrack.getId());
//                                    }
//                                }).start();
                                if (trackPlayer.getPlayerState() == PlayerState.RELEASED)
                                    recreatePlayer();
                                trackPlayer.playTrack(SPlay.init().playingTrack.getId());

                                setButtonEnabled(mButtonPlayerPause, true);
                                setButtonEnabled(mButtonPlayerSkipForward, true);
                                setButtonEnabled(mButtonPlayerPlaylist, true);
                            },
                            ex -> {
                                ErrorHandler.handleError(this, "Не удалось загрузить песню.", (Exception) ex, (dialog, which) -> loadSong(trackInfo));
                            }));
        } else {
            ErrorHandler.handleError(this, "Ошибка соединения.", new Exception(), (dialog, which) -> loadSong(trackInfo));
        }
    }

    protected TrackPlayer recreatePlayer() {
        try {
            doDestroyPlayer();
            trackPlayer = new TrackPlayer(getApplication(), mDeezerConnect, new WifiAndMobileNetworkStateChecker());
            trackPlayer.addPlayerListener(this);
            setAttachedPlayer(trackPlayer);
        } catch (TooManyPlayersExceptions | DeezerError e) {
            ErrorHandler.handleError(this, "Ошибка при создании плеера.", e, null);
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

    protected void displayTrackInfo(final TrackInfo trackInfo) {
        //Зануляем прогресс воспроизведения
        showBufferProgress(0);
        showPlayerProgress(0);
        mTextArtist.setText(trackInfo.getArtist());
        mTextTrack.setText(trackInfo.getTitle());
        // блочим кнопки и показываем плеер
        setButtonEnabled(mButtonPlayerPause, false);
        setButtonEnabled(mButtonPlayerSkipForward, false);
        setButtonEnabled(mButtonPlayerPlaylist, false);
        setPlayerVisible(true);

        Intent intent = new Intent(ACTION_TO_SERVICE);
        intent.putExtra(TYPE_OF_ACTION, CHANGE_INFO_ACTION);
        intent.putExtra(ARTIST, trackInfo.getArtist());
        intent.putExtra(TRACK_NAME, trackInfo.getTitle());
        sendBroadcast(intent);
    }

    public void startNotificationPlayer(String artist, String trackName, Boolean buttonPlay){
        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction(ConstValues.STARTFOREGROUND_ACTION);
        intent.putExtra(ARTIST, artist);
        intent.putExtra(TRACK_NAME, trackName);
        intent.putExtra(BUTTON_PLAY, buttonPlay);
        startService(intent);
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

        switch (state) {
            case STARTED:
                break;
            case INITIALIZING:
                break;
            case READY:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                sendMessageToService(PAUSE_ACTION);
                showPlayerProgress(0);
                break;
            case PLAYING:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_pause);
                if(StaticUtils.isServiceRunning(this, MediaPlayerService.class)){
                    Timber.d(TAG + " service running ");
                    sendMessageToService(PLAY_ACTION);
                } else {
                    Timber.d(TAG + " service not running, start ");
                    if(SPlay.init().playingTrack != null) {
                        startNotificationPlayer(SPlay.init().playingTrack.getArtist().getName(), SPlay.init().playingTrack.getTitle(), true);
                    }
                }

                break;
            case PAUSED:
            case PLAYBACK_COMPLETED:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                sendMessageToService(PAUSE_ACTION);
                break;

            case WAITING_FOR_DATA:
                mButtonPlayerPause.setEnabled(false);
                break;

            case STOPPED:
                mSeekBar.setEnabled(false);
                showPlayerProgress(0);
                showBufferProgress(0);
                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                sendMessageToService(PAUSE_ACTION);
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
            runOnUiThread(() -> ErrorHandler.handleError(getApplicationContext(), "Ошибка при загрузке буфера.", ex, null));
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
            runOnUiThread(() -> ErrorHandler.handleError(getApplicationContext(), "Ошибка плеера", ex, null));
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
            new Thread(() -> showBufferProgress((int) Math.round(percent))).start();
//            runOnUiThread(() -> showBufferProgress((int) Math.round(percent)));
        }

        @Override
        public void onPlayerProgress(final long timePosition) {
//            Timber.d(TAG + "onPlayerProgress " + timePosition);
            new Thread(() -> showPlayerProgress(timePosition)).start();
//            runOnUiThread(() -> showPlayerProgress(timePosition));
        }
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.button_pause, R.id.button_skip_forward, R.id.button_playlist})
    public void onClickButton(final View v) {
        if (v == mButtonPlayerPause) {
            if (trackPlayer.getPlayerState() == PlayerState.PLAYING) {
                trackPlayer.pause();
            } else {
                trackPlayer.play();
            }
        } else if (v == mButtonPlayerSkipForward) {
            playNextTrack();
        } else if (v == mButtonPlayerPlaylist) {
            EventBus.getDefault().post(new ShowPlaylist());
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
        sendMessageToService(STOPFOREGROUND_ACTION);
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

    public void sendMessageToService(String action){
        Intent serviceIntent = new Intent(ACTION_TO_SERVICE);
        serviceIntent.putExtra(TYPE_OF_ACTION, action);
        sendBroadcast(serviceIntent);
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
        ErrorHandler.handleError(this, "Ошибка при запросе на сервер.", e, null);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String typeOfAction = intent.getExtras().getString(TYPE_OF_ACTION);
            Timber.d(TAG + " onReceive() " + typeOfAction);
            if (typeOfAction.equals(PLAY_ACTION)) {
                onClickButton(mButtonPlayerPause);
            } else if (typeOfAction.equals(NEXT_ACTION)) {
                onClickButton(mButtonPlayerSkipForward);
            } else if (typeOfAction.equals(STOPFOREGROUND_ACTION)) {

            }  else if (typeOfAction.equals(RESUME_MAIN_ACTION)) {

            }
        }
    };
}
