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
import com.trackdealer.utils.StaticUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_TRACK;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_TRACK_FAVOURITE;

public class ChartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = "ChartAdapter ";
    private List<TrackInfo> trackInfos;
    private Context context;
    private RecyclerView recyclerView;
    private TrackInfo chosenTrackInfo;
    IChoseTrack iChoseTrack;
    ILongClickTrack iLongClickTrack;
    ITrackOperation iTrackOperation;

    public final int TYPE_TRACK = 0;
    public final int TYPE_LOAD = 1;
    OnLoadMoreListener loadMoreListener;
    boolean isLoading = false, isMoreDataAvailable = true;

    class TrackViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_chart_lay_main_info)
        RelativeLayout relLayMainInfo;
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
        @Bind(R.id.item_chart_text_finish_date)
        TextView textFinishDate;
        @Bind(R.id.item_chart_lay_finish_date)
        RelativeLayout relLayFinishDate;

        TrackViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    class LoadViewHolder extends RecyclerView.ViewHolder {

        LoadViewHolder(View v) {
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
    public int getItemViewType(int position) {
        if (trackInfos.get(position).getDeezerId() != null) {
            return TYPE_TRACK;
        } else {
            return TYPE_LOAD;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Timber.d(TAG + " onCreateViewHolder ");
        if (viewType == TYPE_TRACK) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chart, parent, false);
            return new TrackViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progress_visible, parent, false);
            return new LoadViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Timber.d(TAG + " onBindViewHolder " + position);
        if (position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
            isLoading = true;
            loadMoreListener.onLoadMore();
        }

        if (getItemViewType(position) == TYPE_TRACK) {
            TrackInfo trackInfo = trackInfos.get(position);
            TrackViewHolder trackViewHolder = (TrackViewHolder) holder;
            trackViewHolder.title.setText(trackInfo.getTitle());
            trackViewHolder.artist.setText(trackInfo.getArtist());
            trackViewHolder.duration.setText(" " + trackInfo.getDuration());

            if (trackInfo.getUserNameLoad() != null) {
                trackViewHolder.textUsername.setVisibility(View.VISIBLE);
                trackViewHolder.textUsername.setText(trackInfo.getUserNameLoad() + " >");
                trackViewHolder.textUsername.setOnClickListener(view -> iTrackOperation.clickUser(trackInfo.getUserNameLoad()));
            } else {
                trackViewHolder.textUsername.setVisibility(View.GONE);
            }
//        holder.textPosition.setText(Integer.toString(position+1));
            Picasso.with(context).load(trackInfo.getCoverImage()).placeholder(R.drawable.empty_cover).into(trackViewHolder.artistImage);
            trackViewHolder.relLayInfo.setOnClickListener(view -> {
                SPlay.init().playList.clear();
                SPlay.init().playList.addAll(SPlay.init().showList);
                iChoseTrack.choseTrackForPlay(trackInfos.get(trackViewHolder.getAdapterPosition()), trackViewHolder.getAdapterPosition());
            });

            if (SPlay.init().playlistType == PlaylistType.MAIN || SPlay.init().playlistType == PlaylistType.RANDOM || SPlay.init().playlistType == PlaylistType.FINISHED) {
                trackViewHolder.relLayLikeMain.setVisibility(View.VISIBLE);
                trackViewHolder.textDislike.setText(Long.toString(trackInfo.getCountDislike()));
                trackViewHolder.textLike.setText(Long.toString(trackInfo.getCountLike()));
                trackViewHolder.relLayInfo.setOnLongClickListener(v -> {
                    iLongClickTrack.onLongClickTrack(trackInfos.get(trackViewHolder.getAdapterPosition()));
                    return true;
                });

                fillNothing(trackViewHolder);
                if (trackInfo.getUserLike() != null) {
                    if (trackInfo.getUserLike()) {
                        fillLikes(trackViewHolder);
                    } else {
                        fillDisLikes(trackViewHolder);
                    }
                }

                trackViewHolder.relLayLike.setOnClickListener(view -> {
                    if(trackInfo.getUserLike() == null){
                        // add Userlike
                        Long newLike = trackInfo.getCountLike() + 1;
                        trackViewHolder.textLike.setText(Long.toString(newLike));
                        trackInfo.setCountLike(newLike);
                        trackInfo.setUserLike(true);
                        fillLikes(trackViewHolder);
                        iTrackOperation.trackLike(trackInfo.getDeezerId(), true);
                    } else {
                        if(trackInfo.getUserLike()){
                            // delete Userlike
                            Long newLike = trackInfo.getCountLike() - 1;
                            trackViewHolder.textLike.setText(Long.toString(newLike));
                            trackInfo.setCountLike(newLike);
                            trackInfo.setUserLike(null);
                            fillNothing(trackViewHolder);
                            iTrackOperation.delteLike(trackInfo.getDeezerId(), true);
                        } else {
                            // update Userlike to dislike
                            Long newLike = trackInfo.getCountLike() + 1;
                            trackViewHolder.textLike.setText(Long.toString(newLike));
                            trackInfo.setCountLike(newLike);
                            Long newDisLike = trackInfo.getCountDislike() - 1;
                            trackViewHolder.textDislike.setText(Long.toString(newDisLike));
                            trackInfo.setCountDislike(newDisLike);
                            fillNothing(trackViewHolder);
                            fillLikes(trackViewHolder);
                            trackInfo.setUserLike(true);
                            iTrackOperation.updateLike(trackInfo.getDeezerId(), true);
                        }
                    }
                });

                trackViewHolder.relLayDislike.setOnClickListener(view -> {
                    if(trackInfo.getUserLike() == null){
                        // add UserDislike
                        Long newDislike = trackInfo.getCountDislike() + 1;
                        trackViewHolder.textDislike.setText(Long.toString(newDislike));
                        trackInfo.setCountDislike(newDislike);
                        trackInfo.setUserLike(false);
                        fillDisLikes(trackViewHolder);
                        iTrackOperation.trackLike(trackInfo.getDeezerId(), false);
                    } else {
                        if(!trackInfo.getUserLike()){
                            // delete UserDislike
                            Long newDislike = trackInfo.getCountDislike() - 1;
                            trackViewHolder.textDislike.setText(Long.toString(newDislike));
                            trackInfo.setCountDislike(newDislike);
                            trackInfo.setUserLike(null);
                            fillNothing(trackViewHolder);
                            iTrackOperation.delteLike(trackInfo.getDeezerId(), false);
                        } else {
                            // update UserDislike ti like
                            Long newDislike = trackInfo.getCountDislike() + 1;
                            trackViewHolder.textDislike.setText(Long.toString(newDislike));
                            trackInfo.setCountDislike(newDislike);
                            Long newLike = trackInfo.getCountLike() - 1;
                            trackViewHolder.textLike.setText(Long.toString(newLike));
                            trackInfo.setCountLike(newLike);
                            fillNothing(trackViewHolder);
                            fillDisLikes(trackViewHolder);
                            trackInfo.setUserLike(false);
                            iTrackOperation.updateLike(trackInfo.getDeezerId(), false);
                        }
                    }
                });

                if (trackInfo.getFinishDate() != null) {
                    fillLikes(trackViewHolder);
                    fillDisLikes(trackViewHolder);
                    clickableLikes(trackViewHolder, false);
                }

            } else if (SPlay.init().playlistType == PlaylistType.USER) {
                trackViewHolder.relLayLikeMain.setVisibility(View.VISIBLE);
                trackViewHolder.textDislike.setText(Long.toString(trackInfo.getCountDislike()));
                trackViewHolder.textLike.setText(Long.toString(trackInfo.getCountLike()));
                trackViewHolder.relLayLike.setOnClickListener(null);
                trackViewHolder.relLayDislike.setOnClickListener(null);
                fillLikes(trackViewHolder);
                fillDisLikes(trackViewHolder);
                clickableLikes(trackViewHolder, false);
                trackViewHolder.textUsername.setVisibility(View.GONE);
            } else {
                trackViewHolder.relLayLikeMain.setVisibility(View.GONE);
            }

            if (SPlay.init().playTrackId != null && SPlay.init().playTrackId == trackInfos.get(position).getDeezerId()) {
                Timber.d(TAG + " position VISIBLE " + position);
                trackViewHolder.relLayMainInfo.setBackgroundColor(context.getResources().getColor(R.color.colorLightBlue));
            } else {
                trackViewHolder.relLayMainInfo.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundTransparent));
            }

            if (SPlay.init().playTrackId != null && SPlay.init().playTrackId == trackInfos.get(position).getDeezerId()) {
                Timber.d(TAG + " position VISIBLE " + position);
                trackViewHolder.indicator.setVisibility(View.VISIBLE);
                trackViewHolder.artistImage.setAlpha(0.3f);
            } else {
                trackViewHolder.indicator.setVisibility(View.GONE);
                trackViewHolder.artistImage.setAlpha(1f);
            }

            if (SPlay.init().playlistType == PlaylistType.FINISHED && trackInfos.get(position).getFirst()) {
                trackViewHolder.textFinishDate.setText(context.getString(R.string.period_top_date_text) + " " + StaticUtils.dateFormat(trackInfos.get(position).getFinishDate()));
                trackViewHolder.relLayFinishDate.setVisibility(View.VISIBLE);
            } else {
                trackViewHolder.relLayFinishDate.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void updateAdapter(List<TrackInfo> newList) {
        trackInfos = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return trackInfos.size();
    }

    private void fillLikes(TrackViewHolder holder) {
        int colorOrange = context.getResources().getColor(R.color.colorOrange);
        holder.imageLike.setColorFilter(colorOrange);
        holder.textLike.setTextColor(colorOrange);
    }

    private void fillDisLikes(TrackViewHolder holder) {
        int colorAccent = context.getResources().getColor(R.color.colorAccent);
        holder.imageDislike.setColorFilter(colorAccent);
        holder.textDislike.setTextColor(colorAccent);
    }

    private void fillNothing(TrackViewHolder holder) {
        int colorLightOrange = context.getResources().getColor(R.color.colorLightOrange);
        holder.imageLike.setColorFilter(colorLightOrange);
        holder.textLike.setTextColor(colorLightOrange);
        int colorBlue = context.getResources().getColor(R.color.colorBlue);
        holder.imageDislike.setColorFilter(colorBlue);
        holder.textDislike.setTextColor(colorBlue);
    }

    private void clickableLikes(TrackViewHolder holder, boolean clickable) {
        holder.relLayLike.setClickable(clickable);
        holder.relLayDislike.setClickable(clickable);
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    /* notifyDataSetChanged is final method so we can't override it
         call adapter.notifyDataChanged(); after update the list
         */
    public void notifyDataChanged() {
        notifyDataSetChanged();
        isLoading = false;
    }


    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }
}