package com.trackdealer.utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.player.exception.DeezerPlayerException;
import com.deezer.sdk.player.exception.InvalidStreamTokenException;
import com.deezer.sdk.player.exception.NotAllowedToPlayThatSongException;
import com.deezer.sdk.player.exception.StreamLimitationException;
import com.deezer.sdk.player.exception.StreamTokenAlreadyDecodedException;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.trackdealer.R;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Response;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_LOG_ERROR;
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
    public static String DEFAULT_SERVER_ERROR_MESSAGE = "Возникла ошибка на сервере";

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
                    String errorMesage = response.errorBody().string();
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

    /**
     * Handle errors by displaying a toast and logging.
     *
     * @param exception the exception that occured while contacting Deezer services.
     */
    public static void handleError(Context context, String messageForUser, final Exception exception) {
        String cause = exception.getCause() != null ? exception.getCause().getMessage() : "";
        if (exception instanceof NotAllowedToPlayThatSongException) {
            messageForUser = "Ошибка! Пользователь не имеет прав для вопроизведения.";
        } else if (exception instanceof StreamTokenAlreadyDecodedException) {
            messageForUser = "Ошибка! Контент уже проигрывался.";
        } else if (exception instanceof InvalidStreamTokenException) {
            messageForUser = "Ошибка! Недопустимый токен потока.";
        } else if (exception instanceof StreamLimitationException) {
            messageForUser = "Ошибка! Аккаунт Deezer используется сразу на нескольких устройствах.";
        } else if (exception instanceof TooManyPlayersExceptions) {
            messageForUser = "Ошибка! Слишком много плееров создано.";
        } else if (exception instanceof DeezerPlayerException) {
            messageForUser = "Ошибка плеера.";
        }  else if (exception instanceof UnknownHostException) {
                messageForUser = "Соединение с сервером не установлено.";
            messageForUser += " Проверьте соединение с интернетом. ";
        } else if (exception instanceof ConnectException) {
            messageForUser = "Ошибка соединения.";
            messageForUser += " Проверьте соединение с интернетом. ";
        } else if (exception instanceof SocketException) {
            messageForUser = "Ошибка при загрузке.";
        } else if (exception instanceof DeezerError) {
            switch (((DeezerError) exception).getErrorCode()){
                case DeezerError.ACCESS_TOKEN_RETRIEVAL_FAILURE :
                case DeezerError.TOKEN_INVALID :
                    messageForUser = "Не удалось войти в Deezer.";
                    break;
                case DeezerError.DATA_NOT_FOUND :
                    messageForUser = "Данные не загружены.";
                    break;
                case DeezerError.OAUTH_FAILURE :
                    messageForUser += "Требуется вход в Deezer аккаунт.";
                    break;
                case DeezerError.MISSING_PERMISSION :
                    messageForUser = "Необходимо разрешение.";
                    break;
                case DeezerError.PARAMETER :
                case DeezerError.PARAMETER_MISSING :
                    messageForUser += "Ошибка запроса.";
                    break;
                case DeezerError.PERMISSION :
                    messageForUser = "Необходимо разрешение.";
                    break;
                case DeezerError.QUERY_INVALID :
                case DeezerError.REQUEST_FAILURE :
                    messageForUser = "Не удалось получить ответ от сервера.";
                    messageForUser += " Проверьте соединение с интернетом. ";
                    break;
                case DeezerError.QUOTA :
                case DeezerError.SERVICE_BUSY :
                    messageForUser = "Сервер перегружен.";
                    break;
                case DeezerError.UNEXPECTED_RESULT :
                case DeezerError.UNKNOWN_FAILURE :
                    messageForUser += " Неизвестная ошибка.";
                    messageForUser += " Проверьте соединение с интернетом. ";
                    break;
                case DeezerError.USER_ID_NOT_FOUND :
                    messageForUser = "Пользователь не найден.";
                    break;
                case 4007 :
                    messageForUser += "Слабые условия приема сигнала.";
                    break;
            }
            cause += ((DeezerError) exception).getMessage() != null ? " Message: " + ((DeezerError) exception).getMessage() + "\n" : "";
            cause += " Code: " + ((DeezerError) exception).getErrorCode() + "\n" ;
            cause += ((DeezerError) exception).getErrorType() != null ? " Type: " + ((DeezerError) exception).getErrorType() : "";
        }



        HashMap<String, String> logMap = Prefs.getHashMap(context, SHARED_FILENAME_USER_DATA, SHARED_KEY_LOG_ERROR);
        if (logMap == null)
            logMap = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        logMap.put(sdf.format(calendar.getTime()), messageForUser + "\n" + cause + "\n" + exception.getClass().getName());
        Prefs.putHashMap(context, SHARED_FILENAME_USER_DATA, SHARED_KEY_LOG_ERROR, logMap);

        Toast toast = Toast.makeText(context, messageForUser, Toast.LENGTH_LONG);
        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(context.getResources().getColor(R.color.colorOrange));
        toast.show();

        Timber.d("ErrorHandler Exception occured " + messageForUser + "\n" + cause + "\n" + exception.getClass().getName());
    }
}
