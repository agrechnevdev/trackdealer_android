package com.trackdealer.ui.main.chart;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Genre;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.ChartAdapter;
import com.trackdealer.helpersUI.CustomAlertDialogBuilder;
import com.trackdealer.helpersUI.DeezerHelper;
import com.trackdealer.helpersUI.PlaylistType;
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IChoseTrack;
import com.trackdealer.interfaces.IDispatchTouch;
import com.trackdealer.interfaces.ILongClickTrack;
import com.trackdealer.interfaces.ITrackListState;
import com.trackdealer.interfaces.ITrackOperation;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.mvp.ChartPresenter;
import com.trackdealer.ui.mvp.ChartView;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;
import com.trackdealer.utils.StaticUtils;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_DEEZER_LIST_HELP;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_FILTER;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_FINISHED_LIST_HELP;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_GENRES;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_MAIN_LIST_HELP;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_RANDOM_LIST_HELP;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER_LIST_HELP;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class ChartFragment extends Fragment implements ChartView, SwipeRefreshLayout.OnRefreshListener,
        FilterDialogGenreListener, ITrackOperation, ILongClickTrack, IDispatchTouch {

    private final String TAG = "MainCardsFragment ";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;
    ChartPresenter chartPresenter;

    CompositeDisposable subscription;

    @Bind(R.id.fragment_chart_swipe_lay_main)
    SwipeRefreshLayout swipeLay;

    @Bind(R.id.fragment_chart_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.fragment_chart_text_filter)
    TextView textFilter;

    @Bind(R.id.fragment_chart_lay_filter)
    LinearLayout linLayFilter;

    @Bind(R.id.fragment_chart_lay_help)
    RelativeLayout relLayHelpPanel;
    @Bind(R.id.fragment_chart_help_title)
    TextView textHelpTitle;
    @Bind(R.id.fragment_chart_but_tracks_main)
    ImageView imageViewTracksMain;
    @Bind(R.id.fragment_chart_but_deezer)
    ImageView imageViewDeezer;
    @Bind(R.id.fragment_chart_but_finished)
    ImageView imageViewFinished;
    @Bind(R.id.fragment_chart_but_random)
    ImageView imageViewRandom;
    @Bind(R.id.fragment_chart_but_user_songs)
    ImageView imageViewuUserSong;

    @Bind(R.id.empty_recycler)
    RelativeLayout relLayEmpty;

    @Bind(R.id.empty_recycler_text_empty)
    TextView textEmpty;
    @Bind(R.id.fragment_chart_username)
    TextView textUserName;

    ChartAdapter mTracksAdapter;

    IChoseTrack iChoseTrack;
    ITrackListState iProvideTrackList;

    LinearLayoutManager layoutManager;

    boolean block = true;
    String username;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d(TAG + "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ButterKnife.bind(this, view);

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        chartPresenter = new ChartPresenter(restapi, getActivity().getApplicationContext());
        chartPresenter.attachView(this);

        subscription = new CompositeDisposable();

        String genre = Prefs.genre(getContext());
        if (TextUtils.isEmpty(genre)) {
            Prefs.putString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER, "Все");
            textFilter.setText("Все");
        } else {
            textFilter.setText(genre);
        }


        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        mTracksAdapter = new ChartAdapter(SPlay.init().showList, getActivity().getApplicationContext(), iChoseTrack, this, this);
        mTracksAdapter.setLoadMoreListener(() -> {
            recyclerView.post(() -> loadMoreTracks(SPlay.init().showList.size(), Prefs.genre(getContext())));
        });

        recyclerView.setAdapter(mTracksAdapter);

        SPlay.init().showList.clear();

        username = Prefs.getUser(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER).getUsername();

        checkUserStatus();
        loadTrackListStart(0, Prefs.genre(getContext()));
        setChosenListState();

        swipeLay.setOnRefreshListener(this);
        swipeLay.setColorSchemeResources(R.color.colorAccent);
        return view;
    }

    public void checkUserStatus() {
        block = !Prefs.getUser(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER).getStatus().equals("TRACKDEALER");
        if (block) {
            imageViewFinished.setColorFilter(getResources().getColor(R.color.colorGrey));
            imageViewRandom.setColorFilter(getResources().getColor(R.color.colorGrey));
        } else {
            imageViewFinished.setColorFilter(getResources().getColor(R.color.colorAccent));
            imageViewRandom.setColorFilter(getResources().getColor(R.color.colorAccent));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SPlay.init().showList != null)
            iProvideTrackList.updatePosIndicator();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chartPresenter.detachView();
        subscription.dispose();
    }

    @Override
    public boolean dispatchTouch() {
        return swipeLay.isRefreshing();
    }

    public void updatePositionIndicator() {
        mTracksAdapter.notifyDataSetChanged();
    }

    @Override
    public void trackLike(long trackId, Boolean like) {
        chartPresenter.operTrackLike(ChartPresenter.TypeLike.ADDLIKE, trackId, like);
    }

    @Override
    public void updateLike(long trackId, Boolean like) {
        chartPresenter.operTrackLike(ChartPresenter.TypeLike.UPDATELIKE, trackId, like);
    }

    @Override
    public void delteLike(long trackId, Boolean like) {
        chartPresenter.operTrackLike(ChartPresenter.TypeLike.DELETELIKE, trackId, like);
    }

    @Override
    public void operLikeSuccess() {

    }

    @Override
    public void operLikeFailed(String error) {

    }

    public void loadTrackListStart(int lastNum, String genre) {
        swipeLay.setRefreshing(true);
        switch (SPlay.init().playlistType) {
            case MAIN:
                chartPresenter.loadTrackList(lastNum, genre);
                break;

            case DEEZER:
                chartPresenter.loadFavSongs(lastNum, DeezerHelper.init().mDeezerConnect);
                break;

            case FINISHED:
                chartPresenter.getPeriodsTracks(0, new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
                break;

            case RANDOM:
                chartPresenter.randomList(genre);
                break;

            case USER:
                chartPresenter.userList(username);
                break;
        }
    }

    public void loadMoreTracks(int lastNum, String genre) {
        Timber.d(TAG + " loadMoreTracks lastNum = " + lastNum);
        SPlay.init().showList.add(new TrackInfo());
        mTracksAdapter.notifyItemInserted(SPlay.init().showList.size() - 1);
        switch (SPlay.init().playlistType) {
            case MAIN:
                chartPresenter.loadTrackList(lastNum, genre);
                break;

            case DEEZER:
                chartPresenter.loadFavSongs(lastNum, DeezerHelper.init().mDeezerConnect);
                break;

            case FINISHED:
                chartPresenter.getPeriodsTracks(lastNum, SPlay.init().showList.get(SPlay.init().showList.size() - 2).getFinishDate());
                break;
        }
    }

    @Override
    public void loadUserListSuccess(int offset, List<TrackInfo> list, String username) {
        textUserName.setVisibility(View.VISIBLE);
        textUserName.setText(getString(R.string.user_tracklist) + " " + username);
        loadTrackListSuccess(offset, list);
    }

    @Override
    public void loadTrackListSuccess(int offset, List<TrackInfo> list) {
        Timber.d(TAG + " loadTrackListSuccess lastNum = " + offset + " listSize = " + list.size());

        if (offset == 0) {
            SPlay.init().showList.clear();
        } else {
            SPlay.init().showList.remove(SPlay.init().showList.size() - 1);
        }

        if (list.isEmpty()) {
            mTracksAdapter.setMoreDataAvailable(false);
        } else {
            SPlay.init().showList.addAll(list);
        }
        mTracksAdapter.notifyDataChanged();
        iProvideTrackList.updatePosIndicator();

        if (SPlay.init().showList.isEmpty()) {
            relLayEmpty.setVisibility(View.VISIBLE);
            textEmpty.setText(getString(R.string.list_is_empty));
        } else {
            relLayEmpty.setVisibility(View.GONE);
        }
        swipeLay.setRefreshing(false);
    }

    @Override
    public void loadTrackListFailed(String error) {
        swipeLay.setRefreshing(false);
        ErrorHandler.showToast(getContext(), error);
    }

    @Override
    public void loadTrackListFailed(Exception ex, int lastNum) {
        if (lastNum == 0)
            swipeLay.setRefreshing(false);
        ErrorHandler.handleError(getActivity(), getString(R.string.handle_fav_song_load_error), ex, ((dialog, which) -> loadTrackListStart(lastNum, Prefs.genre(getContext()))));
    }

    @Override
    public void clickUser(String username) {
        SPlay.init().playlistType = PlaylistType.USER;
        this.username = username;
        initIconColor();
        setChosenListState();
        loadTrackListStart(0, Prefs.genre(getContext()));

    }

    @Override
    public void onLongClickTrack(TrackInfo trackInfo) {
        swipeLay.setRefreshing(true);
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserAddTrack(trackInfo.getDeezerId());
        subscription.add(StaticUtils.requestFromDeezer(DeezerHelper.init().mDeezerConnect, request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(obj -> {
                            Toast.makeText(getContext(), getString(R.string.track_add_in_deezer_favourite), Toast.LENGTH_LONG).show();
                            swipeLay.setRefreshing(false);
                        },
                        ex -> {
                            ErrorHandler.handleError(getActivity(), getString(R.string.track_add_in_deezer_failed), (Exception) ex, (dialog, which) -> onLongClickTrack(trackInfo));
                            swipeLay.setRefreshing(false);
                        }
                ));
    }

    @OnClick({R.id.fragment_chart_but_finished, R.id.fragment_chart_but_random})
    public void clickBlockBut(View view) {
        checkListHelp(view);
        if (block) {
            int message = R.string.finish_but_denied;
            switch (view.getId()) {
                case R.id.fragment_chart_but_finished:
                    loadHelpAnim(PlaylistType.FINISHED.getTitle());
                    message = R.string.finish_but_denied;
                    break;
                case R.id.fragment_chart_but_random:
                    loadHelpAnim(PlaylistType.RANDOM.getTitle());
                    message = R.string.random_but_denied;
                    break;
            }
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                    0, message,
                    R.string.ok, (dialog, id) -> {
            });
            builder.create().show();
        } else {
            clickBut(view);
        }

    }

    @OnClick({R.id.fragment_chart_but_tracks_main, R.id.fragment_chart_but_deezer, R.id.fragment_chart_but_user_songs})
    public void clickBut(View view) {
        checkListHelp(view);
        switch (view.getId()) {
            case R.id.fragment_chart_but_tracks_main:
                SPlay.init().playlistType = PlaylistType.MAIN;
                mTracksAdapter.setMoreDataAvailable(true);
                break;
            case R.id.fragment_chart_but_deezer:
                SPlay.init().playlistType = PlaylistType.DEEZER;
                mTracksAdapter.setMoreDataAvailable(true);
                break;
            case R.id.fragment_chart_but_finished:
                SPlay.init().playlistType = PlaylistType.FINISHED;
                mTracksAdapter.setMoreDataAvailable(true);
                break;
            case R.id.fragment_chart_but_random:
                SPlay.init().playlistType = PlaylistType.RANDOM;
                mTracksAdapter.setMoreDataAvailable(false);
                break;

            case R.id.fragment_chart_but_user_songs:
                SPlay.init().playlistType = PlaylistType.USER;
                mTracksAdapter.setMoreDataAvailable(false);
                break;
        }
        initIconColor();
        setChosenListState();
        loadHelpAnim(SPlay.init().playlistType.getTitle());
        loadTrackListStart(0, Prefs.genre(getContext()));
    }

    public void checkListHelp(View view) {
        int icon = 0;
        int message = 0;
        int title = 0;
        String sharedKey = "shared";
        switch (view.getId()) {
            case R.id.fragment_chart_but_tracks_main:
                sharedKey = SHARED_KEY_MAIN_LIST_HELP;
                icon = R.drawable.app_logo_bold_small;
                title = R.string.main_chart_text;
                message = R.string.info_list;
                break;
            case R.id.fragment_chart_but_deezer:
                sharedKey = SHARED_KEY_DEEZER_LIST_HELP;
                icon = R.drawable.ic_deezer_fav_songs;
                title = R.string.deezer_chart_text;
                message = R.string.info_deezer_list;
                break;
            case R.id.fragment_chart_but_finished:
                sharedKey = SHARED_KEY_FINISHED_LIST_HELP;
                icon = R.drawable.ic_finished_tracks;
                title = R.string.finished_chart_text;
                message = R.string.info_finished_list;
                break;
            case R.id.fragment_chart_but_random:
                sharedKey = SHARED_KEY_RANDOM_LIST_HELP;
                icon = R.drawable.ic_random_cube;
                title = R.string.random_chart_text;
                message = R.string.info_random_list;
                break;

            case R.id.fragment_chart_but_user_songs:
                sharedKey = SHARED_KEY_USER_LIST_HELP;
                icon = R.drawable.ic_user_playlist;
                title = R.string.user_chart_text;
                message = R.string.info_user_list;
                break;
        }

        if (!Prefs.getBoolean(getActivity(), SHARED_FILENAME_USER_DATA, sharedKey)) {
            String finalSharedKey = sharedKey;
            new LovelyStandardDialog(getActivity(), LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                    .setTopColorRes(R.color.colorWhite)
                    .setButtonsColorRes(R.color.colorOrange)
                    .setIcon(icon)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, v -> Prefs.putBoolean(getActivity(), SHARED_FILENAME_USER_DATA, finalSharedKey, true))
                    .show();
        }
    }

    public void initIconColor() {
        imageViewTracksMain.setColorFilter(getResources().getColor(R.color.colorAccent));
        imageViewDeezer.setColorFilter(getResources().getColor(R.color.colorAccent));
        imageViewFinished.setColorFilter(getResources().getColor(R.color.colorAccent));
        imageViewRandom.setColorFilter(getResources().getColor(R.color.colorAccent));
        imageViewuUserSong.setColorFilter(getResources().getColor(R.color.colorAccent));
    }

    public void setChosenListState() {
        textUserName.setVisibility(View.GONE);

        switch (SPlay.init().playlistType) {
            case MAIN:
                imageViewTracksMain.setColorFilter(getResources().getColor(R.color.colorOrange));
                mTracksAdapter.setMoreDataAvailable(true);
                break;

            case DEEZER:
                imageViewDeezer.setColorFilter(getResources().getColor(R.color.colorOrange));
                mTracksAdapter.setMoreDataAvailable(true);
                break;

            case RANDOM:
                imageViewRandom.setColorFilter(getResources().getColor(R.color.colorOrange));
                mTracksAdapter.setMoreDataAvailable(false);
                break;

            case FINISHED:
                imageViewFinished.setColorFilter(getResources().getColor(R.color.colorOrange));
                mTracksAdapter.setMoreDataAvailable(true);
                break;

            case USER:
                textUserName.setVisibility(View.VISIBLE);
                imageViewuUserSong.setColorFilter(getResources().getColor(R.color.colorOrange));
                mTracksAdapter.setMoreDataAvailable(false);
                break;
        }
    }

    public void loadHelpAnim(int typeTitle) {
        Animation in;
        Animation out;
        in = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
        out = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
        out.setStartOffset(2000);

        AlphaAnimation alpha = new AlphaAnimation(0.2F, 0.2F);
        alpha.setDuration(2000);
        alpha.setFillAfter(false);

        textHelpTitle.setText(getString(typeTitle));
        relLayHelpPanel.startAnimation(alpha);
        textHelpTitle.startAnimation(in);
        textHelpTitle.startAnimation(out);
        textHelpTitle.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.fragment_chart_lay_filter)
    public void clickFilter() {
        List<Genre> genres = Prefs.getGenreList(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_GENRES);
        if (genres == null) {
            swipeLay.setRefreshing(true);
            DeezerRequest request = DeezerRequestFactory.requestGenres();
            subscription.add(StaticUtils.requestFromDeezer(DeezerHelper.init().mDeezerConnect, request)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(obj -> {
                                List<Genre> newGenres = (List<Genre>) obj;
                                Prefs.putGenreList(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_GENRES, newGenres);
                                showFilterDialog(newGenres);
                                swipeLay.setRefreshing(false);
                            },
                            ex -> swipeLay.setRefreshing(false)
                    ));
        } else {
            showFilterDialog(genres);
        }
    }

    public void showFilterDialog(List<Genre> genres) {
        FilterDialogFragment alertDialog = FilterDialogFragment.newInstance(genres);
        alertDialog.show(ChartFragment.this.getChildFragmentManager(), "filter");
    }

    @Override
    public void filterGenreClickStart() {
        String genre = Prefs.genre(getContext());
        textFilter.setText(genre);
        loadTrackListStart(0, genre);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iChoseTrack = (IChoseTrack) context;
        iProvideTrackList = (ITrackListState) context;
    }

    @Override
    public void onRefresh() {
        loadTrackListStart(0, Prefs.genre(getContext()));
        setChosenListState();
    }


}
