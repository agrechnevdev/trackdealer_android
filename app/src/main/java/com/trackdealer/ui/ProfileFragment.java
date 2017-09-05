package com.trackdealer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.trackdealer.R;
import com.trackdealer.interfaces.IConnectDeezer;
import com.trackdealer.interfaces.IConnected;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static com.deezer.sdk.model.User.STATUS_FREEMIUM;
import static com.deezer.sdk.model.User.STATUS_PREMIUM;
import static com.deezer.sdk.model.User.STATUS_PREMIUM_PLUS;

/**
 * Created by grechnev-av on 05.09.2017.
 */

public class ProfileFragment extends Fragment implements IConnected {

    private final String TAG = "ProfileFragment ";
    CompositeDisposable compositeDisposable;

    DeezerConnect mDeezerConnect = null;
    IConnectDeezer iConnectDeezer;

    @Bind(R.id.profile_lay_deezer_login)
    RelativeLayout relLayDeezerLogin;

    @Bind(R.id.profile_lay_deezer_logout)
    RelativeLayout relLayDeezerLogout;

    @Bind(R.id.profile_lay_deezer_account)
    RelativeLayout relLayDeezerAccount;

    @Bind(R.id.profile_text_deezer_account_name)
    TextView textAccountName;

    @Bind(R.id.profile_text_deezer_account_status)
    TextView textAccountStatus;

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
         if (!mDeezerConnect.isSessionValid()) {
            relLayDeezerLogin.setVisibility(View.VISIBLE);
            relLayDeezerLogout.setVisibility(View.GONE);
            relLayDeezerAccount.setVisibility(View.GONE);
        } else if(mDeezerConnect.getCurrentUser() != null){
            relLayDeezerLogin.setVisibility(View.GONE);
            relLayDeezerLogout.setVisibility(View.VISIBLE);
            relLayDeezerAccount.setVisibility(View.VISIBLE);
            textAccountName.setText(mDeezerConnect.getCurrentUser().getName());
            String status = null;
            switch (mDeezerConnect.getCurrentUser().getStatus()) {
                case STATUS_FREEMIUM:
                    status = "STATUS_FREEMIUM";
                    break;
                case STATUS_PREMIUM:
                    status = "STATUS_PREMIUM";
                    break;
                case STATUS_PREMIUM_PLUS:
                    status = "STATUS_PREMIUM_PLUS";
                    break;
            }
            textAccountStatus.setText(status);
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
