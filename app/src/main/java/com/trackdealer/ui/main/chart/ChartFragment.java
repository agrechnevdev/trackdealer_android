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
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.Genre;
import com.deezer.sdk.model.Track;
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

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class ChartFragment extends Fragment implements ChartView, SwipeRefreshLayout.OnRefreshListener,
        DialogFilterClickListener, ITrackOperation, ILongClickTrack, IDispatchTouch {

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

    @Bind(R.id.fragment_chart_but_deezer_fav_tracks)
    ImageView imageViewFavSongs;

    @Bind(R.id.fragment_chart_but_deezer_fav_tracks_back)
    ImageView imageViewFavSongsBack;

    ChartAdapter mTracksAdapter;

    IChoseTrack iChoseTrack;
    ITrackListState iProvideTrackList;

    DeezerConnect mDeezerConnect = null;
    LinearLayoutManager layoutManager;

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    InfiniteScrollListener infiniteScrollListener;

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

        String genre = Prefs.getString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER);
        if (TextUtils.isEmpty(genre)) {
            Prefs.putString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER, "Все");
            textFilter.setText("Все");
        } else {
            textFilter.setText(genre);
        }
        if (SPlay.init().showList == null || SPlay.init().showList.isEmpty()) {
            loadTrackListStart(Prefs.getString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER));
        }

        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        mTracksAdapter = new ChartAdapter(SPlay.init().showList, getActivity().getApplicationContext(), iChoseTrack, this, this);
        recyclerView.setAdapter(mTracksAdapter);
//        recyclerView.addItemDecoration(new SpacesItemDecorator(20));

        infiniteScrollListener = new InfiniteScrollListener();
        changeShowListState(SPlay.init().favSongs);

        swipeLay.setOnRefreshListener(this);
        swipeLay.setColorSchemeResources(R.color.colorAccent);

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

    public void loadTrackListStart(String genre) {
        swipeLay.setRefreshing(true);
        chartPresenter.loadTrackList(genre);
    }

    @Override
    public void loadTrackListSuccess(List<TrackInfo> list) {
        swipeLay.setRefreshing(false);
        SPlay.init().showList = list;
        SPlay.init().favSongs = false;
        iProvideTrackList.updatePosIndicator();
        if (mTracksAdapter != null) {
            mTracksAdapter.updateAdapter(list);
        }
    }

    @Override
    public void loadTrackListFailed(String error) {

    }

    @Override
    public void onLongClickTrack(TrackInfo trackInfo) {
        swipeLay.setRefreshing(true);
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserAddTrack(trackInfo.getTrackId());
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
        iChoseTrack.playRandomTrack();
    }

    @OnClick(R.id.fragment_chart_but_deezer_fav_tracks_back)
    public void clickDeezerFavTracks() {
        previousTotal = 0;
        if (!SPlay.init().favSongs) {
            changeShowListState(true);
            loadFavSongsStart("0");
        } else {
            changeShowListState(false);
            loadTrackListStart(Prefs.getString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER));
        }
    }

    public void loadFavSongsStart(String index) {
        Timber.d(TAG + " loadFavSongsStart " + index );
        swipeLay.setRefreshing(true);
        if(index.equals("0"))
            SPlay.init().showList.clear();
        DeezerRequest request = DeezerRequestFactory.requestCurrentUserTracks();
        request.addParam("access_token", mDeezerConnect.getAccessToken());
        request.addParam("index", index);
        subscription.add(StaticUtils.requestFromDeezer(mDeezerConnect, request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(obj -> {
                            SPlay.init().showList.addAll(StaticUtils.fromListTracks((List<Track>) obj));
                            SPlay.init().favSongs = true;
                            if (mTracksAdapter != null) {
                                mTracksAdapter.updateAdapter(SPlay.init().showList);
                            }
                            swipeLay.setRefreshing(false);
                        },
                        ex -> {
                            swipeLay.setRefreshing(false);
                            changeShowListState(false);
                            ErrorHandler.handleError(getContext(), "Не получить список любимых песен.", (Exception) ex, ((dialog, which) -> loadFavSongsStart(index)));
                        }
                ));
    }

    public void changeShowListState(boolean favSongs) {
        SPlay.init().favSongs = favSongs;
        if (favSongs) {
            linLayFilter.setVisibility(View.GONE);
            imageViewFavSongs.setColorFilter(getResources().getColor(R.color.colorOrange));
            recyclerView.addOnScrollListener(infiniteScrollListener);
        } else {
            linLayFilter.setVisibility(View.VISIBLE);
            imageViewFavSongs.setColorFilter(getResources().getColor(R.color.colorAccent));
            recyclerView.removeOnScrollListener(infiniteScrollListener);
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
    public void filterClickStart() {
        String genre = Prefs.getString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER);
        textFilter.setText(genre);
        loadTrackListStart(genre);
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
        previousTotal = 0;
        if (SPlay.init().favSongs)
            loadFavSongsStart("0");
        else
            loadTrackListStart(Prefs.getString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER));
    }

    public class InfiniteScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = layoutManager.getItemCount();
            firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                if(SPlay.init().favSongs && SPlay.init().showList.size() % 25 == 0) {
                    loadFavSongsStart(String.valueOf(SPlay.init().showList.size()));
                }
                loading = true;
            }
        }
    }
}
