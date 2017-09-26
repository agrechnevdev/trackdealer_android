package com.trackdealer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trackdealer.R;
import com.trackdealer.utils.Prefs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.trackdealer.utils.ConstValues.SHARED_FILENAME_USER_DATA;
import static com.trackdealer.utils.ConstValues.SHARED_KEY_LOG_ERROR;

/**
 * Created by grechnev-av on 26.09.2017.
 */

public class LogActivity extends BaseActivity {

    @Bind(R.id.log_recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ButterKnife.bind(this);
        HashMap<String, String> logMap = Prefs.getHashMap(this, SHARED_FILENAME_USER_DATA, SHARED_KEY_LOG_ERROR);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(llm);
        LogAdapter mTracksAdapter = new LogAdapter(logMap, this);
        recyclerView.setAdapter(mTracksAdapter);
    }

    public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {

        HashMap<String, String> logMap;
        private Context context;
        private RecyclerView recyclerView;
        List<String> keys;
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());

        public LogAdapter(HashMap<String, String> logMap, Context context) {
            this.logMap = logMap;
            this.context = context;
            if(logMap != null) {
                keys = new ArrayList<>(logMap.keySet());
                Collections.sort(keys, comparator);
                this.logMap = logMap;
            }
            else {
                keys = new ArrayList<>();
                this.logMap = new HashMap<>();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @Bind(R.id.item_log_date)
            TextView textDate;

            @Bind(R.id.item_log_message)
            TextView textMessage;

            ViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_log, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(LogAdapter.ViewHolder holder, int position) {
            String date = keys.get(position);
            String message = logMap.get(keys.get(position));
            holder.textDate.setText(date);
            holder.textMessage.setText(message);
        }

        @Override
        public int getItemCount() {
            return keys.size();
        }

        Comparator<String> comparator = (o1, o2) -> {
            try {
                Date date1 = sdf.parse(o1);
                Date date2 = sdf.parse(o2);
                if (date1.before(date2))
                    return 1;
                else if (date1.after(date2))
                    return -1;
                else
                    return 0;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        };
    }


}
