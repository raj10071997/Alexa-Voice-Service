package com.game.dhanraj.myownalexa;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.game.dhanraj.myownalexa.sharedpref.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.game.dhanraj.myownalexa.Constants.BASE_THEME;

public class RingtonesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RingtoneAdapter ringtoneAdapter;
    private ArrayList<String> ringtones;
    private Toolbar toolbar;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtones);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ringtones");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        try {
            String[] ringtonesNames = getAssets().list("ringtones");
            ringtones = new ArrayList<String>(Arrays.asList(ringtonesNames));
        } catch (IOException e) {
            e.printStackTrace();
        }

        preferences = Util.getPrefernces(RingtonesActivity.this);

        int theme = preferences.getInt(BASE_THEME, R.color.light_background);
        findViewById(R.id.ringtone_layout).setBackgroundColor(theme);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewRingtones);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*ringtoneAdapter = new RingtoneAdapter(this, ringtones, new OnItemClickListener() {
            @Override
            public void onItemClick(Boolean check, CheckBox checkBox, String name) {
                if(check)
                    checkBox.setChecked(true);
                else {
                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                    preferencesEditor.putString("SelectedRingtone", name);
                    preferencesEditor.apply();
                    checkBox.setChecked(true);
                }
            }
        });*/

        ringtoneAdapter = new RingtoneAdapter(this, ringtones);
        recyclerView.setAdapter(ringtoneAdapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                //Values are passing to activity & to fragment as well
                String name = ringtoneAdapter.getItem(position);
                String selectedRingtone = preferences.getString("SelectedRingtone", "alarm.mp3");
                if(!name.equals(selectedRingtone)) {
                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                    preferencesEditor.putString("SelectedRingtone", name);
                    preferencesEditor.apply();
                    ringtoneAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
