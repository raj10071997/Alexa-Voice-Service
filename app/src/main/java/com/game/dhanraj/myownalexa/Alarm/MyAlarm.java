package com.game.dhanraj.myownalexa.Alarm;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.game.dhanraj.myownalexa.DatabaseForAlarmAndTimer.DataBase;
import com.game.dhanraj.myownalexa.R;

/**
 * Created by Dhanraj on 10-06-2017.
 */

public class MyAlarm extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerViewForAlarmAndTimer recyclerViewForAlarmAndTimer;
    private DataBase db;
    private int idtoDelete;
    private RecyclerViewForAlarmAndTimer.ViewHolder myView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myalarm);

        db = new DataBase(this);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerViewForAlarmAndTimer = new RecyclerViewForAlarmAndTimer(this);
        recyclerView.setAdapter(recyclerViewForAlarmAndTimer);

       // recyclerView.setHasFixedSize(true);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Values are passing to activity & to fragment as well

            }

            @Override
            public void onLongClick(View view, int position) {
            AlarmConstants myconst = recyclerViewForAlarmAndTimer.getItem(position);
                prompts(myconst.getAlarmKeyId(),position);
               Toast.makeText(MyAlarm.this,String.valueOf(myconst.getAlarmKeyId())+" "+myconst.getMytime(), Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
      refreshData();

    }

    public static interface ClickListener{
        public void onClick(View view, int position);
        public void onLongClick(View view,int position);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public void prompts(final int ID,final int position)
    {
        LayoutInflater li = LayoutInflater.from(MyAlarm.this);
        View promptsView2 = li.inflate(R.layout.prompts,null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MyAlarm.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView2);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                delete(ID,position);

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private void delete(int ID,int position) {
        db.deleteTheRow(ID);
        refreshData();

// all these solution don't work we only have to deference and then reference the new adapter as the old one will be garbage collected
       /* recyclerViewForAlarmAndTimer recyclerViewForAlarmAndTimer.updateAnswers(db.getDetailsOfAlarmAndTime());

     see documentation - says rely on notifyDataSetChanged as a last resort.
      recyclerViewForAlarmAndTimer.notifyDataSetChanged();
        recyclerView.invalidate();*/

    }

    private void refreshData() {
        recyclerViewForAlarmAndTimer = new RecyclerViewForAlarmAndTimer(this);
        recyclerView.swapAdapter(recyclerViewForAlarmAndTimer,false);
    }


}
