package com.trackdealer.helpersUI;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by grechnev-av on 19.07.2017.
 */

public class CustomAlertDialogBuilder extends AlertDialog.Builder {

    public CustomAlertDialogBuilder(Context context) {
        super(context);
    }

    public CustomAlertDialogBuilder(Context context, int title, int message,
                                    int positive, DialogInterface.OnClickListener positiveListener) {
        super(context);
        if (title != 0)
            setTitle(title);
        if (message != 0)
            setMessage(message);
        setPositiveButton(positive, positiveListener);
        setCancelable(false);
    }

    public CustomAlertDialogBuilder(Context context, int title, int message,
                                    int positive, DialogInterface.OnClickListener positiveListener,
                                    int negative, DialogInterface.OnClickListener negativeListener) {
        super(context);
        if (title != 0)
            setTitle(title);
        if (message != 0)
            setMessage(message);
        setPositiveButton(positive, positiveListener);
        setNegativeButton(negative, negativeListener);
        setCancelable(false);
    }

    @Override
    public AlertDialog.Builder setView(int layoutResId) {
        return super.setView(layoutResId);
    }
}