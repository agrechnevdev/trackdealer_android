package com.trackdealer.ui.main.profile;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.squareup.picasso.Picasso;
import com.trackdealer.BaseApp;
import com.trackdealer.R;
import com.trackdealer.helpersUI.DeezerHelper;
import com.trackdealer.helpersUI.TStatus;
import com.trackdealer.interfaces.IConnectDeezer;
import com.trackdealer.interfaces.IConnected;
import com.trackdealer.interfaces.ILogout;
import com.trackdealer.models.User;
import com.trackdealer.net.Restapi;
import com.trackdealer.ui.main.DeezerActivity;
import com.trackdealer.ui.main.MainActivity;
import com.trackdealer.utils.ErrorHandler;
import com.trackdealer.utils.Prefs;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
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

    CompositeDisposable subscription;

    @Inject
    Retrofit retrofit;
    private Restapi restapi;
    ProfilePresenter profilePresenter;

    IConnectDeezer iConnectDeezer;
    ILogout iLogout;

    @Bind(R.id.profile_text_username)
    TextView textUsername;

    @Bind(R.id.profile_lay_user)
    RelativeLayout userInfo;

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

    @Bind(R.id.profile_text_lay_promo_input)
    TextInputLayout textLayPromo;

    @Bind(R.id.profile_text_promo_input)
    EditText textPromo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d(TAG + "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);

        ((BaseApp) getActivity().getApplication()).getNetComponent().inject(this);
        restapi = retrofit.create(Restapi.class);

        profilePresenter = new ProfilePresenter(restapi, getActivity().getApplicationContext());
        profilePresenter.attachView(this);

        subscription = new CompositeDisposable();

        initFields();
        return view;
    }

    public void initFields() {
        User user = Prefs.getUser(getActivity(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER);
        initUserFields(user);

        if (!DeezerHelper.init().mDeezerConnect.isSessionValid()) {
            relLayDeezerLogin.setVisibility(View.VISIBLE);
            relLayDeezerLogout.setVisibility(View.GONE);
            relLayDeezerAccount.setVisibility(View.GONE);
        } else if (DeezerHelper.init().mDeezerConnect.getCurrentUser() != null) {
            relLayDeezerLogin.setVisibility(View.GONE);
            relLayDeezerLogout.setVisibility(View.VISIBLE);
            relLayDeezerAccount.setVisibility(View.VISIBLE);
            textDeeAccountName.setText(DeezerHelper.init().mDeezerConnect.getCurrentUser().getName());
            String status = null;
            String statusInfo = null;
            switch (DeezerHelper.init().mDeezerConnect.getCurrentUser().getStatus()) {
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
            Picasso.with(getContext()).load(DeezerHelper.init().mDeezerConnect.getCurrentUser().getSmallImageUrl()).into(imageDeeLogo);
        }
    }

    public void promoIdentify(boolean used){
        if(used) {
            textLayPromo.setError(getString(R.string.share_promo_friends));
            textPromo.setText("547512");
            textLayPromo.setEnabled(false);
        } else {
            textLayPromo.setError(getString(R.string.access_promo));
            subscribePromo();
            textLayPromo.setEnabled(true);
        }
    }

    public void initUserFields(User user){
        TStatus tStatus = TStatus.getStatusByName(user.getStatus());
        textUsername.setText(user.getUsername());
        textStatus.setText(tStatus.getName());
        textStatus.setCompoundDrawablePadding(4);
        textStatus.setCompoundDrawablesWithIntrinsicBounds(tStatus.getIcon(), 0, 0, 0);
        promoIdentify(!tStatus.equals(TStatus.TRACKLISTENER));
    }

    public void subscribePromo(){

        subscription.add(
                RxTextView.textChanges(textPromo)
                        .filter(c -> c.length() == 6)
                        .observeOn(Schedulers.computation())
                        .flatMap(c -> restapi.promo(c.toString()))
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    Timber.e(TAG + " promo response code: " + response.code());
                                    if (response.isSuccessful()) {
                                        User user = Prefs.getUser(getActivity(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER);
                                        user.setStatus(TStatus.getUpgradedStatus(user.getStatus()));
                                        Prefs.putUser(getActivity(), SHARED_FILENAME_USER_DATA, SHARED_KEY_USER, user);

                                        subscription.dispose();
                                        initUserFields(user);
                                        ErrorHandler.showToast(getActivity(), getString(R.string.promo_try_new));
                                    } else {
                                        ErrorHandler.showToast(getActivity(), getString(R.string.wrong_promo));
                                        textPromo.setText("");
                                    }
                                },
                                ex -> {
                                    Timber.e(ex, TAG + " promo onError() " + ex.getMessage());
                                    textPromo.setText("");
                                }
                        ));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        iConnectDeezer = ((DeezerActivity) context);
        iLogout = ((MainActivity) context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profilePresenter.detachView();
        subscription.dispose();
    }

    @Override
    public void logoutSuccess() {
        DeezerHelper.init().disconnectFromDeezer(getActivity());
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
        new LovelyCustomDialog(getActivity())
                .setView(R.layout.layout_status)
                .setTopColorRes(R.color.colorWhite)
                .setIcon(R.drawable.ic_info_black)
                .setTitle(R.string.user_status_info_header)
//                .setMessage(R.string.user_status_info)
                .setListener(R.id.layout_button_ok, true, null)
                .show();
    }

    @OnClick(R.id.profile_deezer_but_logout)
    public void clickDeezerLogout() {
        new LovelyStandardDialog(getActivity(), LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                .setTopColorRes(R.color.colorWhite)
                .setButtonsColorRes(R.color.colorOrange)
                .setIcon(R.drawable.ic_warning_red)
                .setTitle(R.string.exit)
                .setMessage(R.string.deezer_account_logout)
                .setPositiveButton(R.string.positive, v -> {
                    DeezerHelper.init().disconnectFromDeezer(getActivity());
                    initFields();
                })
                .setNegativeButton(R.string.negative, null)
                .show();
    }

    @OnClick(R.id.profile_but_exit)
    public void clickLogout() {
        new LovelyStandardDialog(getActivity(), LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                .setTopColorRes(R.color.colorWhite)
                .setButtonsColorRes(R.color.colorOrange)
                .setIcon(R.drawable.ic_warning_red)
                .setTitle(R.string.account_logout_header)
                .setMessage(R.string.account_logout)
                .setPositiveButton(R.string.positive, v -> {
                    profilePresenter.logout();
                })
                .setNegativeButton(R.string.negative, null)
                .show();
    }

    @Override
    public void connectSuccess(DeezerConnect deezerConnect) {
        DeezerHelper.init().mDeezerConnect = deezerConnect;
        initFields();
    }
}
