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
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IChoseTrack;
import com.trackdealer.interfaces.ITrackListState;
import com.trackdealer.interfaces.ITrackOperation;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.Restapi;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class ChartFragment extends Fragment implements ChartView, SwipeRefreshLayout.OnRefreshListener, ITrackOperation {

    private final String TAG = "MainCardsFragment ";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;
    ChartPresenter chartPresenter;

    @Bind(R.id.fragment_chart_swipe_lay_main)
    SwipeRefreshLayout swipeLay;

    @Bind(R.id.fragment_chart_recycler_view)
    RecyclerView recyclerView;

    ChartAdapter mTracksAdapter;

    IChoseTrack iChoseTrack;
    ITrackListState iProvideTrackList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d(TAG + "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ButterKnife.bind(this, view);

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        chartPresenter = new ChartPresenter(restapi, getActivity().getApplicationContext());
        chartPresenter.attachView(this);

        if(SPlay.init().trackList == null || SPlay.init().trackList.isEmpty()){
            loadTrackListStart();
        }

        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        mTracksAdapter = new ChartAdapter(SPlay.init().trackList, getActivity().getApplicationContext(), iChoseTrack, this, llm);
        recyclerView.setAdapter(mTracksAdapter);
//        recyclerView.addItemDecoration(new SpacesItemDecorator(20));

        swipeLay.setOnRefreshListener(this);
        swipeLay.setColorSchemeResources(R.color.colorAccent);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SPlay.init().trackList != null)
            iProvideTrackList.changePosIndicator();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chartPresenter.detachView();
    }

    public void changePositionIndicator() {
        mTracksAdapter.changePositionIndicator();
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
        chartPresenter.loadTrackList();
    }

    @Override
    public void loadTrackListSuccess(List<TrackInfo> list) {
        swipeLay.setRefreshing(false);
        SPlay.init().trackList = list;
        iProvideTrackList.changePosIndicator();
        if (mTracksAdapter != null) {
            mTracksAdapter.updateAdapter(list);
        }
    }

    @Override
    public void loadTrackListFailed(String error) {

    }

//    @OnClick(R.id.fragment_chart_but_random)
//    public void clickRandomTrack(){
//        int pos = new Random().nextInt(SPlay.init().trackList.size());
//        iChoseTrack.choseTrackForPlay(SPlay.init().trackList.get(pos), pos);
//    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iChoseTrack = (IChoseTrack) context;
        iProvideTrackList = (ITrackListState) context;

    }

    @Override
    public void onRefresh() {
        loadTrackListStart();
    }
}
