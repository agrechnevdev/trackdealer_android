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
import com.trackdealer.helpersUI.PlayListAdapter;
import com.trackdealer.helpersUI.SPlay;
import com.trackdealer.interfaces.IChoseTrack;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.trackdealer.utils.ConstValues.TITLE;

/**
 * Created by grechnev-av on 11.10.2017.
 */

public class PlaylistDialog extends DialogFragment {

    final String TAG = "PlaylistDialog ";

    @Bind(R.id.list_recycler)
    RecyclerView recyclerView;

    PlayListAdapter adapter;
    private IChoseTrack iChoseTrack;

    public static PlaylistDialog newInstance(String title) {
        PlaylistDialog dialogFragment = new PlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putString(TITLE, title);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setNegativeButton(R.string.close, null);
        String title = getArguments().getString(TITLE);
        builder.setTitle(title);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.list_recycler, null);
        builder.setView(view);
        ButterKnife.bind(this, view);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        adapter = new PlayListAdapter(SPlay.init().playList, getContext(), iChoseTrack);
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