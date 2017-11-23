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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Genre;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.ChartAdapter;
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IChoseTrack;
import com.trackdealer.interfaces.IDispatchTouch;
import com.trackdealer.interfaces.ILongClickTrack;
import com.trackdealer.interfaces.ITrackListState;
import com.trackdealer.interfaces.ITrackOperation;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.DeezerActivity;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;
import com.trackdealer.utils.StaticUtils;

import java.util.List;

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
import static com.trackdealer.utils.ConstValues.SHARED_KEY_FILTER;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_GENRES;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;

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

    @Bind(R.id.fragment_chart_lay_help)
    RelativeLayout relLayHelpPanel;

    @Bind(R.id.fragment_chart_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.fragment_chart_text_filter)
    TextView textFilter;

    @Bind(R.id.fragment_chart_lay_filter)
    LinearLayout linLayFilter;

    @Bind(R.id.fragment_chart_but_deezer_fav_tracks)
    ImageView imageViewFavSongs;

    @Bind(R.id.fragment_chart_but_deezer_fav_tracks_back)
    ImageView imageViewFavSongsBack;

    @Bind(R.id.empty_recycler)
    RelativeLayout relLayEmpty;

    @Bind(R.id.empty_recycler_text_empty)
    TextView textEmpty;


    ChartAdapter mTracksAdapter;

    IChoseTrack iChoseTrack;
    ITrackListState iProvideTrackList;

    DeezerConnect mDeezerConnect = null;
    LinearLayoutManager layoutManager;

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
        loadTrackListStart(0, Prefs.genre(getContext()));
//        if (SPlay.init().showList == null || SPlay.init().showList.isEmpty()) {
//            loadTrackListStart(0, Prefs.getString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER));
//        }
        changeShowListState(SPlay.init().favSongs);

        swipeLay.setOnRefreshListener(this);
        swipeLay.setColorSchemeResources(R.color.colorAccent);

        if (Prefs.getUser(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER).getStatus().equals("TRACKDEALER"))
            relLayHelpPanel.setVisibility(View.VISIBLE);
        else
            relLayHelpPanel.setVisibility(View.GONE);
        return view;
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
        chartPresenter.trackLike(trackId, like);
    }

    @Override
    public void trackLikeSuccess() {

    }

    @Override
    public void trackLikeFailed(String error) {

    }

    public void loadTrackListStart(int lastNum, String genre) {
        swipeLay.setRefreshing(true);
        mTracksAdapter.setMoreDataAvailable(true);
        if (!SPlay.init().favSongs) {
            chartPresenter.loadTrackList(lastNum, genre);
        } else {
            chartPresenter.loadFavSongs(lastNum, mDeezerConnect);
        }
    }

    public void loadMoreTracks(int lastNum, String genre) {
        Timber.d(TAG + " loadMoreTracks lastNum = " + lastNum);
        SPlay.init().showList.add(new TrackInfo());
        mTracksAdapter.notifyItemInserted(SPlay.init().showList.size() - 1);
        if (!SPlay.init().favSongs) {
            chartPresenter.loadTrackList(lastNum, genre);
        } else {
            chartPresenter.loadFavSongs(lastNum, mDeezerConnect);
        }
    }

    @Override
    public void loadTrackListSuccess(int lastNum, List<TrackInfo> list) {
        Timber.d(TAG + " loadTrackListSuccess lastNum = " + lastNum + " listSize = " + list.size());

        if (lastNum == 0) {
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
            textEmpty.setText("Список пуст");
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
        ErrorHandler.handleError(getContext(), "Не получить список любимых песен.", ex, ((dialog, which) -> loadTrackListStart(lastNum, Prefs.genre(getContext()))));
    }

    @Override
    public void onLongClickTrack(TrackInfo trackInfo) {
        swipeLay.setRefreshing(true);
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserAddTrack(trackInfo.getDeezerId());
        subscription.add(StaticUtils.requestFromDeezer(mDeezerConnect, request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(obj -> {
                            Toast.makeText(getContext(), "Песня добавлена в ваши любимые треки на Deezer \u2764", Toast.LENGTH_LONG).show();
                            swipeLay.setRefreshing(false);
                        },
                        ex -> {
                            ErrorHandler.handleError(getContext(), "Не удалось добавить песню.", (Exception) ex, null);
                            swipeLay.setRefreshing(false);
                        }
                ));
    }

    @OnClick(R.id.fragment_chart_but_random)
    public void clickRandomTrack() {
//        iChoseTrack.playRandomTrack();
        swipeLay.setRefreshing(true);
        mTracksAdapter.setMoreDataAvailable(false);
        chartPresenter.randomList(Prefs.genre(getActivity()));
    }

    @OnClick(R.id.fragment_chart_but_deezer_fav_tracks_back)
    public void clickDeezerFavTracks() {
        SPlay.init().favSongs = !SPlay.init().favSongs;
        changeShowListState(SPlay.init().favSongs);
        loadTrackListStart(0, Prefs.genre(getContext()));
    }

    public void changeShowListState(boolean favSongs) {
        if (favSongs) {
            linLayFilter.setVisibility(View.GONE);
            imageViewFavSongs.setColorFilter(getResources().getColor(R.color.colorOrange));
        } else {
            linLayFilter.setVisibility(View.VISIBLE);
            imageViewFavSongs.setColorFilter(getResources().getColor(R.color.colorAccent));
        }
    }

    @OnClick(R.id.fragment_chart_lay_filter)
    public void clickFilter() {
        List<Genre> genres = Prefs.getGenreList(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_GENRES);
        if (genres == null) {
            swipeLay.setRefreshing(true);
            DeezerRequest request = DeezerRequestFactory.requestGenres();
            subscription.add(StaticUtils.requestFromDeezer(mDeezerConnect, request)
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
        mDeezerConnect = ((DeezerActivity) context).getmDeezerConnect();
    }

    @Override
    public void onRefresh() {
        loadTrackListStart(0, Prefs.genre(getContext()));
    }
}
