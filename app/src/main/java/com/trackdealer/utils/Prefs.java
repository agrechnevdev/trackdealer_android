package com.trackdealer.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.models.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by grechnev-av on 06.07.2017.
 */

public class Prefs {

    /**
     * Возвращаем Set
     *
     * @param context  контекст
     * @param prefName наименование SharedPref
     * @param key      ключ поиска
     * @return Set<String>, default = new HashSet<>();
     */
    public static Set<String> getStringSet(Context context, String prefName, String key) {
        return context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).getStringSet(key, new HashSet<>());
    }

    public static void putStringSet(Context context, String prefName, String key, HashSet<String> putSet) {
        context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().putStringSet(key, putSet).apply();
    }

    public static void putTrackInfo(Context context, String prefName, String key, TrackInfo trackInfo) {
        Gson gson = new Gson();
        String json = gson.toJson(trackInfo);
        context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().putString(key, json).apply();
    }

    public static TrackInfo getTrackInfo(Context context, String prefName, String key) {
        String data = context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(key, "");
        Type type = new TypeToken<TrackInfo>() {
        }.getType();
        return new Gson().fromJson(data, type);
    }

    public static void putTrackList(Context context, String prefName, String key, ArrayList<TrackInfo> trackInfos) {
        Gson gson = new Gson();
        String json = gson.toJson(trackInfos);
        context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().putString(key, json).apply();
    }

    public static ArrayList<TrackInfo> getTrackList(Context context, String prefName, String key) {
        String data = context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(key, "");
        Type type = new TypeToken<ArrayList<TrackInfo>>() {
        }.getType();
        return new Gson().fromJson(data, type);
    }

    /**
     * Возвращаем Boolean
     *
     * @param context  контекст
     * @param prefName наименование SharedPref
     * @param key      ключ поиска
     * @return Boolean, default = false
     */
    public static Boolean getBoolean(Context context, String prefName, String key) {
        return context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).getBoolean(key, false);
    }

    public static void putBoolean(Context context, String prefName, String key, Boolean putBoolean) {
        context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().putBoolean(key, putBoolean).apply();
    }

    public static void putHashMap(Context context, String prefName, String key, HashMap<String, String> hashMap) {
        Gson gson = new Gson();
        String json = gson.toJson(hashMap);
        context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().putString(key, json).apply();
    }

    public static HashMap<String, String> getHashMap(Context context, String prefName, String key) {
        String data = context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(key, "");
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        return new Gson().fromJson(data, type);
    }

    public static void putUser(Context context, String prefName, String key, User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().putString(key, json).apply();
    }

    public static User getUser(Context context, String prefName, String key) {
        String data = context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(key, "");
        Type type = new TypeToken<User>() {
        }.getType();
        return new Gson().fromJson(data, type);
    }


    /**
     * Удаляем данные пользователя: пароль, графический ключ, куки
     * @param context
     */
    public static void clearUserData(Context context) {
        context.getApplicationContext().getSharedPreferences("User-Cookie", Context.MODE_PRIVATE).edit().clear().apply();
    }

    /**
     * Возвращаем String
     *
     * @param context  контекст
     * @param prefName наименование SharedPref
     * @param key      ключ поиска
     * @return String, default = "";
     */
    public static String getString(Context context, String prefName, String key) {
        return context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(key, "");
    }

    public static void putString(Context context, String prefName, String key, String putString) {
        context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().putString(key, putString).apply();
    }

    public static Integer getInt(Context context, String prefName, String key) {
        return context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).getInt(key, 0);
    }

    public static void putInt(Context context, String prefName, String key, Integer putString) {
        context.getApplicationContext().getSharedPreferences(prefName, Context.MODE_PRIVATE).edit().putInt(key, putString).apply();
    }
}
