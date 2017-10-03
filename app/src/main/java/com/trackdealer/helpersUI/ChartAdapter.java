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
import com.trackdealer.interfaces.ILongClickTrack;
import com.trackdealer.interfaces.ITrackOperation;
import com.trackdealer.models.TrackInfo;
import com.trackdealer.utils.Prefs;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;

public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.ViewHolder> {

    private final String TAG = "ChartAdapter ";
    private List<TrackInfo> trackInfos;
    private Context context;
    private RecyclerView recyclerView;
    private TrackInfo chosenTrackInfo;
    IChoseTrack iChoseTrack;
    ILongClickTrack iLongClickTrack;
    ITrackOperation iTrackOperation;

    boolean favSongs = false;

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.layout_like)
        RelativeLayout relLayLikeMain;
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
        @Bind(R.id.item_chart_lay_info)
        RelativeLayout relLayInfo;
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

    public ChartAdapter(List<TrackInfo> trackInfos, Context context, IChoseTrack iChoseTrack, ITrackOperation iTrackOperation, ILongClickTrack iLongClickTrack) {
        this.trackInfos = trackInfos;
        this.context = context;
        this.iChoseTrack = iChoseTrack;
        this.iLongClickTrack = iLongClickTrack;
        this.iTrackOperation = iTrackOperation;
        this.chosenTrackInfo = Prefs.getTrackInfo(context, SHARED_FILENAME_TRACK, SHARED_KEY_TRACK_FAVOURITE);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d(TAG + " onCreateViewHolder ");
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chart, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Timber.d(TAG + " onBindViewHolder " + position);
        TrackInfo trackInfo = trackInfos.get(position);
        holder.title.setText(trackInfo.getTitle());
        holder.artist.setText(trackInfo.getArtist());
        holder.duration.setText(" " + trackInfo.getDuration());

        if(trackInfo.getUser() != null) {
            holder.textUsername.setVisibility(View.VISIBLE);
            holder.textUsername.setText(context.getResources().getString(R.string.chosed_by) + " " + trackInfo.getUser().getUsername());
        }
        else{
            holder.textUsername.setVisibility(View.GONE);
        }
//        holder.textPosition.setText(Integer.toString(position+1));
        Picasso.with(context).load(trackInfo.getCoverImage()).placeholder(R.drawable.empty_cover).into(holder.artistImage);
        holder.relLayInfo.setOnClickListener(view -> {
            SPlay.init().playList.clear();
            SPlay.init().playList.addAll(SPlay.init().showList);
            iChoseTrack.choseTrackForPlay(trackInfos.get(holder.getAdapterPosition()), holder.getAdapterPosition());
        });

        if(!favSongs) {
            holder.relLayLikeMain.setVisibility(View.VISIBLE);
            holder.textDislike.setText(trackInfo.getDislikes().toString());
            holder.textLike.setText(trackInfo.getLikes().toString());
            holder.relLayInfo.setOnLongClickListener(v -> {
                iLongClickTrack.onLongClickTrack(trackInfos.get(holder.getAdapterPosition()));
                return true;
            });
            fillNothing(holder);
            if(trackInfo.getUserLike() == null){
                holder.relLayLike.setOnClickListener(view -> {
                    Integer newLike = trackInfo.getLikes() + 1;
                    holder.textLike.setText(newLike.toString());
                    trackInfo.setUserLike(true);
                    fillLikes(holder);
                    iTrackOperation.trackLike(trackInfo.getTrackId(), true);
                });
                holder.relLayDislike.setOnClickListener(view -> {
                    Integer newLike = trackInfo.getDislikes() + 1;
                    holder.textDislike.setText(newLike.toString());
                    trackInfo.setUserLike(false);
                    fillDisLikes(holder);
                    iTrackOperation.trackLike(trackInfo.getTrackId(), false);
                });
            } else if(trackInfo.getUserLike()){
                fillLikes(holder);
            } else {
                fillDisLikes(holder);
            }
        } else {
            holder.relLayLikeMain.setVisibility(View.GONE);
        }

//        if (SPlay.init().positionPlay != null) {
//            if (SPlay.init().positionPlay.newPos != -1 && SPlay.init().positionPlay.newPos == position) {
//                Timber.d(TAG + " position VISIBLE " + position);
//                holder.indicator.setVisibility(View.VISIBLE);
//                holder.artistImage.setAlpha(0.3f);
//            } else {
//                holder.indicator.setVisibility(View.GONE);
//                holder.artistImage.setAlpha(1f);
//            }
//        }


    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void updateAdapter(List<TrackInfo> newList, boolean favSongs) {
        this.favSongs = favSongs;
        trackInfos = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return trackInfos.size();
    }

    private void fillLikes(ViewHolder holder){
        int colorOrange = context.getResources().getColor(R.color.colorOrange);
        holder.imageLike.setColorFilter(colorOrange);
        holder.textLike.setTextColor(colorOrange);
        clickableLikes(holder, false);
    }

    private void fillDisLikes(ViewHolder holder){
        int colorAccent = context.getResources().getColor(R.color.colorAccent);
        holder.imageDislike.setColorFilter(colorAccent);
        holder.textDislike.setTextColor(colorAccent);
        clickableLikes(holder, false);
    }

    private void fillNothing(ViewHolder holder){
        int color = context.getResources().getColor(R.color.colorGrey);
        holder.imageLike.setColorFilter(color);
        holder.textLike.setTextColor(color);
        clickableLikes(holder, true);
        holder.imageDislike.setColorFilter(color);
        holder.textDislike.setTextColor(color);
        clickableLikes(holder, true);
    }

    private void clickableLikes(ViewHolder holder, boolean clickable){
        holder.relLayLike.setClickable(clickable);
        holder.relLayDislike.setClickable(clickable);
    }

}