package com.trackdealer.ui.main.favour;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.AsyncDeezerTask;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.SearchResultOrder;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.squareup.picasso.Picasso;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.CustomAlertDialogBuilder;
import com.trackdealer.helpersUI.SearchTracksAdapter;
import com.trackdealer.interfaces.IClickTrack;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.DeezerActivity;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;
import com.trackdealer.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_LIST;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class FavourFragment extends Fragment implements FavourView, IClickTrack {

    private final String TAG = "FavourFragment ";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    CompositeDisposable compositeDisposable;
    FavourPresenter favourPresenter;

    @Bind(R.id.chose_song_lay_main)
    RelativeLayout relLayMain;

    @Bind(R.id.chose_song_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.chose_song_lay_fav_song_info)
    RelativeLayout relLayFavSong;
    @Bind(R.id.chose_song_title)
    TextView textSongTitle;
    @Bind(R.id.chose_song_artist)
    TextView textSongArtist;
    @Bind(R.id.chose_song_duration)
    TextView textSongDur;
    @Bind(R.id.chose_song_heart)
    ImageView imageViewSong;

    @Bind(R.id.chose_song_song_empty)
    TextView textSongEmpty;

    @Bind(R.id.chose_song_lay_search)
    RelativeLayout relLaySearch;

    @Bind(R.id.chose_song_like)
    RelativeLayout relLayPercentLike;
    @Bind(R.id.percent_like_text_like)
    TextView textLike;
    @Bind(R.id.percent_like_text_dislike)
    TextView textDisLike;
    @Bind(R.id.percent_like_seekbar)
    SeekBar seekBarLike;

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
        favourPresenter = new FavourPresenter(restapi, getActivity().getApplicationContext());
        favourPresenter.attachView(this);
        loadFavouriteSongStart();

        initSubscription();
        relLaySearch.setVisibility(View.GONE);
        relLayFavSong.setVisibility(View.GONE);
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
        favourPresenter.detachView();
    }

    public void loadFavouriteSongStart() {
        showProgressBar();
        favourPresenter.loadFavourTrack();
    }

    @Override
    public void loadFavourTrackSuccess(TrackInfo trackInfo) {
        hideProgressBar();
        setFavouriteSong(trackInfo);
    }

    @Override
    public void loadFavourTrackFailed(String error) {

    }

    public void setFavouriteSong(TrackInfo trackInfo) {
        if (trackInfo != null) {
            Picasso.with(getContext()).load(trackInfo.getCoverImage()).into(imageViewSong);
            relLayFavSong.setVisibility(View.VISIBLE);
            relLayPercentLike.setVisibility(View.VISIBLE);
            textSongEmpty.setVisibility(View.GONE);
            textSongTitle.setText(trackInfo.getTitle());
            textSongArtist.setText(trackInfo.getArtist());
            textSongDur.setText(trackInfo.getDuration());
            textSongDur.setText(trackInfo.getDuration());
            textLike.setText(Integer.toString(trackInfo.getLikes()));
            textDisLike.setText(Integer.toString(trackInfo.getDislikes()));
            Float progress = (float) trackInfo.getDislikes() / (float) (trackInfo.getDislikes() + trackInfo.getLikes()) * 100;
            seekBarLike.setProgress(progress.intValue());
        } else {
            textSongEmpty.setVisibility(View.VISIBLE);
            relLayFavSong.setVisibility(View.GONE);
            relLayPercentLike.setVisibility(View.GONE);
        }
    }

    public void initSubscription() {
        compositeDisposable.add(RxTextView.textChanges(textSearch)
                .filter(text -> text != null && !TextUtils.isEmpty(text.toString()))
                .debounce(1, TimeUnit.SECONDS)
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
                }
        );
        task.execute(request);
    }

    @OnClick(R.id.chose_song_song_empty)
    public void clickChoseSong() {
        relLaySearch.setVisibility(View.VISIBLE);
        textSearch.requestFocus();
        textSearch.setFocusableInTouchMode(true);
    }

    @OnClick(R.id.chose_song_lay_fav_song_info)
    public void clickReChoseSong() {
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                R.string.chose_song_title, R.string.rechose_song_message,
                R.string.yes, (dialog, id) -> clickChoseSong(),
                R.string.no, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onClickTrack(TrackInfo trackInfo) {
        if (checkTrackUnique(trackInfo)) {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                    R.string.chose_song_title, R.string.chose_song_message,
                    R.string.yes, (dialog, id) -> {
                saveFavSong(trackInfo);
            },
                    R.string.no, (dialog, id) -> dialog.dismiss());
            builder.create().show();
        } else {
            ErrorHandler.showSnackbarError(relLayMain, "Песня уже в чарте!");
        }
    }

    public void saveFavSong(TrackInfo trackInfo) {
        hideKeyboard();
        setFavouriteSong(trackInfo);
        trackInfo.setUser(Prefs.getUser(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER));
        Prefs.putTrackInfo(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE, trackInfo);
        ArrayList<TrackInfo> list = Prefs.getTrackList(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
        list.add(trackInfo);
        Prefs.putTrackList(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST, list);
        relLaySearch.setVisibility(View.GONE);
    }

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean checkTrackUnique(TrackInfo trackInfo) {
        ArrayList<TrackInfo> list = Prefs.getTrackList(getActivity().getApplicationContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_LIST);
        for (TrackInfo tr : list) {
            if (tr.getTrackId() == trackInfo.getTrackId()) {
                return false;
            }
        }
        return true;

    }

    private void showTrackList() {
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
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
