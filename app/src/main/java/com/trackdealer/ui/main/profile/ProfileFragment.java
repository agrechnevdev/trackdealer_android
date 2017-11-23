package com.trackdealer.ui.main.profile;

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
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.CustomAlertDialogBuilder;
import com.trackdealer.interfaces.IConnectDeezer;
import com.trackdealer.interfaces.IConnected;
import com.trackdealer.interfaces.ILogout;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.DeezerActivity;
import com.trackdealer.ui.main.MainActivity;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;
import timber.log.Timber;

import static com.deezer.sdk.model.User.STATUS_FREEMIUM;
import static com.deezer.sdk.model.User.STATUS_PREMIUM;
import static com.deezer.sdk.model.User.STATUS_PREMIUM_PLUS;
import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_USER;

/**
 * Created by grechnev-av on 05.09.2017.
 */

public class ProfileFragment extends Fragment implements IConnected, ProfileView {

    private final String TAG = "ProfileFragment ";

    @Inject
    Retrofit retrofit;
    private Restapi restapi;
    ProfilePresenter profilePresenter;

    DeezerConnect mDeezerConnect = null;
    IConnectDeezer iConnectDeezer;
    ILogout iLogout;

    @Bind(R.id.profile_text_username)
    TextView textUsername;

    @Bind(R.id.profile_user_text_status)
    TextView textStatus;

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

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        profilePresenter = new ProfilePresenter(restapi, getActivity().getApplicationContext());
        profilePresenter.attachView(this);
        initFields();
        return view;
    }

    public void initFields() {
        textUsername.setText(Prefs.getUser(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER).getUsername());
        textStatus.setText(Prefs.getUser(getContext(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER).getStatus());
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
        iLogout = ((MainActivity) context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profilePresenter.detachView();
    }

    @Override
    public void logoutSuccess() {
        iConnectDeezer.disconnectFromDeezer();
        iLogout.logout();
    }

    @Override
    public void logoutFailed(String error) {
        ErrorHandler.showToast(getActivity(), error);
    }

    @OnClick(R.id.profile_but_deezer_login)
    public void clickLoginDeezer() {
        iConnectDeezer.connectToDeezer();
    }

    @OnClick(R.id.profile_user_but_status)
    public void clickStatusInfo() {
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                R.string.user_status_info_header, R.string.user_status_info,
                R.string.ok, (dialog, id) -> {
        });
        builder.create().show();
    }

    @OnClick(R.id.profile_deezer_but_logout)
    public void clickDeezerLogout() {
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                R.string.exit, R.string.deezer_account_logout,
                R.string.yes, (dialog, id) -> {
            iConnectDeezer.disconnectFromDeezer();
            initFields();
        }, R.string.no, null);
        builder.create().show();
    }

    @OnClick(R.id.profile_but_exit)
    public void clickLogout() {
        CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(getContext(),
                R.string.account_logout_header, R.string.account_logout,
                R.string.yes,
                (dialog, id) -> {
                    profilePresenter.logout();
                }, R.string.no, null);
        builder.create().show();

    }

    @Override
    public void connectSuccess(DeezerConnect deezerConnect) {
        mDeezerConnect = deezerConnect;
        initFields();
    }
}
