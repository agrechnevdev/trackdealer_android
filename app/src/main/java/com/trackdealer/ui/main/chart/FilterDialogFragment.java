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

import com.deezer.sdk.model.Genre;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trackdealer.R;
import com.trackdealer.utils.JsonHelper;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by grechnev-av on 27.07.2017.
 */

public class FilterDialogFragment extends DialogFragment implements DialogFilterClickListener {

    final String TAG = "FilterDialogFragment";

    @Bind(R.id.list_recycler)
    RecyclerView recyclerView;

    RecyclerFilterChartAdapter adapter;
    private DialogFilterClickListener dialogInterface;

    public static FilterDialogFragment newInstance(List<Genre> genres) {
        FilterDialogFragment dialogFragment = new FilterDialogFragment();
        Bundle bundle = new Bundle();
        JsonHelper.putObjectInBundle(bundle, "genres", genres, genres.getClass());
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        String data = bundle.getString("genres");
        Type type = new TypeToken<List<Genre>>() {
        }.getType();
        List<Genre> genres = new Gson().fromJson(data, type);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setNegativeButton(R.string.close, null);
        builder.setTitle("Категории");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.list_recycler, null);
        builder.setView(view);
        ButterKnife.bind(this, view);

        dialogInterface = (DialogFilterClickListener) getParentFragment();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(llm);
        adapter = new RecyclerFilterChartAdapter(genres, getContext(), this);
        recyclerView.setAdapter(adapter);
        builder.setCancelable(true);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        dialogInterface = null;
        super.onDetach();
    }

    @Override
    public void filterClickStart() {
        dialogInterface.filterClickStart();
        dismiss();
    }
}