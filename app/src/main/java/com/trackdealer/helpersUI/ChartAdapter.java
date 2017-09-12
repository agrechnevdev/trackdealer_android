package com.trackdealer.helpersUI;

/**
 * Created by grechnev-av on 30.08.2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.taishi.library.Indicator;
import com.trackdealer.R;
import com.trackdealer.interfaces.IChoseTrack;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.Prefs;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;

public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.ViewHolder> {

    private ArrayList<TrackInfo> trackInfos;
    private Context context;
    private RecyclerView recyclerView;
    private TrackInfo chosenTrackInfo;
    IChoseTrack iLoadTrack;
    Integer posPlay;

    class ViewHolder extends RecyclerView.ViewHolder {

//        @Bind(R.id.item_chart_text_position)
//        TextView textPosition;
        @Bind(R.id.item_chart_user)
        TextView textUsername;
        @Bind(R.id.item_chart_image_play)
        ImageView artistImage;
        @Bind(R.id.item_chart_play_indicator)
        Indicator indicator;
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

    public ChartAdapter(ArrayList<TrackInfo> trackInfos, Context context, IChoseTrack iLoadTrack) {
        this.trackInfos = trackInfos;
        this.context = context;
        this.iLoadTrack = iLoadTrack;
        this.chosenTrackInfo = Prefs.getTrackInfo(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chart, parent, false);
        ViewHolder vh = new ViewHolder(v);
        vh.relLayMain.setOnClickListener(view -> {
            iLoadTrack.choseTrackForPlay(trackInfos.get(vh.getAdapterPosition()), vh.getAdapterPosition()+1);
        });

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
        holder.textUsername.setText(trackInfo.getUser().getUsername());
//        holder.textPosition.setText(Integer.toString(position+1));
        Picasso.with(context).load(trackInfo.getCoverImage()).placeholder(R.drawable.empty_cover).into(holder.artistImage);

//        holder.relLayMain.setOnClickListener(view -> {
//            if(posPlay != null) {
//                ViewHolder vh = (ChartAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(posPlay);
//                vh.indicator.setVisibility(View.GONE);
//                vh.artistImage.setAlpha(1f);
//            }
//            posPlay = position;
//            iLoadTrack.choseTrackForPlay(trackInfos.get(posPlay), posPlay+1);
//            holder.indicator.setVisibility(View.VISIBLE);
//            holder.artistImage.setAlpha(0.3f);
//        });
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