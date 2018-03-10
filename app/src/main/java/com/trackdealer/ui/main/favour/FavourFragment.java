package com.trackdealer.ui.main.favour;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.squareup.picasso.Picasso;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.CustomAlertDialogBuilder;
import com.trackdealer.interfaces.IDispatchTouch;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.DeezerActivity;
import com.trackdealer.ui.mvp.FavourPresenter;
import com.trackdealer.ui.mvp.FavourView;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;
import com.trackdealer.utils.StaticUtils;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;

/**
 * Created by grechnev-av on 31.08.2017.
 */

public class FavourFragment extends Fragment implements FavourView, ISearchDialog, IDispatchTouch {

    private final String TAG = "FavourFragment ";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;

    CompositeDisposable subscription;
    FavourPresenter favourPresenter;

    @Bind(R.id.chose_song_lay_main)
    RelativeLayout relLayMain;

    @Bind(R.id.chose_song_lay_all)
    RelativeLayout relLayAll;

    @Bind(R.id.chose_song_fav_song)
    RelativeLayout relLayFavSong;

    @Bind(R.id.chose_song_title)
    TextView textSongTitle;
    @Bind(R.id.chose_song_artist)
    TextView textSongArtist;
    @Bind(R.id.chose_song_duration)
    TextView textSongDur;
    @Bind(R.id.chose_song_heart)
    ImageView imageViewSong;

    @Bind(R.id.chose_song_fav_song_empty)
    RelativeLayout relLayEmpty;

    @Bind(R.id.chose_song_but_change)
    Button butChange;
    @Bind(R.id.chose_song_like)
    RelativeLayout relLayPercentLike;
    @Bind(R.id.percent_like_text_like)
    TextView textLike;
    @Bind(R.id.percent_like_text_dislike)
    TextView textDisLike;
    @Bind(R.id.percent_like_seekbar)
    SeekBar seekBarLike;

    @Bind(R.id.progressbar)
    ProgressBar progressBar;

    DeezerConnect mDeezerConnect = null;

    SearchDialogFragment searchDialogFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favour, container, false);
        ButterKnife.bind(this, view);

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        subscription = new CompositeDisposable();
        favourPresenter = new FavourPresenter(restapi, getActivity().getApplicationContext());
        favourPresenter.attachView(this);
        loadFavouriteSongStart();

        relLayFavSong.setVisibility(GONE);
        butChange.setVisibility(GONE);
        return view;
    }

    @Override
    public boolean dispatchTouch() {
        return progressBar.getVisibility() == VISIBLE;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDeezerConnect = ((DeezerActivity) context).getmDeezerConnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        subscription.dispose();
        favourPresenter.detachView();
    }

    public void loadFavouriteSongStart() {
        showProgressBar();
        favourPresenter.loadFavourTrack();
    }

    @Override
    public void loadFavourTrackSuccess(TrackInfo trackInfo) {
        hideProgressBar();
        if(trackInfo.getFinished() != null && trackInfo.getFinished()) {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                    0, R.string.congratulations_track_likes,
                    R.string.ok, (dialog, id) -> {});
            builder.create().show();
        }
        setFavouriteSong(trackInfo);
    }

    @Override
    public void loadFavourTrackFailed(String error) {
        hideProgressBar();
        ErrorHandler.handleError(getContext(), error, new Exception(), ((dialog, which) -> loadFavouriteSongStart()));
    }

    public void setFavouriteSong(TrackInfo trackInfo) {
        if (trackInfo != null && trackInfo.getId() != -1) {
            Picasso.with(getContext()).load(trackInfo.getCoverImage()).into(imageViewSong);
            butChange.setVisibility(VISIBLE);
            relLayEmpty.setVisibility(GONE);
            relLayFavSong.setVisibility(VISIBLE);

            textSongTitle.setText(trackInfo.getTitle());
            textSongArtist.setText(trackInfo.getArtist());
            textSongDur.setText(trackInfo.getDuration());
            textSongDur.setText(trackInfo.getDuration());
            textLike.setText(Long.toString(trackInfo.getCountLike()));
            textDisLike.setText(Long.toString(trackInfo.getCountDislike()));
            if(trackInfo.getCountDislike() + trackInfo.getCountLike() != 0) {
                Long progress = trackInfo.getCountDislike() / (trackInfo.getCountDislike() + trackInfo.getCountLike()) * 100;
                seekBarLike.setProgress(progress.intValue());
            } else {
                seekBarLike.setProgress(0);
            }
        } else {
            butChange.setVisibility(GONE);
            relLayEmpty.setVisibility(VISIBLE);
            relLayFavSong.setVisibility(GONE);
        }
    }


    @OnClick({R.id.chose_song_song_empty,R.id.chose_song_but_change})
    public void clickChoseSong() {
        searchDialogFragment = new SearchDialogFragment();
        searchDialogFragment.show(FavourFragment.this.getChildFragmentManager(), "search");
    }

//    @OnClick(R.id.chose_song_but_change)
//    public void clickReChoseSong() {
//        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
//                R.string.chose_song_title, R.string.rechose_song_message,
//                R.string.yes, (dialog, id) -> clickChoseSong(),
//                R.string.no, (dialog, id) -> dialog.dismiss());
//        builder.create().show();
//    }

    @Override
    public void onClickTrack(TrackInfo trackInfo) {
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                R.string.chose_song_title, R.string.chose_song_message,
                R.string.yes,
                (dialog, id) -> {
                    searchDialogFragment.dismiss();
                    saveFavSong(trackInfo);
                },
                R.string.no,
                (dialog, id) -> {
                    dialog.dismiss();
                    searchDialogFragment.dismiss();
                });
        builder.create().show();
    }

    public void saveFavSong(TrackInfo trackInfo) {
        hideKeyboard();
        showProgressBar();
        DeezerRequest request = DeezerRequestFactory.requestAlbum(trackInfo.getAlbumId());
        Observable<String> observableGenre = StaticUtils.requestFromDeezer(mDeezerConnect, request)
                .map(obj -> {
                    if (!((Album) obj).getGenres().isEmpty())
                        return ((Album) obj).getGenres().get(0).getName();
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());

        subscription.add(observableGenre
                .flatMap(genre -> Observable.create(e ->
                        {
                            trackInfo.setGenre(genre);
                            subscription.add(restapi.changeFavTrack(trackInfo)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                            response -> {
                                                Timber.e(TAG + " changeFavTrack response code: " + response.code());
                                                if (response.isSuccessful()) {
                                                    loadFavouriteSongStart();
                                                    Prefs.putTrackInfo(getContext(), SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE, trackInfo);
                                                    ErrorHandler.showToast(getActivity(), "Ваш новый трек добавлен в чарт!");
                                                } else {
                                                    ErrorHandler.showToast(getActivity(), ErrorHandler.getErrorMessageFromResponse(response));
                                                }
                                                hideProgressBar();
                                            },
                                            ex -> {
                                                Timber.e(ex, TAG + " changeFavTrack onError() " + ex.getMessage());
                                                ErrorHandler.showToast(getActivity(), ErrorHandler.DEFAULT_SERVER_ERROR_MESSAGE);
                                                hideProgressBar();
                                            }
                                    ));
                            e.onNext(genre);
                            e.onComplete();
                        })
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showProgressBar() {
        progressBar.setVisibility(VISIBLE);
        relLayAll.setVisibility(GONE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(GONE);
        relLayAll.setVisibility(VISIBLE);
    }
}
