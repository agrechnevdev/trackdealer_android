package com.trackdealer.utils;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.trackdealer.R;

import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;

/**
 * Обработчик ошибок
 * <p>
 * Created by grechnev-av on 23.06.2017.
 */

public class ErrorHandler {

    public static String DEFAULT_NETWORK_ERROR_MESSAGE_SHORT = "Возможно отсутствует интернет - подключение";
    public static String DEFAULT_ERROR_MESSAGE_SHORT = "В настоящий момент операция невозможна.";
    public static String DEFAULT_ERROR_MESSAGE = "В настоящий момент операция невозможна.\n\nПожалуйста, повторите попытку позднее.\n\nПриносим извинения за неудобства.";
    public static String DEFAULT_NETWORK_ERROR_MESSAGE = "В настоящий момент операция невозможна.\n\nПожалуйста, проверьте соединение с Интернетом или повторите попытку позднее.\n\nПриносим извинения за неудобства.";

    public static final int DURATION = 5000;
    /**
     * Отображает snackbar
     *
     * @param view    главный layout
     * @param message сообщение для вывода пользователю
     */
    public static void showSnackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setDuration(DURATION);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextSize(12);
        snackbarTextView.setMaxLines(3);
        snackbar.show();
    }

    /**
     * Отображает snackbar для ошибок
     *
     * @param view    главный layout
     * @param message сообщение для вывода пользователю
     */
    public static void showSnackbarError(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorErrorSnackbar));
        snackbar.setDuration(DURATION);
        TextView snackbarTextView = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        snackbarTextView.setTextSize(12);
        snackbarTextView.setMaxLines(3);
        snackbar.show();
    }

    /**
     * Отображает snackbar для ошибок c перезагрузкой страницы
     *
     * @param view          - layout
     * @param message       - сообщение
     * @param actionMessage - сообщение (кнопка)
     * @param listener      - листенер для нажатия на кнопку
     */
    public static void showSnackbarReload(View view, String message, String actionMessage, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorAccentTranslucentAA));
        snackbar.setDuration(DURATION);
//        snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.colorBackground));
        TextView tv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
//        tv.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorBackground));
        snackbar.setAction(actionMessage, listener);
        snackbar.show();
    }

    public static String buildErrorDescription(Throwable throwable) {
        if (throwable instanceof IOException) {
            return DEFAULT_NETWORK_ERROR_MESSAGE;
        }
        return DEFAULT_ERROR_MESSAGE;
    }

    public static String buildErrorDescriptionShort(Throwable throwable) {
        if (throwable instanceof IOException) {
            return DEFAULT_ERROR_MESSAGE_SHORT + DEFAULT_NETWORK_ERROR_MESSAGE_SHORT;
        }
        return DEFAULT_ERROR_MESSAGE_SHORT;
    }

    public static String getErrorMessageFromResponse(Response response) {
        String element = "Ошибка! ";

        int code = response.code();
        switch (code) {
            case HTTP_BAD_REQUEST:
                element = element + "Неверно введены данные!";
                return element;

            case HTTP_FORBIDDEN:
                element = element + "Доступ ограничен!";
                return element;

            default:
                try {
                    JSONObject post = new JSONObject(response.errorBody().string());
                    String errorMesage = post.getString("errorMessage");
                    element = element + " " + errorMesage;
                    if (!TextUtils.isEmpty(errorMesage))
                        return element;
                    else {
                        element = element + DEFAULT_ERROR_MESSAGE_SHORT;
                        return element;
                    }
                } catch (Exception e) {
                    element = element + DEFAULT_ERROR_MESSAGE_SHORT;
                    return element;
                }
        }
    }
}
