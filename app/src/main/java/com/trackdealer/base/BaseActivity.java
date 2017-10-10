package com.trackdealer.base;

import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.trackdealer.utils.ErrorHandler;

/**
 * Created by grechnev-av on 12.09.2017.
 */

public class BaseActivity extends AppCompatActivity {

//    ProgressBar progressBar;
//
//    @Override
//    public void setContentView(@LayoutRes int layoutResID) {
//        LayoutInflater inflater = getLayoutInflater();
//        RelativeLayout relLay = (RelativeLayout) inflater.inflate(R.layout.progressbar, null);
//        progressBar = (ProgressBar) relLay.findViewById(R.id.progressbar);
//
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        relLay.setLayoutParams(layoutParams);
//
//        RelativeLayout screenView = (RelativeLayout) inflater.inflate(layoutResID, null);
//        screenView.addView(relLay);
//        super.setContentView(screenView);
//    }
//
    protected void showError(View v, String message) {
//        hideSearchProgressBar();

    }

//    protected void showSearchProgressBar() {
//        progressBar.setVisibility(View.VISIBLE);
//    }
//
//    protected void hideSearchProgressBar() {
//        progressBar.setVisibility(View.GONE);
//    }
}
