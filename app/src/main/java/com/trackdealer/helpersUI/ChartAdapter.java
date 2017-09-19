package com.trackdealer.helpersUI;

/**
 * Created by grechnev-av on 30.08.2017.
 */

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
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
import com.trackdealer.models.PositionPlay;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.Prefs;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;

public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.ViewHolder> {

    private final String TAG = "ChartAdapter ";
    private ArrayList<TrackInfo> trackInfos;
    private Context context;
    private RecyclerView recyclerView;
    private TrackInfo chosenTrackInfo;
    IChoseTrack iChoseTrack;
    LinearLayoutManager llm;
    PositionPlay positionPlay;

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.layout_like_lay_like)
        RelativeLayout relLayLike;
        @Bind(R.id.layout_like_lay_dislike)
        RelativeLayout relLayDislike;
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
        @Bind(R.id.layout_like_image_dislike)
        ImageView imageDislike;
        @Bind(R.id.layout_like_image_like)
        ImageView imageLike;
        @Bind(R.id.layout_like_text_dislike)
        TextView textDislike;
        @Bind(R.id.layout_like_text_like)
        TextView textLike;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public ChartAdapter(ArrayList<TrackInfo> trackInfos, Context context, IChoseTrack iLoadTrack, LinearLayoutManager llm) {
        this.trackInfos = trackInfos;
        this.context = context;
        this.iChoseTrack = iLoadTrack;
        this.llm = llm;
        this.chosenTrackInfo = Prefs.getTrackInfo(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d(TAG + " onCreateViewHolder ");
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chart, parent, false);
        ViewHolder vh = new ViewHolder(v);

        vh.relLayLike.setOnClickListener(view -> {
            vh.imageLike.setColorFilter(context.getResources().getColor(R.color.colorOrange));
            vh.textLike.setTextColor(context.getResources().getColor(R.color.colorOrange));
            vh.relLayDislike.setClickable(false);
        });
        vh.relLayDislike.setOnClickListener(view -> {
            vh.imageDislike.setColorFilter(context.getResources().getColor(R.color.colorAccent));
            vh.textDislike.setTextColor(context.getResources().getColor(R.color.colorAccent));
            vh.relLayLike.setClickable(false);
        });

        return vh;
    }

    public void changePositionIndicator(PositionPlay positionPlay) {
        this.positionPlay = positionPlay;
        notifyItemChanged(positionPlay.oldPos);
        notifyItemChanged(positionPlay.newPos);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Timber.d(TAG + " onBindViewHolder " + position);
        TrackInfo trackInfo = trackInfos.get(position);
        holder.title.setText(trackInfo.getTitle());
        holder.artist.setText(trackInfo.getArtist());
        holder.duration.setText(" " + trackInfo.getDuration());
        holder.textDislike.setText(trackInfo.getDislikes().toString());
        holder.textLike.setText(trackInfo.getLikes().toString());
        holder.textUsername.setText(context.getResources().getString(R.string.chosed_by) + " " + trackInfo.getUser().getUsername());
//        holder.textPosition.setText(Integer.toString(position+1));
        Picasso.with(context).load(trackInfo.getCoverImage()).placeholder(R.drawable.empty_cover).into(holder.artistImage);
        holder.relLayMain.setOnClickListener(view -> {
            iChoseTrack.choseTrackForPlay(trackInfos.get(holder.getAdapterPosition()), holder.getAdapterPosition());
        });
        holder.indicator.setVisibility(View.GONE);
        holder.artistImage.setAlpha(1f);

        if(positionPlay != null){
            if(positionPlay.newPos != -1 && positionPlay.newPos == position) {
                Timber.d(TAG + " position VISIBLE " + position);
                holder.indicator.setVisibility(View.VISIBLE);
                holder.artistImage.setAlpha(0.3f);
            }
        }

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void updateAdapter(ArrayList<TrackInfo> newList) {
        trackInfos = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return trackInfos.size();
    }

}