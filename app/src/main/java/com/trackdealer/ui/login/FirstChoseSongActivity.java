package com.trackdealer.ui.login;

import android.os.Bundle;

import com.trackdealer.R;
import com.trackdealer.base.BaseActivity;
import com.trackdealer.ui.main.favour.FavourFragment;
import com.trackdealer.utils.Prefs;

import butterknife.ButterKnife;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_NOT_FIRST_START;

/**
 * Created by anton on 06.01.2018.
 */

public class FirstChoseSongActivity extends BaseActivity {


//    @Inject
//    Retrofit retrofit;
//    private Restapi restapi;

    FavourFragment favourFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_chose_song);
        ButterKnife.bind(this);
        // restore existing deezer Connection

//        ((BaseApp) getApplication()).getNetComponent().inject(this);
//        restapi = retrofit.create(Restapi.class);
        favourFragment = new FavourFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.first_chose_song_fav_song, favourFragment).commit();

//        Prefs.putBoolean(this, SHARED_FILENAME_USER_DATA, SHARED_KEY_NOT_FIRST_START, true);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
