package com.trackdealer.net;

import com.trackdealer.module.AppModule;
import com.trackdealer.module.NetModule;
import com.trackdealer.ui.ChartFragment;
import com.trackdealer.ui.FavourFragment;
import com.trackdealer.ui.LoginActivity;
import com.trackdealer.ui.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface NetComponent {

    void inject(MainActivity activity);

    void inject(LoginActivity activity);

    void inject(ChartFragment fragment);

    void inject(FavourFragment fragment);

}