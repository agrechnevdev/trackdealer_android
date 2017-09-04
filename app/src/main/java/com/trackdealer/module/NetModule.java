package com.trackdealer.module;


import android.app.Application;
import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.trackdealer.utils.AddCookiesInterceptor;
import com.trackdealer.utils.LogResponseInterceptor;
import com.trackdealer.utils.ReceivedCookiesInterceptor;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetModule {

    private String mBaseUrl;
    private Context context;

    public NetModule(String mBaseUrl, Context context) {
        this.mBaseUrl = mBaseUrl;
        this.context = context;
    }

    @Provides
    @Singleton
    @Named("baseUrl")
    String baseUrl() {
        return mBaseUrl;
    }


    @Provides
    @Singleton
    Cache provideHttpCache(Application application) {
        int cacheSize = 10 * 1024 * 1024;
        return new Cache(application.getCacheDir(), cacheSize);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        gsonBuilder.setDateFormat("yyyy-MM-dd hh:mm:ss.S");
        return gsonBuilder.create();
    }

    @Provides
    @Singleton
    OkHttpClient provideOkhttpClient(Cache cache) {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.interceptors().add(new AddCookiesInterceptor(context));
        client.interceptors().add(new ReceivedCookiesInterceptor(context));
        client.interceptors().add(new LogResponseInterceptor(context));
        client.connectTimeout(5, TimeUnit.SECONDS);
        client.cache(cache);
        return client.build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(mBaseUrl)
                .build();
    }
}
