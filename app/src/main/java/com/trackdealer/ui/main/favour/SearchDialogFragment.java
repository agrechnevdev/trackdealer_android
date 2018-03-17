package com.trackdealer.ui.main.favour;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.SearchResultOrder;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trackdealer.R;
import com.trackdealer.helpersUI.DeezerHelper;
import com.trackdealer.helpersUI.SearchTracksAdapter;
import com.trackdealer.interfaces.IClickTrack;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.ui.main.DeezerActivity;
import com.trackdealer.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by grechnev-av on 23.10.2017.
 */

public class SearchDialogFragment  extends DialogFragment implements IClickTrack {

    final String TAG = "FilterDialogFragment";

    @Bind(R.id.search_recycler)
    RecyclerView recyclerView;

    @Bind(R.id.search_text_search)
    EditText textSearch;

    @Bind(R.id.progressbar)
    ProgressBar progressBar;

    SearchTracksAdapter adapter;
    private ISearchDialog iSearchDialog;
    CompositeDisposable subscription;

    List<TrackInfo> trackList = new ArrayList<>();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setNegativeButton(R.string.close, null);
        builder.setTitle("Твой выбор");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_search, null);
        builder.setView(view);
        ButterKnife.bind(this, view);

        iSearchDialog = (ISearchDialog) getParentFragment();

        showTrackList(trackList);
        initSubscription();

        builder.setCancelable(true);
        return builder.create();
    }

    public void initSubscription() {
        subscription = new CompositeDisposable();
        subscription.add(RxTextView.textChanges(textSearch)
                .filter(text -> text != null && !TextUtils.isEmpty(text.toString()))
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(text -> startSearch(text.toString()))
        );
    }

    public void startSearch(String search) {
        showProgressBar();
        DeezerRequest request = DeezerRequestFactory.requestSearchTracks(search, SearchResultOrder.Ranking);
        subscription.add(StaticUtils.requestFromDeezer(DeezerHelper.init().mDeezerConnect, request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(obj -> {
                            hideProgressBar();
                            List<TrackInfo> trackList = StaticUtils.fromListTracks((List<Track>) obj);
                            showTrackList(trackList);

                        },
                        ex -> hideProgressBar()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        subscription.dispose();
        super.onDismiss(dialog);

    }

    public void showTrackList(List<TrackInfo> list) {
        this.trackList = list;
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        adapter = new SearchTracksAdapter(trackList, getActivity(), this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDetach() {
        iSearchDialog = null;
        super.onDetach();
    }

    @Override
    public void onClickTrack(TrackInfo trackInfo) {
        iSearchDialog.onClickTrack(trackInfo);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}