package com.trackdealer.ui.mvp;

import com.trackdealer.base.BaseView;
import com.trackdealer.models.UserSettings;

/**
 * Created by anton on 05.01.2018.
 */

public interface UserSettingsView extends BaseView {

    void getUserSettingsSuccess(UserSettings userSettings);

    void getUserSettingsFailed(String error);
}
