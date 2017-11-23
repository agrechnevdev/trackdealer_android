package com.trackdealer.ui.main.profile;

import com.trackdealer.base.BaseView;

/**
 * Created by grechnev-av on 22.11.2017.
 */

public interface ProfileView extends BaseView{

    void logoutSuccess();

    void logoutFailed(String error);
}
