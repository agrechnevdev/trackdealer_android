package com.trackdealer.ui;

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
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.FakeRestApi;
import com.trackdealer.net.Restapi;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class ChartFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private final String TAG = "MainCardsFragment ";
    CompositeDisposable compositeDisposable;
    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    @Bind(R.id.fragment_chart_swipe_lay_main)
    SwipeRefreshLayout swipeLay;

    @Bind(R.id.fragment_chart_recycler_view)
    RecyclerView recyclerView;

    ArrayList<TrackInfo> trackList = new ArrayList<>();
    ChartAdapter mTracksAdapter;

    IChoseTrack iChoseTrack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d(TAG + "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ButterKnife.bind(this, view);

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        compositeDisposable = new CompositeDisposable();
        loadTrackListStart();

        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        mTracksAdapter = new ChartAdapter(trackList, getActivity().getApplicationContext(), iChoseTrack);
        recyclerView.setAdapter(mTracksAdapter);

        swipeLay.setOnRefreshListener(this);
        swipeLay.setColorSchemeResources(R.color.colorAccent);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    public void loadTrackListStart() {
        swipeLay.setRefreshing(true);
        trackList.clear();
        compositeDisposable.add(FakeRestApi.getChartTrack(getActivity())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        this::loadTrackListSuccess,
                        ex -> Timber.d(TAG + ex.getMessage())
                ));
    }

    public void loadTrackListSuccess(ArrayList<TrackInfo> list) {
        swipeLay.setRefreshing(false);
        trackList.addAll(list);
        if (mTracksAdapter != null)
            mTracksAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iChoseTrack = (IChoseTrack) context;
    }

    @Override
    public void onRefresh() {
        loadTrackListStart();
    }
}
