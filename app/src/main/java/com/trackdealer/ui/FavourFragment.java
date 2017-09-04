package com.trackdealer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.SearchResultOrder;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.CustomAlertDialogBuilder;
import com.trackdealer.helpersUI.SearchTracksAdapter;
import com.trackdealer.interfaces.IClickTrack;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.FakeRestApi;
import com.trackdealer.net.Restapi;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;
import com.trackdealer.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_LIST;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class FavourFragment extends Fragment implements IClickTrack {

    private final String TAG = "FavourFragment ";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    CompositeDisposable compositeDisposable;

    @Bind(R.id.chose_song_lay_main)
    RelativeLayout relLayMain;

    @Bind(R.id.chose_song_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.chose_song_title)
    TextView textSongTitle;

    @Bind(R.id.chose_song_artist)
    TextView textSongArtist;

    @Bind(R.id.chose_song_duration)
    TextView textSongDur;

    @Bind(R.id.chose_song_search)
    EditText textSearch;
    @Bind(R.id.progressbar)
    ProgressBar progressBar;

    ArrayList<TrackInfo> trackList = new ArrayList<>();

    SearchTracksAdapter mTracksAdapter;
    DeezerConnect mDeezerConnect = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favour, container, false);
        ButterKnife.bind(this, view);

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        compositeDisposable = new CompositeDisposable();

        loadFavouriteSongStart();

        initSubscription();

        showTrackList();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDeezerConnect = ((DeezerActivity) context).getmDeezerConnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    public void loadFavouriteSongStart() {
        showProgressBar();
        compositeDisposable.add(FakeRestApi.getFavouriteTrack(getActivity())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        this::loadFavouriteSongSuccess,
                        ex -> Timber.d(TAG + ex.getMessage()),
                        this::hideProgressBar
                ));
    }

    public void loadFavouriteSongSuccess(TrackInfo trackInfo) {
        hideProgressBar();
        setFavouriteSong(trackInfo);
    }

    public void setFavouriteSong(TrackInfo trackInfo) {
        if (trackInfo != null) {
            textSongTitle.setText(trackInfo.getTitle());
            textSongArtist.setText(trackInfo.getArtist());
            textSongDur.setText(trackInfo.getDuration());
        }
    }

    public void initSubscription() {
        compositeDisposable.add(
                RxTextView.textChanges(textSearch)
                        .skip(1)
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .filter(text -> text != null && !TextUtils.isEmpty(text.toString()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(text -> startSearch(text.toString()))
        );
    }


    public void startSearch(String search) {
        showProgressBar();
        DeezerRequest request = DeezerRequestFactory.requestSearchTracks(search, SearchResultOrder.Ranking);
//        Bundle params = new Bundle(1);
//        params.putString("q", search);
//        DeezerRequest request = new DeezerRequest("search/track", params);
        AsyncDeezerTask task = new AsyncDeezerTask(mDeezerConnect,
                new JsonRequestListener() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onResult(final Object result, final Object requestId) {
                        trackList = StaticUtils.fromListTracks((List<Track>) result);

                        showTrackList();
                        hideProgressBar();
                    }

                    @Override
                    public void onUnparsedResult(final String response, final Object requestId) {
//                        handleError(new DeezerError("Unparsed reponse"));
                        hideProgressBar();
                    }

                    @Override
                    public void onException(final Exception exception,
                                            final Object requestId) {
                        hideProgressBar();
//                        handleError(exception);
                    }

                });
        task.execute(request);
    }

    @Override
    public void onClickTrack(TrackInfo trackInfo) {
        if (checkTrackUnique(trackInfo)) {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                    R.string.chose_song_title, R.string.chose_song_message,
                    R.string.yes, (dialog, id) -> {
                setFavouriteSong(trackInfo);
                Prefs.putTrackInfo(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE, trackInfo);
                ArrayList<TrackInfo> list = Prefs.getTrackList(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
                list.add(trackInfo);
                Prefs.putTrackList(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST, list);
            },
                    R.string.no, (dialog, id) -> dialog.dismiss());
            builder.create().show();
        } else {
            ErrorHandler.showSnackbarError(relLayMain, "Песня уже в чарте!");
        }
    }

    public boolean checkTrackUnique(TrackInfo trackInfo) {
        ArrayList<TrackInfo> list = Prefs.getTrackList(getActivity().getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
        for (TrackInfo tr : list) {
            if (tr.getTrackId().equals(trackInfo.getTrackId())) {
                return false;
            }
        }
        return true;

    }

    private void showTrackList() {
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        mTracksAdapter = new SearchTracksAdapter(trackList, getActivity(), this);
        recyclerView.setAdapter(mTracksAdapter);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
