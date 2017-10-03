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
import com.deezer.sdk.network.request.event.DeezerError;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.ChartAdapter;
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IChoseTrack;
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

public class ChartFragment extends Fragment implements ChartView, SwipeRefreshLayout.OnRefreshListener, DialogFilterClickListener, ITrackOperation, ILongClickTrack {

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

    ChartAdapter mTracksAdapter;

    IChoseTrack iChoseTrack;
    ITrackListState iProvideTrackList;

    DeezerConnect mDeezerConnect = null;

    boolean favSongs = false;

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

        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        mTracksAdapter = new ChartAdapter(SPlay.init().showList, getActivity().getApplicationContext(), iChoseTrack, this, this);
        recyclerView.setAdapter(mTracksAdapter);
//        recyclerView.addItemDecoration(new SpacesItemDecorator(20));

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
        iProvideTrackList.updatePosIndicator();
        if (mTracksAdapter != null) {
            mTracksAdapter.updateAdapter(list, false);
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
                            ErrorHandler.handleError(getContext(), "Не удалось добавить песню", (DeezerError) ex);
                            swipeLay.setRefreshing(false);
                        }
                ));
    }

    @OnClick(R.id.fragment_chart_but_random)
    public void clickRandomTrack() {
        iChoseTrack.playRandomTrack();
    }

    @OnClick(R.id.fragment_chart_but_deezer_fav_tracks)
    public void clickDeezerFavTracks() {
        if (!favSongs) {
            changeShowListState(true);
            swipeLay.setRefreshing(true);
            DeezerRequest request = DeezerRequestFactory.requestCurrentUserTracks();
            subscription.add(StaticUtils.requestFromDeezer(mDeezerConnect, request)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(obj -> {
                                SPlay.init().showList = StaticUtils.fromListTracks((List<Track>) obj);
                                if (mTracksAdapter != null) {
                                    mTracksAdapter.updateAdapter(SPlay.init().showList, true);
                                }
                                swipeLay.setRefreshing(false);
                            },
                            ex -> {
                                ErrorHandler.handleError(getContext(), "Не получить список любимых песен", (DeezerError) ex);
                                swipeLay.setRefreshing(false);
                            }
                    ));
        } else {
            changeShowListState(false);
        }
    }

    public void changeShowListState(boolean favSongs) {
        this.favSongs = favSongs;
        if (favSongs) {
            linLayFilter.setClickable(false);
            imageViewFavSongs.setColorFilter(getResources().getColor(R.color.colorOrange));
        } else {
            linLayFilter.setClickable(true);
            imageViewFavSongs.setColorFilter(getResources().getColor(R.color.colorAccent));
            loadTrackListStart("Все");
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
        if (favSongs)
            clickDeezerFavTracks();
        else
            loadTrackListStart(Prefs.getString(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER));
    }
}
