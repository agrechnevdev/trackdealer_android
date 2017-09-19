package com.trackdealer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.squareup.picasso.Picasso;
import com.trackdealer.R;
import com.trackdealer.interfaces.IConnectDeezer;
import com.trackdealer.interfaces.IConnected;
import com.trackdealer.utils.Prefs;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static com.deezer.sdk.model.User.STATUS_FREEMIUM;
import static com.deezer.sdk.model.User.STATUS_PREMIUM;
import static com.deezer.sdk.model.User.STATUS_PREMIUM_PLUS;
import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;

/**
 * Created by grechnev-av on 05.09.2017.
 */

public class ProfileFragment extends Fragment implements IConnected {

    private final String TAG = "ProfileFragment ";
    CompositeDisposable compositeDisposable;

    DeezerConnect mDeezerConnect = null;
    IConnectDeezer iConnectDeezer;

    @Bind(R.id.profile_text_username)
    TextView textUsername;

    @Bind(R.id.profile_lay_deezer_login)
    RelativeLayout relLayDeezerLogin;

    @Bind(R.id.profile_lay_deezer_logout)
    RelativeLayout relLayDeezerLogout;

    @Bind(R.id.profile_lay_deezer_account)
    RelativeLayout relLayDeezerAccount;

    @Bind(R.id.profile_text_deezer_account_name)
    TextView textDeeAccountName;

    @Bind(R.id.profile_text_deezer_account_status)
    TextView textDeeAccountStatus;

    @Bind(R.id.profile_text_status_info)
    TextView textDeeStatusInfo;

    @Bind(R.id.profile_deezer_account_user_logo)
    ImageView imageDeeLogo;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d(TAG + "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

//        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
//        restapi = retrofit.create(Restapi.class);

        initFields();
        compositeDisposable = new CompositeDisposable();
        return view;
    }

    public void initFields() {
        textUsername.setText(Prefs.getUser(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER).getUsername());
        if (!mDeezerConnect.isSessionValid()) {
            relLayDeezerLogin.setVisibility(View.VISIBLE);
            relLayDeezerLogout.setVisibility(View.GONE);
            relLayDeezerAccount.setVisibility(View.GONE);
        } else if (mDeezerConnect.getCurrentUser() != null) {
            relLayDeezerLogin.setVisibility(View.GONE);
            relLayDeezerLogout.setVisibility(View.VISIBLE);
            relLayDeezerAccount.setVisibility(View.VISIBLE);
            textDeeAccountName.setText(mDeezerConnect.getCurrentUser().getName());
            String status = null;
            String statusInfo = null;
            switch (mDeezerConnect.getCurrentUser().getStatus()) {
                case STATUS_FREEMIUM:
                    status = "Deezer Freemium";
                    statusInfo = getString(R.string.deezer_freemium_account);
                    break;
                case STATUS_PREMIUM:
                    status = "Deezer Premium";
                    statusInfo = getString(R.string.deezer_premium_account);
                    break;
                case STATUS_PREMIUM_PLUS:
                    status = "Deezer Premium +";
                    statusInfo = getString(R.string.deezer_premium_account);
                    break;
            }
            textDeeAccountStatus.setText(status);
            textDeeStatusInfo.setText(statusInfo);
            Picasso.with(getContext()).load(mDeezerConnect.getCurrentUser().getSmallImageUrl()).into(imageDeeLogo);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDeezerConnect = ((DeezerActivity) context).getmDeezerConnect();
        iConnectDeezer = ((DeezerActivity) context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    @OnClick(R.id.profile_but_deezer_login)
    public void clickLogin() {
        iConnectDeezer.connectToDeezer();
    }

    @OnClick(R.id.profile_deezer_but_logout)
    public void clickLogout() {
        iConnectDeezer.disconnectFromDeezer();
        initFields();
    }

    @Override
    public void connectSuccess(DeezerConnect deezerConnect) {
        mDeezerConnect = deezerConnect;
        initFields();
    }
}
