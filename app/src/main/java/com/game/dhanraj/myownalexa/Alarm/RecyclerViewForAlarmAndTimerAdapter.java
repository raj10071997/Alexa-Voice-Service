package com.game.dhanraj.myownalexa.Alarm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.game.dhanraj.myownalexa.DatabaseForAlarmAndTimer.DataBase;
import com.game.dhanraj.myownalexa.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dhanraj on 20-06-2017.
 */

public class RecyclerViewForAlarmAndTimerAdapter extends RecyclerView.Adapter<RecyclerViewForAlarmAndTimerAdapter.ViewHolder> {

    private LayoutInflater layoutInflater;
    private ArrayList<AlarmConstants> myConstants;
    private DataBase db;

    public RecyclerViewForAlarmAndTimerAdapter(Context context){
        layoutInflater = LayoutInflater.from(context);
        myConstants = new ArrayList<>();
        db = new DataBase(context);
        myConstants = db.getDetailsOfAlarmAndTime();
    }

    @Override
    public RecyclerViewForAlarmAndTimerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_row_recycler,parent,false);
        ViewHolder myHolder = new ViewHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewForAlarmAndTimerAdapter.ViewHolder holder, int position) {
        AlarmConstants nwConstants = myConstants.get(position);
        holder.myTime.setText(nwConstants.getMytime());
        holder.myType.setText(nwConstants.getType());
        holder.myImage.setImageResource(nwConstants.getIconsIDs());
        holder.id.setText(String.valueOf(nwConstants.getAlarmKeyId()));
    }

    @Override
    public int getItemCount() {
        return myConstants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView myTime;
        private CircleImageView myImage;
        private TextView myType;
        private TextView id;

        public ViewHolder(View itemView) {
            super(itemView);
            myTime = (TextView) itemView.findViewById(R.id.textoftime);
            myImage = (CircleImageView) itemView.findViewById(R.id.circleimage);
            myType = (TextView) itemView.findViewById(R.id.textType);
            id = (TextView) itemView.findViewById(R.id.myId);
        }
    }

    public void updateAnswers(ArrayList<AlarmConstants> items) {
        myConstants.clear();
        myConstants = items;
        notifyDataSetChanged();
    }

    public void updateData()
    {
        myConstants = db.getDetailsOfAlarmAndTime();
    }

    public AlarmConstants getItem(int position)
    {
        return myConstants.get(position);
    }

}
