package com.trackdealer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.trackdealer.net.Restapi;
import com.trackdealer.utils.Prefs;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Retrofit;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_LIST;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class ChartFragment extends Fragment {

    private final String TAG = "MainCardsFragment";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    @Bind(R.id.fragment_chart_recycler_view)
    RecyclerView recyclerView;

    ArrayList<TrackInfo> trackList = new ArrayList<>();
    ChartAdapter mTracksAdapter;

    IChoseTrack iChoseTrack;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        ButterKnife.bind(this, view);

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        loadTrackList();

        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        mTracksAdapter = new ChartAdapter(trackList, getActivity().getApplicationContext(), iChoseTrack);
        recyclerView.setAdapter(mTracksAdapter);

        return view;
    }

    public void loadTrackList() {
        trackList.clear();
        trackList.addAll(Prefs.getTrackList(getActivity().getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iChoseTrack = (IChoseTrack) context;
    }
}
