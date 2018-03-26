package com.trackdealer.utils;

import android.content.Context;
import android.content.DialogInterface;
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
import com.trackdealer.helpersUI.CustomAlertDialogBuilder;

import org.json.JSONException;
import org.json.JSONObject;

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
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

/**
 * Обработчик ошибок
 * <p>
 * Created by grechnev-av on 23.06.2017.
 */

public class ErrorHandler {

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

    public static void showToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public static String buildErrorDescriptionShort(Context context, Throwable throwable) {
        if (throwable instanceof IOException) {
            return context.getString(R.string.default_error) + context.getString(R.string.default_network_error);
        }
        return context.getString(R.string.default_error);
    }

public static String getErrorMessageFromResponse(Context context, Response response) {
        String element = context.getString(R.string.handle_error);

        int code = response.code();
        switch (code) {
            case 600:
                try {
                    JSONObject jObjError = new JSONObject (response.errorBody().string());
                    return (String) jObjError.get("message");
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            case HTTP_BAD_REQUEST:
                element = element + context.getString(R.string.handle_bad_request);
                return element;

            case HTTP_FORBIDDEN:
                element = element + context.getString(R.string.handle_excess_denied);
                return element;

            case HTTP_INTERNAL_ERROR:
                if (response.errorBody() != null)
                    try {
                        String errorMesage = response.errorBody().string();
                        return errorMesage.replaceAll("[^А-Яа-яЁё ]", "");
                    } catch (IOException e) {
                        element = element + context.getString(R.string.default_error);
                        return element;
                    }

            default:
                try {
                    String errorMesage = response.errorBody().string();
                    element = element + " " + errorMesage;
                    if (!TextUtils.isEmpty(errorMesage))
                        return element;
                    else {
                        element = element + context.getString(R.string.default_error);
                        return element;
                    }
                } catch (Exception e) {
                    element = element + context.getString(R.string.default_error);
                    return element;
                }
        }
    }

    /**
     * Handle errors by displaying a toast and logging.
     *
     * @param exception the exception that occured while contacting Deezer services.
     */
    public static void handleError(Context context, String messageForUser, final Exception exception, DialogInterface.OnClickListener repeatListener) {
        String cause = exception.getCause() != null ? exception.getCause().getMessage() : "";
        if (exception instanceof NotAllowedToPlayThatSongException) {
            messageForUser = context.getString(R.string.handle_no_writes_for_play);
        } else if (exception instanceof StreamTokenAlreadyDecodedException) {
            messageForUser = context.getString(R.string.handle_content_already_play);
        } else if (exception instanceof InvalidStreamTokenException) {
            messageForUser = context.getString(R.string.handle_invalid_token);
        } else if (exception instanceof StreamLimitationException) {
            messageForUser = context.getString(R.string.handle_limit_device);
        } else if (exception instanceof TooManyPlayersExceptions) {
            messageForUser = context.getString(R.string.handle_too_much_players);
        } else if (exception instanceof DeezerPlayerException) {
            messageForUser = context.getString(R.string.handle_error_player);
        } else if (exception instanceof UnknownHostException) {
            messageForUser = context.getString(R.string.handle_server_connect_error);
        } else if (exception instanceof ConnectException) {
            messageForUser = context.getString(R.string.handle_connect_error);
        } else if (exception instanceof SocketException) {
            messageForUser = context.getString(R.string.handle_load_error);
        } else if (exception instanceof DeezerError) {
            switch (((DeezerError) exception).getErrorCode()) {
                case DeezerError.ACCESS_TOKEN_RETRIEVAL_FAILURE:
                case DeezerError.TOKEN_INVALID:
                    messageForUser = context.getString(R.string.handle_enter_deezer_error);
                    break;
                case DeezerError.DATA_NOT_FOUND:
                    messageForUser = context.getString(R.string.handle_no_data);
                    break;
                case DeezerError.OAUTH_FAILURE:
                    messageForUser += context.getString(R.string.handle_required_enter_deezer);
                    break;
                case DeezerError.MISSING_PERMISSION:
                    messageForUser = context.getString(R.string.handle_required_permission);
                    break;
                case DeezerError.PARAMETER:
                case DeezerError.PARAMETER_MISSING:
                    messageForUser += context.getString(R.string.handle_request_error);
                    break;
                case DeezerError.PERMISSION:
                    messageForUser = context.getString(R.string.handle_required_permission);
                    break;
                case DeezerError.QUERY_INVALID:
                case DeezerError.REQUEST_FAILURE:
                    messageForUser = context.getString(R.string.handle_no_data_from_server);
                    break;
                case DeezerError.QUOTA:
                case DeezerError.SERVICE_BUSY:
                    messageForUser = context.getString(R.string.handle_server_busy);
                    break;
                case DeezerError.UNEXPECTED_RESULT:
                case DeezerError.UNKNOWN_FAILURE:
                    messageForUser += " " + context.getString(R.string.handle_unknown_error);
                    break;
                case DeezerError.USER_ID_NOT_FOUND:
                    messageForUser = context.getString(R.string.handle_user_not_found);
                    break;
                case 4007:
                    messageForUser += context.getString(R.string.handle_weak_signal);
                    break;
            }
            cause += ((DeezerError) exception).getMessage() != null ? " Message: " + ((DeezerError) exception).getMessage() + "\n" : "";
            cause += " Code: " + ((DeezerError) exception).getErrorCode() + "\n";
            cause += ((DeezerError) exception).getErrorType() != null ? " Type: " + ((DeezerError) exception).getErrorType() : "";
        }


        HashMap<String, String> logMap = Prefs.getHashMap(context, SHARED_FILENAME_USER_DATA, SHARED_KEY_LOG_ERROR);
        if (logMap == null)
            logMap = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        logMap.put(sdf.format(calendar.getTime()), messageForUser + "\n" + cause + "\n" + exception.getClass().getName());
        Prefs.putHashMap(context, SHARED_FILENAME_USER_DATA, SHARED_KEY_LOG_ERROR, logMap);
        if (repeatListener != null) {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(context, 0, messageForUser,
                    R.string.back, (dialog, id) -> {
            }, R.string.repeat, repeatListener);
            builder.create().show();
        } else {
            CustomAlertDialogBuilder builder = new CustomAlertDialogBuilder(context, 0, messageForUser,
                    R.string.ok, (dialog, id) -> {});
            builder.create().show();
        }
//        Toast toast = Toast.makeText(context, messageForUser, Toast.LENGTH_LONG);
//        ((TextView) toast.getView().findViewById(android.R.id.message)).setTextColor(context.getResources().getColor(R.color.colorOrange));
//        toast.show();

        Timber.d("ErrorHandler Exception occured " + messageForUser + "\n" + cause + "\n" + exception.getClass().getName());
    }
}
