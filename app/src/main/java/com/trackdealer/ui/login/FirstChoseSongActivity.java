package com.trackdealer.ui.login;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.helpersUI.CustomAlertDialogBuilder;
import com.trackdealer.ui.main.favour.FavourFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by anton on 06.01.2018.
 */

public class FirstChoseSongActivity extends BaseActivity {

    ValueAnimator animNewOpt;
    ValueAnimator animatorLike;
    ValueAnimator animatorDislike;

    @Bind(R.id.first_chose_song_text_dislike)
    TextView textDislike;
    @Bind(R.id.first_chose_song_text_like)
    TextView textLike;
    @Bind(R.id.fragment_chart_but_finished)
    ImageView imageFinished;
    @Bind(R.id.fragment_chart_but_random)
    ImageView imageRandom;

    FavourFragment favourFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_chose_song);
        ButterKnife.bind(this);

        favourFragment = FavourFragment.newInstance(false);
        getSupportFragmentManager().beginTransaction().replace(R.id.first_chose_song_fav_song, favourFragment).commit();
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 201);
//        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case 201: {
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
//                            .setTopColorRes(R.color.colorWhite)
//                            .setButtonsColorRes(R.color.colorOrange)
//                            .setIcon(R.drawable.ic_warning_red)
//                            .setTitle(R.string.permisstion_needed_title)
//                            .setMessage(R.string.permisstion_needed_text)
//                            .setPositiveButton(R.string.positive, v -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 201))
//                            .setNegativeButton(R.string.negative, null)
//                            .show();
//                }
//            }
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        startLikeAnimation();
        startDislikeAnimation();
        startNewOptionsAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        animNewOpt.cancel();
        animatorLike.cancel();
        animatorLike.cancel();
    }

    private void startLikeAnimation() {
        animatorLike = ValueAnimator.ofInt(0, 1234);
        animatorLike.setDuration(5000);
        animatorLike.addUpdateListener(animation -> textLike.setText(animation.getAnimatedValue().toString()));
        animatorLike.start();
    }

    private void startDislikeAnimation() {
        animatorDislike = ValueAnimator.ofInt(0, 111);
        animatorDislike.setDuration(3000);
        animatorDislike.addUpdateListener(animation -> textDislike.setText(animation.getAnimatedValue().toString()));
        animatorDislike.start();
    }

    private void startNewOptionsAnimation() {
        animNewOpt = ValueAnimator.ofInt(0, 2000);
        animNewOpt.addUpdateListener(animation -> {
            if ((Integer) animation.getAnimatedValue() < 1000) {
                imageFinished.setColorFilter(getResources().getColor(R.color.colorGrey));
                imageRandom.setColorFilter(getResources().getColor(R.color.colorGrey));
            } else {
                imageFinished.setColorFilter(getResources().getColor(R.color.colorAccent));
                imageRandom.setColorFilter(getResources().getColor(R.color.colorAccent));
            }
        });
        animNewOpt.setRepeatCount(ValueAnimator.INFINITE);
        animNewOpt.setDuration(2000);
        animNewOpt.start();
    }

    @OnClick(R.id.first_chose_but_cont)
    public void clickCont() {
        if (favourFragment.songChosen()) {
            finish();
        } else {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this,
                    0, R.string.song_not_chosen, R.string.ok, null);
            builder.create().show();
        }
    }

    @OnClick({R.id.fragment_chart_but_tracks_main, R.id.fragment_chart_but_deezer, R.id.fragment_chart_but_user_songs, R.id.fragment_chart_but_finished, R.id.fragment_chart_but_random})
    public void clickMenuItem(View view) {

        int message = 0;
        int title = 0;
        switch (view.getId()) {
            case R.id.fragment_chart_but_tracks_main:
                title = R.string.main_chart_text;
                message = R.string.info_list;
                break;
            case R.id.fragment_chart_but_deezer:
                title = R.string.deezer_chart_text;
                message = R.string.info_deezer_list;
                break;
            case R.id.fragment_chart_but_finished:
                title = R.string.finished_chart_text;
                message = R.string.info_finished_list;
                break;
            case R.id.fragment_chart_but_random:
                title = R.string.random_chart_text;
                message = R.string.info_random_list;
                break;
            case R.id.fragment_chart_but_user_songs:
                title = R.string.user_chart_text;
                message = R.string.info_user_list;
                break;
        }

        if (message != 0) {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this,
                    title, message,
                    R.string.ok, (dialog, id) -> {
            }
            );
            builder.create().show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
