package com.trackdealer.ui.mvp;

import com.trackdealer.base.BaseView;

/**
 * Created by anton on 05.01.2018.
 */

public interface UserSettingsView extends BaseView {

    void getUserSettingsSuccess();

    void getUserSettingsFailed(String error);
}
