package com.game.dhanraj.myownalexa.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.game.dhanraj.myownalexa.DatabaseForAlarmAndTimer.DataBase;
import com.game.dhanraj.myownalexa.DownChannel;
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
    private AlarmManager alarmManager;
    private DownChannel downChannel;
    private boolean mBounded;
    private Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myalarm);

        db = new DataBase(this);

        alarmManager =  (AlarmManager) getSystemService(ALARM_SERVICE);

        downChannel = new DownChannel();

        toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Alarms and Timers");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerViewForAlarmAndTimer = new RecyclerViewForAlarmAndTimer(this);
        recyclerView.setAdapter(recyclerViewForAlarmAndTimer);

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

    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this,DownChannel.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

    }

    ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            downChannel = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            DownChannel.LocalBinder mLocalBinder = (DownChannel.LocalBinder)service;
            downChannel = mLocalBinder.getServerInstance();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
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

       // downChannel.cancelPendingIntent(ID);

        Intent i = new Intent(MyAlarm.this, AlarmReceiver.class);
      //  PendingIntent pendingIntent = PendingIntent.getBroadcast(MyAlarm.this,ID,i,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent.getBroadcast(MyAlarm.this,ID,i,PendingIntent.FLAG_CANCEL_CURRENT).cancel();
        //alarmManager.cancel(pendingIntent);
        refreshData();


    }

    private void refreshData() {
        recyclerViewForAlarmAndTimer = new RecyclerViewForAlarmAndTimer(this);
        recyclerView.swapAdapter(recyclerViewForAlarmAndTimer,false);
    }




}
