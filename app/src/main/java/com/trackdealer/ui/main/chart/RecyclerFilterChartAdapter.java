package com.trackdealer.ui.main.chart;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.model.Genre;
import com.trackdealer.R;
import com.trackdealer.utils.Prefs;

import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_FILTER;

/**
 * Created by grechnev-av on 27.07.2017.
 */

public class RecyclerFilterChartAdapter extends RecyclerView.Adapter<RecyclerFilterChartAdapter.ViewHolder> {

    private List<Genre> genres;
    private Context context;
    private RecyclerView recyclerView;
    DialogFilterClickListener dialogItemClickListener;

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_image_info_lay_main)
        RelativeLayout relLayMain;
        @Bind(R.id.item_image_info_image)
        ImageView imageView;
        @Bind(R.id.item_image_info_text)
        TextView text;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public RecyclerFilterChartAdapter(List<Genre> genres, Context context, DialogFilterClickListener dialogItemClickListener) {
        this.genres = genres;
        this.context = context;
        this.dialogItemClickListener = dialogItemClickListener;
    }

    @Override
    public RecyclerFilterChartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                    int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_info, parent, false);
        ViewHolder vh = new ViewHolder(v);
        vh.text.setTextColor(context.getResources().getColor(randomColor()));
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerFilterChartAdapter.ViewHolder holder, int position) {
        Genre genre = genres.get(position);
        holder.text.setText(genre.getName());

        holder.relLayMain.setOnClickListener(view -> {
            Prefs.putString(context, SHARED_FILENAME_USER_DATA, SHARED_KEY_FILTER, genres.get(position).getName());
            dialogItemClickListener.filterClickStart();
        });
    }

    public int randomColor(){
        Random rnd = new Random();
        int count = rnd.nextInt(7);
        switch (count) {
            case 0 : return R.color.colorRandom1;
            case 1 : return R.color.colorRandom2;
            case 2 : return R.color.colorRandom3;
            case 3 : return R.color.colorRandom4;
            case 4 : return R.color.colorRandom5;
            case 5 : return R.color.colorRandom6;
            case 6 : return R.color.colorRandom7;
            default : return R.color.colorAccent;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }
}
