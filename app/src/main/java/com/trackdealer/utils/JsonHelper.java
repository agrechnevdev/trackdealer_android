package com.trackdealer.utils;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

/**
 * Created by grechnev-av on 28.09.2017.
 */

public class JsonHelper<T> {

    /**
     * Кладем объект в интент
     *
     * @param intent    интент
     * @param key       ключ в Bundle
     * @param object    объект
     * @param className класс объекта
     */
    public static void putObjectInIntent(Intent intent, String key, Object object, Class className) {
        Bundle bundle = new Bundle();
        String json = new Gson().toJson(className.cast(object));
        bundle.putString(key, json);
        intent.putExtras(bundle);
    }


    public T getObjectFromIntent(Intent intent, String key, Class<T> tClass) {
        String data = intent.getExtras().getString(key);
        return new Gson().fromJson(data, tClass);
    }

    /**
     * Кладем объект в Bundle
     */
    public static void putObjectInBundle(Bundle bundle, String key, Object object, Class className) {
        String json = new Gson().toJson(className.cast(object));
        bundle.putString(key, json);
    }


    public T getObjectFromBundle(Bundle bundle, String key, Class<T> tClass) {
        String data = bundle.getString(key);
        return new Gson().fromJson(data, tClass);
    }
}
