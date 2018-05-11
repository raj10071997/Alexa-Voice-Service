package com.game.dhanraj.myownalexa;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.game.dhanraj.myownalexa.sharedpref.Util;
import com.mikepenz.iconics.view.IconicsImageView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.game.dhanraj.myownalexa.Constants.BASE_THEME;
import static com.game.dhanraj.myownalexa.Constants.BASE_THEME_INTEGER;
import static com.game.dhanraj.myownalexa.Constants.DARK_THEME;
import static com.game.dhanraj.myownalexa.Constants.LIGHT_THEME;

public class SettingsActivity extends AppCompatActivity {

    //DOUBT - are static variables independent of activity lifecycle

    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    public int theme, theme_integer;
    private ArrayList<String> ringtones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sharedPreferences = Util.getPrefernces(SettingsActivity.this);
        theme = sharedPreferences.getInt(BASE_THEME, ContextCompat.getColor(SettingsActivity.this, R.color.light_background));
        theme_integer = sharedPreferences.getInt(BASE_THEME_INTEGER, 1);
        findViewById(R.id.settings_layout).setBackgroundColor(theme);

        findViewById(R.id.ll_alarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsActivity.this, RingtonesActivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.ll_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseThemeDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @SuppressLint("ResourceAsColor")
    private void baseThemeDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this, getDialogStyle());

        final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_basic_theme, null);
        final TextView dialogTitle = (TextView) dialogLayout.findViewById(R.id.basic_theme_title);
        final CardView dialogCardView = (CardView) dialogLayout.findViewById(R.id.basic_theme_card);

        final IconicsImageView themeIconWhite = (IconicsImageView) dialogLayout.findViewById(R.id.white_basic_theme_icon);
        final IconicsImageView themeIconDark = (IconicsImageView) dialogLayout.findViewById(R.id.dark_basic_theme_icon);
        final IconicsImageView whiteSelect = (IconicsImageView) dialogLayout.findViewById(R.id.white_basic_theme_select);
        final IconicsImageView darkSelect = (IconicsImageView) dialogLayout.findViewById(R.id.dark_basic_theme_select);

        themeIconWhite.setIcon("gmd-invert-colors");
        themeIconDark.setIcon("gmd-invert-colors");
        whiteSelect.setIcon("gmd-done");
        darkSelect.setIcon("gmd-done");

        switch (getBaseThemeInteger()) {
            case LIGHT_THEME:
                whiteSelect.setVisibility(View.VISIBLE);
                darkSelect.setVisibility(View.GONE);
                break;
            case DARK_THEME:
                whiteSelect.setVisibility(View.GONE);
                darkSelect.setVisibility(View.VISIBLE);
                break;
        }

        /** SET OBJ THEME **/
        dialogCardView.setCardBackgroundColor(R.color.cardview_light_background);

        dialogLayout.findViewById(R.id.ll_white_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.VISIBLE);
                darkSelect.setVisibility(View.GONE);
                setBaseTheme(LIGHT_THEME, false);
            }
        });
        dialogLayout.findViewById(R.id.ll_dark_basic_theme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whiteSelect.setVisibility(View.GONE);
                darkSelect.setVisibility(View.VISIBLE);
                setBaseTheme(DARK_THEME, false);
            }
        });

        dialogBuilder.setView(dialogLayout);
        dialogBuilder.setPositiveButton(getString(R.string.ok_action).toUpperCase(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor preferences = sharedPreferences.edit();
                preferences.putInt(BASE_THEME, getBaseTheme());
                preferences.putInt(BASE_THEME_INTEGER, getBaseThemeInteger());
                preferences.apply();
                findViewById(R.id.settings_layout).setBackgroundColor(theme);
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // do nothing
            }
        });
        dialogBuilder.setView(dialogLayout);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private int getBaseThemeInteger() { return theme_integer; }

    private int getBaseTheme() {
            return theme;
    }

    private int getDialogStyle() {
        int style;
        switch (getBaseTheme()) {
            case R.color.dark_background:
                style = R.style.AlertDialog_Dark;
                break;
           // case AMOLED_THEME: style = R.style.AlertDialog_Dark_Amoled;break;
            case R.color.light_background:
                style = R.style.AlertDialog_Light;
                break;
            default:
                style = R.style.AlertDialog_Light;
        }
        return style;
    }

    private void setBaseTheme(int theme, boolean permanent) {
        if(permanent){
           // TODO: implement it
        } else {
            switch(theme) {
                case LIGHT_THEME :
                    this.theme = ContextCompat.getColor(SettingsActivity.this, R.color.light_background);
                    theme_integer = LIGHT_THEME;
                    break;
                case DARK_THEME:
                    this.theme = ContextCompat.getColor(SettingsActivity.this, R.color.dark_background);
                    theme_integer = DARK_THEME;
                    break;
            }
        }
    }

}
