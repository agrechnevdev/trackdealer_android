package com.trackdealer.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.PlayerWrapper.RepeatMode;
import com.deezer.sdk.player.TrackPlayer;
import com.deezer.sdk.player.event.BufferState;
import com.deezer.sdk.player.event.OnBufferErrorListener;
import com.deezer.sdk.player.event.OnBufferProgressListener;
import com.deezer.sdk.player.event.OnBufferStateChangeListener;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.exception.NotAllowedToPlayThatSongException;
import com.deezer.sdk.player.exception.StreamLimitationException;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.trackdealer.R;
import com.trackdealer.interfaces.IConnectDeezer;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.StaticUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;


public class DeezerActivity extends AppCompatActivity implements IConnectDeezer {

    private final String TAG = "DeezerActivity";

    private PlayerHandler mPlayerHandler = new PlayerHandler();
    private OnClickHandler mOnClickHandler = new OnClickHandler();

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

    private boolean mIsUserSeeking = false;

    @Bind(R.id.text_time)
    TextView mTextTime;
    @Bind(R.id.text_length)
    TextView mTextLength;

    @Bind(R.id.text_artist)
    TextView mTextArtist;
    @Bind(R.id.text_track)
    TextView mTextTrack;


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
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void setContentView(final int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        mButtonPlayerPause.setOnClickListener(mOnClickHandler);
        mButtonPlayerStop.setOnClickListener(mOnClickHandler);
        mButtonPlayerSkipForward.setOnClickListener(mOnClickHandler);
        mButtonPlayerSkipBackward.setOnClickListener(mOnClickHandler);
        mButtonPlayerSeekForward.setOnClickListener(mOnClickHandler);
        mButtonPlayerSeekBackward.setOnClickListener(mOnClickHandler);
        mButtonPlayerRepeat.setOnClickListener(mOnClickHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d(TAG + " onPause() ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d(TAG + " onDestroy() ");
        doDestroyPlayer();
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
            Toast.makeText(this, "Уже залогинен вDeezer ", Toast.LENGTH_LONG).show();
        } else {
            mDeezerConnect.authorize(this, PERMISSIONS, mDeezerDialogListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d(TAG + " onResume() ");
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    /**
     * Handle errors by displaying a toast and logging.
     *
     * @param exception the exception that occured while contacting Deezer services.
     */
    protected void handleError(final Exception exception) {
        String message = exception.getMessage();
        if (TextUtils.isEmpty(message)) {
            message = exception.getClass().getName();
        }

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(Color.RED);
        toast.show();

        Log.e("BaseActivityDee", "Exception occured " + exception.getClass().getName(), exception);
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

    protected void displayTrackInfo(final TrackInfo trackInfo) {
        // artist name
        if ((trackInfo.getArtist() == null) || (trackInfo.getArtist() == null)) {
            mTextArtist.setVisibility(View.GONE);
        } else {
            mTextArtist.setVisibility(View.VISIBLE);
            mTextArtist.setText(trackInfo.getArtist());
        }

        // track name
        if (trackInfo.getTitle() == null) {
            mTextTrack.setVisibility(View.GONE);
        } else {
            mTextTrack.setVisibility(View.VISIBLE);
            mTextTrack.setText(trackInfo.getTitle());
        }
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

    /**
     * Displays the current progression of the playback
     */
    public void showPlayerProgress(final long timePosition) {
        if (!mIsUserSeeking) {
            mSeekBar.setProgress((int) timePosition / 1000);
            String text = StaticUtils.formatTime(timePosition);
            mTextTime.setText(text);
        }

        mSeekBar.setEnabled(false);
    }

    /**
     * Displays the current state of the player
     *
     * @param state the current player's state
     */
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
                mButtonPlayerPause
                        .setEnabled(true /*player.getPosition() != player.getTrackDuration()*/);
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


    /**
     * displays the current progression of the buffer
     *
     * @param position
     */
    public void showBufferProgress(final int position) {
        synchronized (this) {
            if (trackPlayer != null) {
                if (position > 0) {
                    showTrackDuration(trackPlayer.getTrackDuration());
                }
                long progress = (position * trackPlayer.getTrackDuration()) / 100;
                mSeekBar.setSecondaryProgress((int) progress / 1000);
            }
        }
    }

    /**
     * @param trackLength
     */
    public void showTrackDuration(final long trackLength) {
        String text = StaticUtils.formatTime(trackLength);
        mTextLength.setText(text);
        mSeekBar.setMax((int) trackLength / 1000);
    }


    //////////////////////////////////////////////////////////////////////////////////////
    // Click handler 
    //////////////////////////////////////////////////////////////////////////////////////

    private class OnClickHandler implements View.OnClickListener {

        @Override
        public void onClick(final View v) {
            if (v == mButtonPlayerPause) {
                if (trackPlayer.getPlayerState() == PlayerState.PLAYING) {
                    trackPlayer.pause();
                } else {
                    trackPlayer.play();
                }
            } else if (v == mButtonPlayerStop) {
                trackPlayer.stop();
                //setPlayerVisible(false);
            } else if (v == mButtonPlayerSkipForward) {
                onSkipToNextTrack();
            } else if (v == mButtonPlayerSkipBackward) {
                onSkipToPreviousTrack();
            } else if (v == mButtonPlayerSeekBackward) {
                try {
                    trackPlayer.seek(trackPlayer.getPosition() - (10 * 1000));
                } catch (Exception e) {
                    handleError(e);
                }
            } else if (v == mButtonPlayerSeekForward) {
                try {
                    trackPlayer.seek(trackPlayer.getPosition() + (10 * 1000));
                } catch (Exception e) {
                    handleError(e);
                }
            } else if (v == mButtonPlayerRepeat) {
                switchRepeatMode();
            }
        }
    }

    protected void onSkipToNextTrack() {

    }

    protected void onSkipToPreviousTrack() {

    }

    protected void switchRepeatMode() {
        RepeatMode current = trackPlayer.getRepeatMode();
        RepeatMode next;
        String toast;

        switch (current) {
            case NONE:
                next = RepeatMode.ONE;
                toast = "Repeat mode set to : Repeat One";
                break;
            case ONE:
                next = RepeatMode.ALL;
                toast = "Repeat mode set to : Repeat All";
                break;
            case ALL:
            default:
                next = RepeatMode.NONE;
                toast = "Repeat mode set to : No Repeat";
                break;
        }

        trackPlayer.setRepeatMode(next);
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Player Handler
    //////////////////////////////////////////////////////////////////////////////////////


    /**
     * Handler for messages sent by the player and buffer
     */
    private class PlayerHandler
            implements
            OnPlayerProgressListener,
            OnBufferProgressListener,
            OnPlayerStateChangeListener,
            OnPlayerErrorListener,
            OnBufferStateChangeListener,
            OnBufferErrorListener {

        @Override
        public void onBufferError(final Exception ex, final double percent) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    handleError(ex);
                }
            });
        }

        @Override
        public void onBufferStateChange(final BufferState state, final double percent) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    showBufferProgress((int) Math.round(percent));
                }
            });
        }

        @Override
        public void onPlayerError(final Exception ex, final long timePosition) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    handleError(ex);
                    if (ex instanceof NotAllowedToPlayThatSongException) {
                        trackPlayer.skipToNextTrack();
                    } else if (ex instanceof StreamLimitationException) {
                        // Do nothing , 
                    } else {
                        finish();
                    }
                }
            });
        }

        @Override
        public void onPlayerStateChange(final PlayerState state, final long timePosition) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    showPlayerState(state);
                    showPlayerProgress(timePosition);
                }
            });
        }

        @Override
        public void onBufferProgress(final double percent) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    showBufferProgress((int) Math.round(percent));
                }
            });
        }

        @Override
        public void onPlayerProgress(final long timePosition) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    showPlayerProgress(timePosition);
                }
            });
        }
    }

    public DeezerConnect getmDeezerConnect() {
        return mDeezerConnect;
    }
}
