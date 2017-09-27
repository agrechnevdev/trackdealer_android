package com.trackdealer.base;

/**
 * Интерфейс презентера
 * <p>
 * Created by grechnev-av on 20.06.2017.
 */

public interface Presenter<V extends BaseView> {

    void attachView(V baseView);

    void detachView();
}