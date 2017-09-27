package com.trackdealer.ui.main.chart;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.ChartAdapter;
import com.trackdealer.interfaces.IChoseTrack;
import com.trackdealer.interfaces.INextSongSetImage;
import com.trackdealer.interfaces.IProvideTrackList;
import com.trackdealer.interfaces.ITrackOperation;
import com.trackdealer.models.PositionPlay;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.Restapi;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class ChartFragment extends Fragment implements ChartView, SwipeRefreshLayout.OnRefreshListener, INextSongSetImage, ITrackOperation {

    private final String TAG = "MainCardsFragment ";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;
    ChartPresenter chartPresenter;

    @Bind(R.id.fragment_chart_swipe_lay_main)
    SwipeRefreshLayout swipeLay;

    @Bind(R.id.fragment_chart_recycler_view)
    RecyclerView recyclerView;

    ArrayList<TrackInfo> trackList = new ArrayList<>();
    ChartAdapter mTracksAdapter;

    IChoseTrack iChoseTrack;
    IProvideTrackList iProvideTrackList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d(TAG + "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ButterKnife.bind(this, view);

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        chartPresenter = new ChartPresenter(restapi, getActivity().getApplicationContext());
        chartPresenter.attachView(this);
        loadTrackListStart();
//        trackList = (ArrayList<TrackInfo>) iProvideTrackList.getTrackList();
//        if (trackList == null) {
//            trackList = new ArrayList<>();
//            loadTrackListStart();
//        }

        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        mTracksAdapter = new ChartAdapter(trackList, getActivity().getApplicationContext(), iChoseTrack, llm);
        recyclerView.setAdapter(mTracksAdapter);
//        recyclerView.addItemDecoration(new SpacesItemDecorator(20));

        swipeLay.setOnRefreshListener(this);
        swipeLay.setColorSchemeResources(R.color.colorAccent);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(trackList != null)
//            iProvideTrackList.provideTrackList(trackList);
        if (mTracksAdapter != null) {
            mTracksAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chartPresenter.detachView();
    }

    @Override
    public void changePos(PositionPlay positionPlay) {
        mTracksAdapter.changePositionIndicator(positionPlay);
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

    public void loadTrackListStart() {
        swipeLay.setRefreshing(true);
        trackList.clear();
        chartPresenter.loadTrackList();
    }

    @Override
    public void loadTrackListSuccess(List<TrackInfo> list) {
        swipeLay.setRefreshing(false);
        trackList.addAll(list);
        iProvideTrackList.provideTrackList(list);
        if (mTracksAdapter != null) {
            mTracksAdapter.updateAdapter(list);
        }
    }

    @Override
    public void loadTrackListFailed(String error) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iChoseTrack = (IChoseTrack) context;
        iProvideTrackList = (IProvideTrackList) context;

    }

    @Override
    public void onRefresh() {
        loadTrackListStart();
    }
}
