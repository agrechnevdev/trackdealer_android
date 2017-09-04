package com.trackdealer.helpersUI;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trackdealer.R;
import com.trackdealer.interfaces.IClickTrack;
import com.trackdealer.models.TrackInfo;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by grechnev-av on 29.08.2017.
 */

public class SearchTracksAdapter extends RecyclerView.Adapter<SearchTracksAdapter.ViewHolder> {

    private ArrayList<TrackInfo> trackInfos;
    private Context context;
    private RecyclerView recyclerView;
    IClickTrack iClickTrack;

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_chart_lay_main)
        RelativeLayout relLayMain;
        @Bind(R.id.item_chart_title)
        TextView title;
        @Bind(R.id.item_chart_artist)
        TextView artist;
        @Bind(R.id.item_chart_duration)
        TextView duration;
        @Bind(R.id.item_chart_text_down)
        TextView textDown;
        @Bind(R.id.item_chart_text_up)
        TextView textUp;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public SearchTracksAdapter(ArrayList<TrackInfo> trackInfos, Context context, IClickTrack iClickTrack) {
        this.trackInfos = trackInfos;
        this.context = context;
        this.iClickTrack = iClickTrack;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chart, parent, false);
        ViewHolder vh = new ViewHolder(v);
        vh.relLayMain.setOnClickListener(view -> iClickTrack.onClickTrack(trackInfos.get(vh.getAdapterPosition())));
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TrackInfo trackInfo = trackInfos.get(position);
        holder.title.setText(trackInfo.getTitle());
        holder.artist.setText(trackInfo.getArtist());
        holder.duration.setText(trackInfo.getDuration());
        holder.textDown.setText(trackInfo.getDislikes().toString());
        holder.textUp.setText(trackInfo.getLikes().toString());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return trackInfos.size();
    }
}