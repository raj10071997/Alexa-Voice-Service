package com.game.dhanraj.myownalexa.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.game.dhanraj.myownalexa.ClickListener;
import com.game.dhanraj.myownalexa.DatabaseForAlarmAndTimer.DataBase;
import com.game.dhanraj.myownalexa.DownChannel;
import com.game.dhanraj.myownalexa.R;
import com.game.dhanraj.myownalexa.RecyclerTouchListener;
import com.game.dhanraj.myownalexa.sharedpref.Util;

import static com.game.dhanraj.myownalexa.Constants.BASE_THEME;
import static com.game.dhanraj.myownalexa.Constants.BASE_THEME_INTEGER;

/**
 * Created by Dhanraj on 10-06-2017.
 */

public class MyAlarm extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerViewForAlarmAndTimerAdapter recyclerViewForAlarmAndTimerAdapter;
    private DataBase db;
    private int idtoDelete;
    private RecyclerViewForAlarmAndTimerAdapter.ViewHolder myView;
    private AlarmManager alarmManager;
    private DownChannel downChannel;
    private boolean mBounded;
    private Toolbar toolbar;
    private int theme, themeInteger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myalarm);

        db = new DataBase(this);

        alarmManager =  (AlarmManager) getSystemService(ALARM_SERVICE);

        downChannel = new DownChannel();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Alarms and Timers");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        SharedPreferences preferences = Util.getPrefernces(MyAlarm.this);
        theme = preferences.getInt(BASE_THEME, ContextCompat.getColor(MyAlarm.this, R.color.light_background));
        themeInteger = preferences.getInt(BASE_THEME_INTEGER, 1);
        setUpLayout();

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerViewForAlarmAndTimerAdapter = new RecyclerViewForAlarmAndTimerAdapter(this);
        recyclerView.setAdapter(recyclerViewForAlarmAndTimerAdapter);

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
                AlarmConstants myconst = recyclerViewForAlarmAndTimerAdapter.getItem(position);
                prompts(myconst.getAlarmKeyId(),position,view);
            }
        }));
    }

    private void setUpLayout() {
        findViewById(R.id.alarm_layout).setBackgroundColor(theme);
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
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void prompts(final int ID, final int position, final View view)
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
                                delete(ID,position,view);
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


    private void delete(int ID,int position, View view) {
        db.deleteTheRow(ID);
       // downChannel.cancelPendingIntent(ID);

        Intent i = new Intent(MyAlarm.this, AlarmReceiver.class);
      //  PendingIntent pendingIntent = PendingIntent.getBroadcast(MyAlarm.this,ID,i,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent.getBroadcast(MyAlarm.this,ID,i,PendingIntent.FLAG_CANCEL_CURRENT).cancel();
        //alarmManager.cancel(pendingIntent);
        Snackbar.make(view, "Alarm Deleted", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        refreshData();
    }

    private void refreshData() {
        recyclerViewForAlarmAndTimerAdapter = new RecyclerViewForAlarmAndTimerAdapter(this);
        recyclerView.swapAdapter(recyclerViewForAlarmAndTimerAdapter,false);
    }
}
