package com.trackdealer.base;

/**
 * Презентер для BaseView
 * <p>
 * Created by grechnev-av on 20.06.2017.
 */

public class BasePresenter<T extends BaseView> implements Presenter<T> {

    private T baseView;

    @Override
    public void attachView(T mvpView) {
        baseView = mvpView;
    }

    @Override
    public void detachView() {
        baseView = null;
    }

    public boolean isViewAttached() {
        return baseView != null;
    }

    public T getMvpView() {
        return baseView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new BaseViewNotAttachedException();
    }

    public static class BaseViewNotAttachedException extends RuntimeException {
        public BaseViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before" +
                    " requesting data to the Presenter");
        }
    }
}

