package com.trackdealer.ui.login;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.graphics.drawable.ArgbEvaluator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.helpersUI.CustomAlertDialogBuilder;
import com.trackdealer.ui.main.favour.FavourFragment;
import com.trackdealer.utils.Prefs;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_NOT_FIRST_START;

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

    }

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
            Prefs.putBoolean(this, SHARED_FILENAME_USER_DATA, SHARED_KEY_NOT_FIRST_START, true);
            finish();
        } else {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(this,
                    0, R.string.song_not_chosen,
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
