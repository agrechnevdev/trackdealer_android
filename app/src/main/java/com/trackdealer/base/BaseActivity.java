package com.trackdealer.base;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.trackdealer.utils.ErrorHandler;

/**
 * Created by grechnev-av on 12.09.2017.
 */

public class BaseActivity extends AppCompatActivity {

    protected void showError(View v, String message){
        hideProgressBar();
        ErrorHandler.showSnackbarError(v, message);
    }

    protected void showProgressBar(){

    }

    protected void hideProgressBar(){

    }
}
