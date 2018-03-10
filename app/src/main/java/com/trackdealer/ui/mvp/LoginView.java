package com.trackdealer.ui.mvp;

import com.trackdealer.base.BaseView;
import com.trackdealer.models.RMessage;
import com.trackdealer.models.UserSettings;

/**
 * Created by anton on 03.12.2017.
 */

public interface LoginView extends BaseView {

    void loginSuccess();

    void loginFailed(String error);



}
