package com.game.dhanraj.myownalexa;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.game.dhanraj.myownalexa.sharedpref.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.game.dhanraj.myownalexa.Constants.BASE_THEME;
import static com.game.dhanraj.myownalexa.Constants.PREF_REFRESH_TOKEN;

public class SplashActivitiy extends AppCompatActivity {

    private int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activitiy);
        EventBus.getDefault().register(this);

        int theme = Util.getPrefernces(SplashActivitiy.this).getInt(BASE_THEME, ContextCompat.getColor(SplashActivitiy.this, R.color.light_background));
        findViewById(R.id.splash_layout).setBackgroundColor(theme);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences= Util.getPrefernces(SplashActivitiy.this);
                if (preferences.contains(PREF_REFRESH_TOKEN)) {
                    ConnectivityManager connectivityManager
                            = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if(activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Intent st = new Intent(SplashActivitiy.this,DownChannel.class);
                        startService(st);
                    } else  {
                        Intent i = new Intent(SplashActivitiy.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                } else {
                    Intent i = new Intent(SplashActivitiy.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MessageEvent event){
        switch (event.event){
            case TokenHandler.SplashActivity:
                    if(event.message.equals("finishSplashActivity")) {
                        Log.d("checkthismethod","splashscreen");
                        finish();
                    }
                break;
        }
    }
}
