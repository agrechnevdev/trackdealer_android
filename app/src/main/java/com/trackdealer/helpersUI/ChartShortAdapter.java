package com.trackdealer.helpersUI;

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

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;

/**
 * Created by grechnev-av on 11.10.2017.
 */

public class ChartShortAdapter extends RecyclerView.Adapter<ChartShortAdapter.ViewHolder> {

    private final String TAG = "ChartShortAdapter ";
    private List<TrackInfo> trackInfos;
    private Context context;
    private RecyclerView recyclerView;
    private TrackInfo chosenTrackInfo;
    IChoseTrack iChoseTrack;

    boolean favSongs = false;

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_chart_short_lay_main)
        RelativeLayout relLayMain;
        @Bind(R.id.item_chart_short_user)
        TextView textUsername;
        @Bind(R.id.item_chart_short_image_play)
        ImageView artistImage;
        @Bind(R.id.item_chart_short_play_indicator)
        Indicator indicator;
        @Bind(R.id.item_chart_short_lay_info)
        RelativeLayout relLayInfo;
        @Bind(R.id.item_chart_short_title)
        TextView title;
        @Bind(R.id.item_chart_short_artist)
        TextView artist;
        @Bind(R.id.item_chart_short_duration)
        TextView duration;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public ChartShortAdapter(List<TrackInfo> trackInfos, Context context, IChoseTrack iChoseTrack) {
        this.trackInfos = trackInfos;
        this.context = context;
        this.iChoseTrack = iChoseTrack;
        this.chosenTrackInfo = Prefs.getTrackInfo(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE);

    }

    @Override
    public ChartShortAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Timber.d(TAG + " onCreateViewHolder ");
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chart_short, parent, false);
        ChartShortAdapter.ViewHolder vh = new ChartShortAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ChartShortAdapter.ViewHolder holder, int position) {
//        Timber.d(TAG + " onBindViewHolder " + position);
        TrackInfo trackInfo = trackInfos.get(position);
        holder.title.setText(trackInfo.getTitle());
        holder.artist.setText(trackInfo.getArtist());
        holder.duration.setText(" " + trackInfo.getDuration());

        if (trackInfo.getUser() != null) {
            holder.textUsername.setVisibility(View.VISIBLE);
            holder.textUsername.setText(context.getResources().getString(R.string.chosed_by) + " " + trackInfo.getUser().getUsername());
        } else {
            holder.textUsername.setVisibility(View.GONE);
        }
//        holder.textPosition.setText(Integer.toString(position+1));
        Picasso.with(context).load(trackInfo.getCoverImage()).placeholder(R.drawable.empty_cover).into(holder.artistImage);
        holder.relLayInfo.setOnClickListener(view -> {
            iChoseTrack.choseTrackForPlay(trackInfos.get(holder.getAdapterPosition()), holder.getAdapterPosition());
        });

        if (SPlay.init().playTrackId != null && SPlay.init().playTrackId == trackInfos.get(position).getTrackId()) {
            Timber.d(TAG + " position VISIBLE " + position);
            holder.relLayMain.setBackgroundColor(context.getResources().getColor(R.color.colorLightBlue));
        } else {
            holder.relLayMain.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundTransparent));
        }

        if (SPlay.init().playTrackId != null && SPlay.init().playTrackId == trackInfos.get(position).getTrackId()) {
                Timber.d(TAG + " position VISIBLE " + position);
                holder.indicator.setVisibility(View.VISIBLE);
                holder.artistImage.setAlpha(0.3f);
            } else {
                holder.indicator.setVisibility(View.GONE);
                holder.artistImage.setAlpha(1f);
            }
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