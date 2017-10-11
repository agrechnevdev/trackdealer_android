package com.trackdealer.ui.main.chart;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.trackdealer.R;
import com.trackdealer.helpersUI.ChartShortAdapter;
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IChoseTrack;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by grechnev-av on 11.10.2017.
 */

public class PlaylistDialog extends DialogFragment {

    final String TAG = "PlaylistDialog ";

    @Bind(R.id.list_recycler)
    RecyclerView recyclerView;

    ChartShortAdapter adapter;
    private IChoseTrack iChoseTrack;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setNegativeButton(R.string.close, null);
        builder.setTitle("Список вопроизведения");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.list_recycler, null);
        builder.setView(view);
        ButterKnife.bind(this, view);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        adapter = new ChartShortAdapter(SPlay.init().playList, getContext(), iChoseTrack);
        recyclerView.setAdapter(adapter);
        builder.setCancelable(false);
        return builder.create();
    }

    public void updatePositionIndicator() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        iChoseTrack = (IChoseTrack) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        iChoseTrack = null;
        super.onDetach();
    }
}