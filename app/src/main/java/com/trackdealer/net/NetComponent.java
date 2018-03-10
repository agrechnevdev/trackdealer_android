package com.trackdealer.net;

import com.trackdealer.module.AppModule;
import com.trackdealer.module.NetModule;
import com.trackdealer.ui.login.PreloginActivity;
import com.trackdealer.ui.login.RegisterActivity;
import com.trackdealer.ui.login.StartImageActivity;
import com.trackdealer.ui.main.chart.ChartFragment;
import com.trackdealer.ui.main.favour.FavourFragment;
import com.trackdealer.ui.login.LoginActivity;
import com.trackdealer.ui.main.MainActivity;
import com.trackdealer.ui.main.profile.ProfileFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface NetComponent {

    void inject(StartImageActivity activity);

    void inject(MainActivity activity);

    void inject(RegisterActivity activity);

    void inject(LoginActivity activity);

    void inject(ChartFragment fragment);

    void inject(FavourFragment fragment);

    void inject(ProfileFragment fragment);

}