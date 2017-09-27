package com.trackdealer.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.exception.DeezerPlayerException;
import com.deezer.sdk.player.exception.InvalidStreamTokenException;
import com.deezer.sdk.player.exception.NotAllowedToPlayThatSongException;
import com.deezer.sdk.player.exception.StreamLimitationException;
import com.deezer.sdk.player.exception.StreamTokenAlreadyDecodedException;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.trackdealer.R;
import com.trackdealer.interfaces.IChoseTrack;
import com.trackdealer.interfaces.IConnectDeezer;
import com.trackdealer.models.PositionPlay;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.Prefs;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_LOG_ERROR;


public class DeezerActivity extends AppCompatActivity implements IConnectDeezer, IChoseTrack {

    private final String TAG = "DeezerActivity ";

    private PlayerHandler mPlayerHandler = new PlayerHandler();

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
    protected PositionPlay positionPlay;
    protected Track playingTrack;
    protected List<TrackInfo> trackList;

    protected DeezerConnect mDeezerConnect = null;
    protected TrackPlayer trackPlayer;
    public static final String APP_ID = "250582";
    protected static final String[] PERMISSIONS = new String[]{
            Permissions.BASIC_ACCESS, Permissions.OFFLINE_ACCESS
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        establishDeezerConnect();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
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
        doDestroyPlayer();
    }

    protected int playNextTrack() {
        for (int i = 0; i < trackList.size(); i++) {
            if (trackList.get(i).getTrackId() == playingTrack.getId()) {
                if (i + 1 < trackList.size()) {
                    choseTrackForPlay(trackList.get(i + 1), i + 1);
                    return i + 1;
                } else {
                    choseTrackForPlay(trackList.get(0), 0);
                    return 0;
                }
            }
        }
        return 0;
    }

    protected Integer getPositionPlay() {
        Integer positionPlay = -1;
        for (int i = 0; i < trackList.size(); i++) {
            if (playingTrack != null && playingTrack.getId() == trackList.get(i).getTrackId()) {
                positionPlay = i;
                break;
            }
        }
        return positionPlay;
    }

    @Override
    public void choseTrackForPlay(TrackInfo trackInfo, Integer pos) {
        PlayerState playerState = trackPlayer.getPlayerState();
        if (playingTrack != null && playingTrack.getId() == trackInfo.getTrackId()) {
            if (playerState == PlayerState.PLAYING)
                trackPlayer.pause();
            else if (playerState == PlayerState.PAUSED)
                trackPlayer.play();
        } else {
            if (playerState == PlayerState.PLAYING)
                trackPlayer.stop();
            displayTrackInfo(trackInfo, pos);
            setButtonEnabled(mButtonPlayerPause, false);
            setButtonEnabled(mButtonPlayerSkipForward, false);
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

                Integer oldPos = getPositionPlay();

                playingTrack = (Track) result;

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        trackPlayer.playTrack(playingTrack.getId());
                    }
                }).start();


                Integer newPos = getPositionPlay();
                positionPlay = new PositionPlay(oldPos, newPos);
                EventBus.getDefault().post(positionPlay);

                setButtonEnabled(mButtonPlayerPause, true);
                setButtonEnabled(mButtonPlayerSkipForward, true);

            }

            @Override
            public void onUnparsedResult(final String response, final Object requestId) {
                handleError(new DeezerError("Не удалось обработать ответ от сервера"));
            }

            @Override
            public void onException(final Exception exception,
                                    final Object requestId) {
                handleError(exception);
            }
        });
        task.execute(request);
    }

    protected TrackPlayer recreatePlayer() {
        try {
            doDestroyPlayer();
            trackPlayer = new TrackPlayer(getApplication(), mDeezerConnect, new WifiAndMobileNetworkStateChecker());
            setAttachedPlayer(trackPlayer);
        } catch (TooManyPlayersExceptions | DeezerError e) {
            handleError(e);
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
    }

    public void showPlayerProgress(final long timePosition) {
        mSeekBar.setProgress((int) timePosition / 1000);
    }

    public void showBufferProgress(final int position) {
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
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
                break;
            case INITIALIZING:
                mButtonPlayerPause.setEnabled(true);
                mButtonPlayerPause.setImageResource(R.drawable.ic_play);
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
            runOnUiThread(() -> handleError(ex));
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
                handleError(ex);
                if (ex instanceof NotAllowedToPlayThatSongException) {
                    trackPlayer.skipToNextTrack();
                }
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
     * Handle errors by displaying a toast and logging.
     *
     * @param exception the exception that occured while contacting Deezer services.
     */
    protected void handleError(final Exception exception) {
        String message = exception.getMessage();
        if (exception instanceof NotAllowedToPlayThatSongException) {
            message = "Ошибка! Пользователь не имеет прав для вопроизведения.";
        } else if (exception instanceof StreamTokenAlreadyDecodedException) {
            message = "Ошибка! Контент уже проигрывался.";
        } else if (exception instanceof InvalidStreamTokenException) {
            message = "Ошибка! Недопустимый токен потока.";
        } else if (exception instanceof StreamLimitationException) {
            message = "Ошибка! Аккаунт Deezer используется сразу на нескольких устройствах.";
        } else if (exception instanceof TooManyPlayersExceptions) {
            message = "Ошибка! Слишком много плееров создано.";
        } else if (exception instanceof DeezerPlayerException) {
            message = "Ошибка плеера";
        } else if (exception instanceof DeezerError) {
            if (((DeezerError) exception).getErrorType() != null)
                message = "Ошибка: " + ((DeezerError) exception).toString();
        } else {
            message = exception.getClass().getName();
        }

        HashMap<String, String> logMap = Prefs.getHashMap(this, SHARED_FILENAME_USER_DATA, SHARED_KEY_LOG_ERROR);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        logMap.put(sdf.format(calendar.getTime()), message);

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(Color.RED);
        toast.show();

        Timber.d(TAG + " Exception occured " + message);
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
}
