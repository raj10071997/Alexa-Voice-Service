package com.game.dhanraj.myownalexa;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.game.dhanraj.myownalexa.Alarm.AlarmConstants;
import com.game.dhanraj.myownalexa.Alarm.RecyclerViewForAlarmAndTimerAdapter;
import com.game.dhanraj.myownalexa.sharedpref.Util;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dhanraj on 11/5/18.
 */

public class RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private ArrayList<String> ringtones;
    private SharedPreferences preferences;
//    private final OnItemClickListener listener;

    public RingtoneAdapter(Context context, ArrayList<String> ringtones) {
        layoutInflater = LayoutInflater.from(context);
        this.ringtones = ringtones;
        preferences = Util.getPrefernces(context);
//        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_row_recycler_ringtone,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String selectedRingTone = preferences.getString("SelectedRingtone", "alarm.mp3");
        holder.bind(ringtones.get(position), selectedRingTone);
    }

    @Override
    public int getItemCount() {
        return ringtones.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView ringtone;
        private CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            ringtone = (TextView) itemView.findViewById(R.id.textofringtonename);
            checkBox = (CheckBox) itemView.findViewById(R.id.ringtonecheckbox);
        }

        private void bind(final String name, final String selectedRingtone) {
            ringtone.setText(name);
            if(name.equals(selectedRingtone)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }

           /* itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(name.equals(selectedRingtone), checkBox, name);
                }
            });*/
        }
    }

    public String getItem(int position){ return ringtones.get(position); }
}
